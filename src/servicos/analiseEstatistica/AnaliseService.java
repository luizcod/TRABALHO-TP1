package servicos.analiseEstatistica;

import dominio.administracao.Perfil;
import servicos.administracao.Autenticacao;
import excecoes.ExcecaoRegraNegocio;

import dominio.analiseEstatistica.Analise;
import dominio.analiseEstatistica.ComparacaoPesquisa;
import dominio.analiseEstatistica.FabricaRelatorio;
import dominio.analiseEstatistica.FiltroRelatorio;
import dominio.analiseEstatistica.PesquisaResumo;
import dominio.analiseEstatistica.PesquisaResumo.Situacao;
import dominio.analiseEstatistica.Relatorio;
import dominio.analiseEstatistica.RespostaAnalise;
import dominio.analiseEstatistica.Resultado;
import persistencia.analiseEstatistica.ExportadorFactory;
import persistencia.analiseEstatistica.FormatoExportacao;
import integracao.analiseEstatistica.ColetaGateway;
import integracao.analiseEstatistica.PesquisaGateway;
import persistencia.analiseEstatistica.ArquivoRepositorioResultados;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Camada de aplicação do módulo de Análise Estatística: orquestra os casos de uso
 * Processar Pesquisa, Gerar Relatórios e Gráficos (com exportação) e Comparar Pesquisas.
 * Deve ser chamada somente por usuários com perfil ANALISTA ou ADMINISTRADOR (checagem na camada de apresentação/autenticação).
 */
public class AnaliseService {

    private final PesquisaGateway pesquisas;
    private final ColetaGateway coleta;
    private final ArquivoRepositorioResultados repositorio;
    private final Autenticacao autenticacao = Autenticacao.getInstancia();
    private final Analise analise = new Analise();

    public AnaliseService(PesquisaGateway pesquisas, ColetaGateway coleta, ArquivoRepositorioResultados repositorio) {
        this.pesquisas = pesquisas;
        this.coleta = coleta;
        this.repositorio = repositorio;
    }

    public List<PesquisaResumo> listarPesquisas() {
        autenticacao.exigirPerfil(Perfil.ANALISTA);
        return pesquisas.listar();
    }

    /** Processar Pesquisa: só após o encerramento da coleta; reprocessar é permitido. */
    public Resultado processarPesquisa(String codigo, String perguntaId) {
        autenticacao.exigirPerfil(Perfil.ANALISTA);
        PesquisaResumo p = buscarOuFalhar(codigo);
        if (p.situacao() != Situacao.ENCERRADA && p.situacao() != Situacao.PROCESSADA) {
            throw new ExcecaoRegraNegocio("A pesquisa " + p.codigo() + " está '" + p.situacao()
                    + "'. Uma pesquisa só pode ser processada após o encerramento da coleta.");
        }
        Resultado r = analise.processar(p.codigo(), perguntaId, coleta.respostas(p.codigo(), perguntaId));
        repositorio.salvar(r);
        pesquisas.marcarComoProcessada(p.codigo());
        return r;
    }

    /** Gerar Relatórios e Gráficos: exige pesquisa processada; aceita filtros e cruzamentos. */
    public Relatorio gerarRelatorio(String codigo, String perguntaId, FiltroRelatorio filtro,
                                    boolean incluirCruzamentos) {
        autenticacao.exigirPerfil(Perfil.ANALISTA);
        PesquisaResumo p = exigirProcessada(codigo);
        FiltroRelatorio f = filtro == null ? FiltroRelatorio.semFiltros() : filtro;
        Resultado r;
        if (f.vazio()) {
            r = repositorio.buscar(p.codigo(), perguntaId).orElseGet(
                    () -> analise.processar(p.codigo(), perguntaId, coleta.respostas(p.codigo(), perguntaId)));
        } else {
            // A amostra mantém todos os candidatos; o destaque do candidato é feito na FabricaRelatorio.
            List<RespostaAnalise> amostra = coleta.respostas(p.codigo(), perguntaId).stream()
                    .filter(f::aceita).toList();
            if (amostra.isEmpty()) {
                throw new ExcecaoRegraNegocio("Nenhuma resposta atende aos filtros: " + f.descricao());
            }
            r = analise.processar(p.codigo(), perguntaId, amostra);
        }
        return FabricaRelatorio.deResultado(p, f, r, incluirCruzamentos);
    }

    /** Comparar Pesquisas (inclui Verificar Compatibilidade, executada no construtor de ComparacaoPesquisa). */
    public Relatorio compararPesquisas(String codigoA, String perguntaA, String codigoB, String perguntaB) {
        autenticacao.exigirPerfil(Perfil.ANALISTA);
        PesquisaResumo a = buscarOuFalhar(codigoA);
        PesquisaResumo b = buscarOuFalhar(codigoB);
        Resultado ra = resultadoProcessado(a, perguntaA);
        Resultado rb = resultadoProcessado(b, perguntaB);
        return FabricaRelatorio.deComparacao(new ComparacaoPesquisa(a, ra, b, rb));
    }

    /** Exportar PDF ou CSV (ou exibir em tela). */
    public void exportar(Relatorio relatorio, FormatoExportacao formato, Path destino) {
        autenticacao.exigirPerfil(Perfil.ANALISTA);
        try {
            ExportadorFactory.criar(formato).exportar(relatorio, destino);
        } catch (IOException e) {
            throw new ExcecaoRegraNegocio("Falha ao exportar o relatório: " + e.getMessage(), e);
        }
    }

    private Resultado resultadoProcessado(PesquisaResumo p, String perguntaId) {
        if (p.situacao() != Situacao.PROCESSADA) {
            throw new ExcecaoRegraNegocio("A pesquisa " + p.codigo() + " ainda não foi processada.");
        }
        return repositorio.buscar(p.codigo(), perguntaId).orElseThrow(() -> new ExcecaoRegraNegocio(
                "Não há resultado salvo para a pesquisa " + p.codigo() + " (pergunta " + perguntaId
                        + "). Processe a pesquisa novamente."));
    }

    private PesquisaResumo exigirProcessada(String codigo) {
        PesquisaResumo p = buscarOuFalhar(codigo);
        if (p.situacao() != Situacao.PROCESSADA) {
            throw new ExcecaoRegraNegocio("A pesquisa " + p.codigo()
                    + " ainda não foi processada. Processe-a antes de gerar relatórios.");
        }
        return p;
    }

    private PesquisaResumo buscarOuFalhar(String codigo) {
        return pesquisas.buscar(codigo).orElseThrow(
                () -> new ExcecaoRegraNegocio("Pesquisa não encontrada: " + codigo));
    }
}
