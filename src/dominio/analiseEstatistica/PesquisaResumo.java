package dominio.analiseEstatistica;

/**
 * Visão somente leitura de uma pesquisa, como o módulo de Análise a enxerga.
 * A classe real Pesquisa pertence ao módulo de Pesquisas; este resumo é preenchido pelo PesquisaGateway.
 */
public record PesquisaResumo(String codigo, String nome, String cargo, String tipoPesquisa,
                             Situacao situacao, int qtdPrevista) {

    /** Espelha o enum SituPesquisa do módulo de Pesquisas. */
    public enum Situacao { PLANEJADA, EM_COLETA, ENCERRADA, PROCESSADA }

    public PesquisaResumo comSituacao(Situacao nova) {
        return new PesquisaResumo(codigo, nome, cargo, tipoPesquisa, nova, qtdPrevista);
    }
}
