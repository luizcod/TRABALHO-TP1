package servicos.coleta;

import dominio.administracao.*;
import dominio.coleta.*;
import dominio.pesquisas.OpcaoResposta;
import dominio.pesquisas.Pergunta;
import dominio.pesquisas.Pesquisa;
import dominio.pesquisas.RelatorioAcompanhamento;
import dominio.pesquisas.SituPesquisa;
import excecoes.ExcecaoRegraNegocio;
import servicos.administracao.Autenticacao;
import servicos.administracao.ParametroServico;
import servicos.pesquisas.PesquisaService;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class ColetaServico {
    private final RepositorioColeta repositorio;
    private final PesquisaService pesquisas;
    private final ParametroServico parametros;
    private final Autenticacao autenticacao = Autenticacao.getInstancia();

    public ColetaServico(RepositorioColeta repositorio, PesquisaService pesquisas, ParametroServico parametros) {
        this.repositorio = repositorio;
        this.pesquisas = pesquisas;
        this.parametros = parametros;
    }

    // ---------- Entrevistadores ----------

    public Entrevistador cadastrarEntrevistador(String codigo, String nome) {
        autenticacao.exigirPerfil(Perfil.PESQUISADOR);
        Entrevistador novo = new Entrevistador(codigo, nome);   // valida os campos
        if (repositorio.buscarEntrevistador(novo.getCodigo()).isPresent()) {
            throw new ExcecaoRegraNegocio("Já existe um entrevistador com o código " + novo.getCodigo() + ".");
        }
        repositorio.salvarEntrevistador(novo);
        return novo;
    }

    public List<Entrevistador> listarEntrevistadores() {
        autenticacao.exigirPerfil(Perfil.PESQUISADOR);
        return repositorio.listarEntrevistadores();
    }

    public void removerEntrevistador(String codigo) {
        autenticacao.exigirPerfil(Perfil.PESQUISADOR);
        boolean emUso = pesquisas.listarPesquisas().stream()
                .flatMap(p -> repositorio.listarEntrevistas(p.getCodigo()).stream())
                .anyMatch(e -> e.getEntrevistador().getCodigo().equalsIgnoreCase(codigo));
        if (emUso) {
            throw new ExcecaoRegraNegocio("O entrevistador possui entrevistas e não pode ser excluído.");
        }
        repositorio.removerEntrevistador(codigo);
    }

    // ---------- Participante (características precisam ser parâmetros cadastrados) ----------

    public Participante criarParticipante(String codigo, Genero genero, FaixaEtaria faixa,
                                          Escolaridade escolaridade, Regiao regiao, FaixaRenda renda) {
        autenticacao.exigirPerfil(Perfil.PESQUISADOR);
        exigirCadastrado(faixa, CategoriaParametro.FAIXA_ETARIA);
        exigirCadastrado(escolaridade, CategoriaParametro.ESCOLARIDADE);
        exigirCadastrado(regiao, CategoriaParametro.REGIAO);
        exigirCadastrado(renda, CategoriaParametro.FAIXA_RENDA);
        return new Participante(codigo, genero, faixa, escolaridade, regiao, renda);
    }

    private void exigirCadastrado(Parametro p, CategoriaParametro categoria) {
        if (p == null || !parametros.listarPorCategoria(categoria).contains(p)) {
            throw new ExcecaoRegraNegocio("A característica informada (" + p
                    + ") não está cadastrada nos parâmetros gerais.");
        }
    }

    // ---------- Entrevistas ----------

    public Entrevista iniciarEntrevista(String pesquisaCodigo, String id, Participante participante,
                                        String codigoEntrevistador) {
        autenticacao.exigirPerfil(Perfil.PESQUISADOR);
        Pesquisa pesquisa = pesquisas.buscarPesquisaPorCodigo(pesquisaCodigo);   // vínculo com pesquisa existente

        if (pesquisa.getSituacao() != SituPesquisa.EM_COLETA) {
            throw new ExcecaoRegraNegocio("A pesquisa " + pesquisa.getCodigo() + " está '"
                    + pesquisa.getSituacao() + "' e não recebe novas entrevistas.");
        }
        if (repositorio.buscarEntrevista(pesquisa.getCodigo(), id == null ? "" : id).isPresent()) {
            throw new ExcecaoRegraNegocio("Já existe a entrevista " + id + " nesta pesquisa.");
        }
        if (repositorio.listarEntrevistas(pesquisa.getCodigo()).size() >= pesquisa.getQtdPrevista()) {
            throw new ExcecaoRegraNegocio("A quantidade prevista de entrevistas ("
                    + pesquisa.getQtdPrevista() + ") já foi atingida.");
        }
        Entrevistador entrevistador = repositorio.buscarEntrevistador(codigoEntrevistador)
                .orElseThrow(() -> new ExcecaoRegraNegocio("Entrevistador não encontrado."));
        if (participanteJaEntrevistado(pesquisa.getCodigo(), participante)) {
            throw new ExcecaoRegraNegocio("O participante já foi entrevistado nesta pesquisa.");
        }

        Entrevista nova = new Entrevista(id, pesquisa.getCodigo(), participante, entrevistador, LocalDate.now());
        repositorio.salvarEntrevista(nova);
        return nova;
    }

    private boolean participanteJaEntrevistado(String pesquisaCodigo, Participante p) {
        return p != null && repositorio.listarEntrevistas(pesquisaCodigo).stream()
                .anyMatch(e -> e.getParticipante().getCodigo().equals(p.getCodigo()));
    }

    public void responder(String pesquisaCodigo, String entrevistaId, int indicePergunta, String textoOpcao) {
        autenticacao.exigirPerfil(Perfil.PESQUISADOR);
        Entrevista e = buscarAberta(pesquisaCodigo, entrevistaId);
        Pergunta pergunta = pergunta(pesquisaCodigo, indicePergunta);
        OpcaoResposta opcao = new OpcaoResposta(textoOpcao);
        e.adicionarResposta(Resposta.daOpcao(pergunta, opcao));
        repositorio.salvarEntrevista(e);
    }

    public void responderEspecial(String pesquisaCodigo, String entrevistaId, int indicePergunta, TipoEspecial tipo) {
        autenticacao.exigirPerfil(Perfil.PESQUISADOR);
        Entrevista e = buscarAberta(pesquisaCodigo, entrevistaId);
        e.adicionarResposta(Resposta.especial(pergunta(pesquisaCodigo, indicePergunta), tipo));
        repositorio.salvarEntrevista(e);
    }

    public void concluirEntrevista(String pesquisaCodigo, String entrevistaId) {
        autenticacao.exigirPerfil(Perfil.PESQUISADOR);
        Entrevista e = buscarAberta(pesquisaCodigo, entrevistaId);
        e.concluir(pesquisas.buscarPesquisaPorCodigo(pesquisaCodigo).getPerguntas());
        repositorio.salvarEntrevista(e);
    }

    // ---------- Consultas ----------

    public List<Entrevista> listarEntrevistas(String pesquisaCodigo) {
        autenticacao.exigirAutenticacao();
        return repositorio.listarEntrevistas(pesquisaCodigo);
    }

    /** Filtra por características da amostra; qualquer critério pode ser null. */
    public List<Entrevista> filtrar(String pesquisaCodigo, Regiao regiao, FaixaEtaria faixa,
                                    Escolaridade escolaridade, FaixaRenda renda, Genero genero) {
        autenticacao.exigirAutenticacao();
        return repositorio.listarEntrevistas(pesquisaCodigo).stream()
                .filter(e -> regiao == null || e.getParticipante().getRegiao().equals(regiao))
                .filter(e -> faixa == null || e.getParticipante().getFaixaEtaria().equals(faixa))
                .filter(e -> escolaridade == null || e.getParticipante().getEscolaridade().equals(escolaridade))
                .filter(e -> renda == null || e.getParticipante().getFaixaRenda().equals(renda))
                .filter(e -> genero == null || e.getParticipante().getGenero() == genero)
                .collect(Collectors.toList());
    }

    /** Entrevistas concluídas por grupo (descrição da característica), usado nas metas do plano amostral. */
    public Map<String, Integer> contarPorGrupo(String pesquisaCodigo) {
        autenticacao.exigirAutenticacao();
        Map<String, Integer> grupos = new LinkedHashMap<>();
        for (Entrevista e : repositorio.listarEntrevistas(pesquisaCodigo)) {
            if (!e.isConcluida()) continue;
            Participante p = e.getParticipante();
            for (String g : List.of(p.getRegiao().getDescricao(), p.getFaixaEtaria().getDescricao(),
                    p.getEscolaridade().getDescricao(), p.getFaixaRenda().getDescricao(),
                    p.getGenero().getDescricao())) {
                grupos.merge(g, 1, Integer::sum);
            }
        }
        return grupos;
    }

    public RelatorioAcompanhamento acompanhar(String pesquisaCodigo) {
        autenticacao.exigirAutenticacao();
        int realizadas = (int) repositorio.listarEntrevistas(pesquisaCodigo).stream()
                .filter(Entrevista::isConcluida).count();
        return pesquisas.acompanharPesquisa(pesquisaCodigo, realizadas, contarPorGrupo(pesquisaCodigo));
    }

    /** Usado na Main: impede excluir parâmetro usado por algum participante. */
    public boolean parametroEmUso(Parametro parametro) {
        return pesquisas.listarPesquisas().stream()
                .flatMap(p -> repositorio.listarEntrevistas(p.getCodigo()).stream())
                .map(Entrevista::getParticipante)
                .anyMatch(p -> parametro.equals(p.getRegiao()) || parametro.equals(p.getFaixaEtaria())
                        || parametro.equals(p.getEscolaridade()) || parametro.equals(p.getFaixaRenda()));
    }

    // ---------- auxiliares ----------

    private Entrevista buscarAberta(String pesquisaCodigo, String id) {
        Entrevista e = repositorio.buscarEntrevista(pesquisaCodigo, id)
                .orElseThrow(() -> new ExcecaoRegraNegocio("Entrevista não encontrada."));
        if (e.isConcluida()) {
            throw new ExcecaoRegraNegocio("Entrevista concluída não pode ser alterada.");
        }
        return e;
    }

    private Pergunta pergunta(String pesquisaCodigo, int indice) {
        List<Pergunta> perguntas = pesquisas.buscarPesquisaPorCodigo(pesquisaCodigo).getPerguntas();
        if (indice < 0 || indice >= perguntas.size()) {
            throw new ExcecaoRegraNegocio("Pergunta inexistente: " + (indice + 1) + ".");
        }
        return perguntas.get(indice);
    }
}
