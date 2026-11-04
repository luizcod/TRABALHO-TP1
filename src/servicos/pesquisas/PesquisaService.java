package servicos.pesquisas;

import dominio.administracao.Perfil;
import servicos.administracao.Autenticacao;
import dominio.candidatospartidos.Candidato;
import dominio.candidatospartidos.Cargo;
import dominio.pesquisas.*;
import excecoes.*;
import persistencia.pesquisas.PesquisaRepository;
import persistencia.pesquisas.PesquisaRepositoryArquivo;

import java.util.*;

/**
 * Camada de Serviço e Lógica de Negócio para o Módulo de Pesquisas (Etapa 3 - TP1 / UnB).
 * Responsável pelo cadastro, configuração, ciclo de vida e acompanhamento amostral das pesquisas,
 * implementando todas as regras de negócio e validações de integridade.
 */
public class PesquisaService {

    private final PesquisaRepository pesquisaRepository;
    private final Autenticacao autenticacao = Autenticacao.getInstancia();

    private static PesquisaService instanciaSingleton;

    public static synchronized PesquisaService getInstance() throws PersistenciaException {
        if (instanciaSingleton == null) {
            instanciaSingleton = new PesquisaService(PesquisaRepositoryArquivo.getInstance());
        }
        return instanciaSingleton;
    }

    public PesquisaService() throws PersistenciaException {
        this(PesquisaRepositoryArquivo.getInstance());
    }

    public PesquisaService(PesquisaRepository pesquisaRepository) {
        if (pesquisaRepository == null) {
            throw new IllegalArgumentException("O repositório de pesquisas não pode ser nulo.");
        }
        this.pesquisaRepository = pesquisaRepository;
    }

    // =========================================================================
    // CADASTRO E GERENCIAMENTO DE PESQUISA
    // =========================================================================

    /**
     * Cadastra uma nova pesquisa eleitoral no sistema.
     * Valida os campos obrigatórios, datas e impede código duplicado.
     */
    public void cadastrarPesquisa(Pesquisa pesquisa) throws ExcecaoRegraNegocio {
        autenticacao.exigirPerfil(Perfil.PESQUISADOR);
        validarDadosPesquisa(pesquisa);

        Optional<Pesquisa> existente = pesquisaRepository.buscarPesquisaPorCodigo(pesquisa.getCodigo());
        if (existente.isPresent()) {
            throw new ExcecaoRegraNegocio(String.format("Já existe uma pesquisa cadastrada com o código '%s'.", pesquisa.getCodigo()));
        }

        pesquisa.setSituacao(SituPesquisa.PLANEJADA);
        pesquisaRepository.salvarPesquisa(pesquisa);
    }

    /**
     * Atualiza os dados cadastrais de uma pesquisa existente.
     *
     * [REGRA DE NEGÓCIO]
     * Se a SituPesquisa estiver ENCERRADA ou PROCESSADA, bloquear edições/alterações
     * lançando PesquisaBloqueadaException.
     */
    public void atualizarPesquisa(Pesquisa pesquisa) throws ExcecaoRegraNegocio {
        autenticacao.exigirPerfil(Perfil.PESQUISADOR);
        validarDadosPesquisa(pesquisa);

        Pesquisa existente = buscarPesquisaPorCodigo(pesquisa.getCodigo());

        // [REGRA DE NEGÓCIO] Bloqueio se ENCERRADA ou PROCESSADA
        verificarBloqueioEdicao(existente);

        existente.setNome(pesquisa.getNome());
        existente.setCargo(pesquisa.getCargo());
        existente.setTipoPesquisa(pesquisa.getTipoPesquisa());
        existente.definirRegioes(pesquisa.getRegioes());
        existente.setDataInicio(pesquisa.getDataInicio());
        existente.setDataFim(pesquisa.getDataFim());
        existente.setQtdPrevista(pesquisa.getQtdPrevista());

        pesquisaRepository.atualizarPesquisa(existente);
    }

    /**
     * Exclui uma pesquisa do sistema.
     * Somente pesquisas na situação PLANEJADA podem ser excluídas.
     */
    public void removerPesquisa(String codigo) throws ExcecaoRegraNegocio {
        autenticacao.exigirPerfil(Perfil.PESQUISADOR);
        Pesquisa pesquisa = buscarPesquisaPorCodigo(codigo);
        verificarBloqueioEdicao(pesquisa);

        if (pesquisa.getSituacao() != SituPesquisa.PLANEJADA) {
            throw new ExcecaoRegraNegocio(String.format("Não é permitido excluir pesquisa na situação '%s'.", pesquisa.getSituacao()));
        }

        pesquisaRepository.removerPesquisa(codigo);
    }

    public Pesquisa buscarPesquisaPorCodigo(String codigo) throws ExcecaoRegraNegocio {
        autenticacao.exigirAutenticacao();
        return pesquisaRepository.buscarPesquisaPorCodigo(codigo)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Pesquisa não encontrada com o código: " + codigo));
    }

    public List<Pesquisa> listarPesquisas() throws PersistenciaException {
        autenticacao.exigirAutenticacao();
        return pesquisaRepository.listarPesquisas();
    }

    public List<Pesquisa> listarPesquisasPorCargo(Cargo cargo) throws PersistenciaException {
        autenticacao.exigirAutenticacao();
        return pesquisaRepository.listarPesquisasPorCargo(cargo);
    }

    public List<Pesquisa> listarPesquisasPorSituacao(SituPesquisa situacao) throws PersistenciaException {
        autenticacao.exigirAutenticacao();
        return pesquisaRepository.listarPesquisasPorSituacao(situacao);
    }

    // =========================================================================
    // ASSOCIAÇÃO DE CANDIDATO À PESQUISA
    // =========================================================================

    /**
     * Associa um Candidato a uma Pesquisa Eleitoral.
     *
     * [REGRAS DE NEGÓCIO IMPLEMENTADAS]:
     * 1. Bloqueia alteração se a pesquisa estiver ENCERRADA ou PROCESSADA (PesquisaBloqueadaException).
     * 2. Valida se o Cargo do candidato é o mesmo da pesquisa (CargoIncompativelException).
     * 3. Valida se a candidatura está validada/ativa para pesquisa (CandidaturaInvalidaException).
     * 4. Valida se o candidato já não está associado a ela (CandidatoJaAssociadoException).
     */
    public void associarCandidatoAPesquisa(String codigoPesquisa, Candidato candidato) throws ExcecaoRegraNegocio {
        autenticacao.exigirPerfil(Perfil.PESQUISADOR);
        if (candidato == null) {
            throw new ExcecaoRegraNegocio("O candidato informado não pode ser nulo.");
        }

        Pesquisa pesquisa = buscarPesquisaPorCodigo(codigoPesquisa);

        // [REGRA DE NEGÓCIO] Bloqueio se ENCERRADA ou PROCESSADA
        verificarBloqueioEdicao(pesquisa);

        // [REGRA DE NEGÓCIO] Cargo do candidato deve ser compatível com o cargo da pesquisa
        if (candidato.getCargo() != pesquisa.getCargo()) {
            throw new CargoIncompativelException(candidato.getNome(), candidato.getCargo(), pesquisa.getCargo());
        }

        // [REGRA DE NEGÓCIO] Somente candidatura validada/ativa pode participar de pesquisa
        if (!candidato.isAptoParaPesquisa()) {
            String situacao = (candidato.getCandidatura() != null) ? candidato.getCandidatura().getSituacao() : "INDEFINIDA";
            throw new CandidaturaInvalidaException(candidato.getNome(), situacao);
        }

        // [REGRA DE NEGÓCIO] Impedir candidato duplicado na mesma pesquisa
        if (pesquisa.contemCandidato(candidato)) {
            throw new CandidatoJaAssociadoException(candidato.getNome(), pesquisa.getCodigo());
        }

        pesquisa.associarCandidato(candidato);
        pesquisaRepository.atualizarPesquisa(pesquisa);
    }

    /**
     * Desassocia um candidato da pesquisa.
     * Bloqueia alterações caso a pesquisa esteja encerrada ou processada.
     */
    public void desassociarCandidatoDaPesquisa(String codigoPesquisa, Candidato candidato) throws ExcecaoRegraNegocio {
        autenticacao.exigirPerfil(Perfil.PESQUISADOR);
        if (candidato == null) {
            throw new ExcecaoRegraNegocio("O candidato informado não pode ser nulo.");
        }

        Pesquisa pesquisa = buscarPesquisaPorCodigo(codigoPesquisa);
        verificarBloqueioEdicao(pesquisa);

        pesquisa.desassociarCandidato(candidato);
        pesquisaRepository.atualizarPesquisa(pesquisa);
    }

    // =========================================================================
    // GERENCIAMENTO DE PERGUNTAS E PLANO AMOSTRAL
    // =========================================================================

    /**
     * Adiciona uma pergunta ao questionário da pesquisa.
     * Bloqueia alterações se a pesquisa estiver ENCERRADA ou PROCESSADA.
     */
    public void adicionarPergunta(String codigoPesquisa, Pergunta pergunta) throws ExcecaoRegraNegocio {
        autenticacao.exigirPerfil(Perfil.PESQUISADOR);
        Pesquisa pesquisa = buscarPesquisaPorCodigo(codigoPesquisa);
        verificarBloqueioEdicao(pesquisa);

        if (pergunta == null || pergunta.getTexto() == null || pergunta.getTexto().trim().isEmpty()) {
            throw new ExcecaoRegraNegocio("O texto da pergunta não pode ser vazio.");
        }

        pesquisa.adicionarPergunta(pergunta);
        pesquisaRepository.atualizarPesquisa(pesquisa);
    }

    /**
     * Define ou atualiza o Plano Amostral e as cotas/metas mínimas da pesquisa.
     * Bloqueia alterações se a pesquisa estiver ENCERRADA ou PROCESSADA.
     */
    public void definirPlanoAmostral(String codigoPesquisa, PlanoAmostral planoAmostral) throws ExcecaoRegraNegocio {
        autenticacao.exigirPerfil(Perfil.PESQUISADOR);
        Pesquisa pesquisa = buscarPesquisaPorCodigo(codigoPesquisa);
        verificarBloqueioEdicao(pesquisa);

        if (planoAmostral == null) {
            throw new ExcecaoRegraNegocio("O plano amostral não pode ser nulo.");
        }

        pesquisa.setPlanoAmostral(planoAmostral);
        pesquisaRepository.atualizarPesquisa(pesquisa);
    }

    // =========================================================================
    // CONTROLE DE CICLO DE VIDA E SITUAÇÃO
    // =========================================================================

    /**
     * Altera a situação da pesquisa, controlando o fluxo do ciclo de vida:
     * PLANEJADA -> EM_COLETA -> ENCERRADA -> PROCESSADA.
     *
     * [REGRA DE NEGÓCIO]:
     * - Se a pesquisa for do tipo 'Intenção de Voto', validar se possui ao menos
     *   1 candidato associado antes de transicionar para EM_COLETA.
     */
    public void alterarSituacao(String codigoPesquisa, SituPesquisa novaSituacao) throws ExcecaoRegraNegocio {
        autenticacao.exigirPerfil(novaSituacao == SituPesquisa.PROCESSADA ? Perfil.ANALISTA : Perfil.PESQUISADOR);
        Pesquisa pesquisa = buscarPesquisaPorCodigo(codigoPesquisa);

        if (pesquisa.getSituacao() == novaSituacao) {
            return;
        }
        if (!pesquisa.getSituacao().podeIrPara(novaSituacao)) {
            throw new ExcecaoRegraNegocio("Transição inválida: de " + pesquisa.getSituacao()
                    + " para " + novaSituacao + ".");
        }

        switch (novaSituacao) {
            case EM_COLETA:
                // [REGRA DE NEGÓCIO] Se a pesquisa for "Intenção de Voto", validar se possui ao menos 1 candidato
                if (pesquisa.isIntencaoDeVoto()) {
                    if (pesquisa.getCandidatosAssociados().isEmpty()) {
                        throw new PesquisaSemCandidatoException(pesquisa.getCodigo());
                    }
                }
                break;

            case ENCERRADA:
                if (pesquisa.getSituacao() != SituPesquisa.EM_COLETA) {
                    throw new ExcecaoRegraNegocio("Apenas pesquisas com status 'EM_COLETA' podem ser encerradas.");
                }
                break;

            case PROCESSADA:
                // [REGRA DO DOCUMENTO] Uma pesquisa somente poderá ser processada após o encerramento da coleta
                if (pesquisa.getSituacao() != SituPesquisa.ENCERRADA) {
                    throw new ExcecaoRegraNegocio("A pesquisa somente pode ser processada após ser devidamente encerrada (ENCERRADA).");
                }
                break;

            default:
                break;
        }

        pesquisa.setSituacao(novaSituacao);
        pesquisaRepository.atualizarPesquisa(pesquisa);
    }

    // =========================================================================
    // MÉTODO DE ACOMPANHAMENTO AMOSTRAL E METAS
    // =========================================================================

    /**
     * Método de Acompanhamento:
     * Compara entrevistas realizadas com a qtdPrevista e verifica metas mínimas atingidas em MetaAmostral.
     *
     * [REGRAS DE NEGÓCIO]:
     * - Apresentar quantidade prevista, quantidade realizada e percentual de conclusão (RF-09).
     * - Identificar pesquisas e grupos que não atingiram a quantidade mínima prevista (RN-10 e RF-10).
     *
     * @param codigoPesquisa Código da pesquisa a ser acompanhada
     * @param entrevistasRealizadas Quantidade total acumulada de entrevistas realizadas
     * @param entrevistasPorGrupo Mapa relacionando [Nome do Grupo de Amostra -> Total de entrevistas coletadas]
     * @return DTO RelatorioAcompanhamento com estatísticas detalhadas e alertas de déficit
     */
    public RelatorioAcompanhamento acompanharPesquisa(String codigoPesquisa,
                                                      int entrevistasRealizadas,
                                                      Map<String, Integer> entrevistasPorGrupo) throws ExcecaoRegraNegocio {
        autenticacao.exigirAutenticacao();
        Pesquisa pesquisa = buscarPesquisaPorCodigo(codigoPesquisa);

        int qtdPrevista = pesquisa.getQtdPrevista();
        double percentualConclusao = (qtdPrevista > 0) ? ((double) entrevistasRealizadas / qtdPrevista) * 100.0 : 0.0;

        List<String> gruposAbaixoDaMeta = new ArrayList<>();
        PlanoAmostral plano = pesquisa.getPlanoAmostral();

        if (plano != null && plano.getMetas() != null) {
            for (MetaAmostral meta : plano.getMetas()) {
                int realizadasNoGrupo = 0;
                if (entrevistasPorGrupo != null && entrevistasPorGrupo.containsKey(meta.getGrupo())) {
                    realizadasNoGrupo = entrevistasPorGrupo.get(meta.getGrupo());
                }

                // [REGRA DE NEGÓCIO] Verifica se a quantidade realizada atingiu o mínimo do grupo
                if (!meta.atingiuMeta(realizadasNoGrupo)) {
                    gruposAbaixoDaMeta.add(String.format("Grupo '%s': mínimo requerido = %d, realizado = %d (déficit de %d entrevistas)",
                            meta.getGrupo(), meta.getMinimo(), realizadasNoGrupo, (meta.getMinimo() - realizadasNoGrupo)));
                }
            }
        }

        return new RelatorioAcompanhamento(
                pesquisa.getCodigo(),
                pesquisa.getNome(),
                qtdPrevista,
                entrevistasRealizadas,
                percentualConclusao,
                entrevistasPorGrupo,
                gruposAbaixoDaMeta
        );
    }

    // =========================================================================
    // VALIDAÇÕES INTERNAS
    // =========================================================================

    /**
     * [REGRA DE NEGÓCIO] Se a SituPesquisa estiver ENCERRADA ou PROCESSADA,
     * bloquear edições/alterações lançando PesquisaBloqueadaException.
     */
    private void verificarBloqueioEdicao(Pesquisa pesquisa) throws PesquisaBloqueadaException {
        if (pesquisa.isBloqueada()) {
            throw new PesquisaBloqueadaException(pesquisa.getCodigo(), pesquisa.getSituacao());
        }
    }

    private void validarDadosPesquisa(Pesquisa pesquisa) throws ExcecaoRegraNegocio {
        if (pesquisa == null) {
            throw new ExcecaoRegraNegocio("A pesquisa não pode ser nula.");
        }
        if (pesquisa.getCodigo() == null || pesquisa.getCodigo().trim().isEmpty()) {
            throw new ExcecaoRegraNegocio("O código identificador da pesquisa é obrigatório.");
        }
        if (pesquisa.getNome() == null || pesquisa.getNome().trim().isEmpty()) {
            throw new ExcecaoRegraNegocio("O nome da pesquisa é obrigatório.");
        }
        if (pesquisa.getCargo() == null) {
            throw new ExcecaoRegraNegocio("O cargo definido para a pesquisa é obrigatório.");
        }
        if (pesquisa.getQtdPrevista() <= 0) {
            throw new ExcecaoRegraNegocio("A quantidade prevista de entrevistas deve ser maior que zero.");
        }
        if (pesquisa.getDataInicio() == null || pesquisa.getDataFim() == null) {
            throw new ExcecaoRegraNegocio("O período de coleta (início e fim) é obrigatório.");
        }
        if (pesquisa.getDataFim().isBefore(pesquisa.getDataInicio())) {
            throw new ExcecaoRegraNegocio("A data de término da pesquisa não pode ser anterior à data de início.");
        }
        if (pesquisa.getTipoPesquisa() == null) {
            throw new ExcecaoRegraNegocio("O tipo da pesquisa é obrigatório.");
        }
        if (pesquisa.getRegioes().isEmpty()) {
            throw new ExcecaoRegraNegocio("Informe ao menos uma região de abrangência.");
        }
    }
}
