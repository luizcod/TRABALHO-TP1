package integracao.analiseEstatistica;

import dominio.analiseEstatistica.PesquisaResumo;
import dominio.analiseEstatistica.PesquisaResumo.Situacao;
import dominio.pesquisas.Pesquisa;
import dominio.pesquisas.SituPesquisa;
import excecoes.EntidadeNaoEncontradaException;
import servicos.pesquisas.PesquisaService;

import java.util.List;
import java.util.Optional;

/** Liga a Análise ao módulo de Pesquisas real. */
public class PesquisaGatewayReal implements PesquisaGateway {
    private final PesquisaService pesquisas;

    public PesquisaGatewayReal(PesquisaService pesquisas) {
        this.pesquisas = pesquisas;
    }

    @Override
    public Optional<PesquisaResumo> buscar(String codigo) {
        try {
            return Optional.of(resumir(pesquisas.buscarPesquisaPorCodigo(codigo)));
        } catch (EntidadeNaoEncontradaException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<PesquisaResumo> listar() {
        return pesquisas.listarPesquisas().stream().map(PesquisaGatewayReal::resumir).toList();
    }

    @Override
    public void marcarComoProcessada(String codigo) {
        pesquisas.alterarSituacao(codigo, SituPesquisa.PROCESSADA);   // reprocessar não dá erro
    }

    private static PesquisaResumo resumir(Pesquisa p) {
        return new PesquisaResumo(p.getCodigo(), p.getNome(), p.getCargo().name(),
                p.getTipoPesquisa().getDescricao(), Situacao.valueOf(p.getSituacao().name()),
                p.getQtdPrevista());
    }
}