package dominio.analiseEstatistica;

import excecoes.ExcecaoRegraNegocio;
/** Caso de uso "Verificar Compatibilidade" (incluído em "Comparar Pesquisas"). */
public final class VerificadorCompatibilidade {

    private VerificadorCompatibilidade() {
    }

    public static void verificar(PesquisaResumo a, PesquisaResumo b, Resultado ra, Resultado rb) {
        if (a.codigo().equalsIgnoreCase(b.codigo())) {
            throw new ExcecaoRegraNegocio("Escolha duas pesquisas diferentes para comparar.");
        }
        if (a.situacao() != PesquisaResumo.Situacao.PROCESSADA
                || b.situacao() != PesquisaResumo.Situacao.PROCESSADA) {
            throw new ExcecaoRegraNegocio("Só é possível comparar pesquisas já processadas.");
        }
        if (!a.cargo().equalsIgnoreCase(b.cargo())) {
            throw new ExcecaoRegraNegocio("Pesquisas incompatíveis: cargos diferentes ("
                    + a.cargo() + " x " + b.cargo() + ").");
        }
        if (!a.tipoPesquisa().equalsIgnoreCase(b.tipoPesquisa())) {
            throw new ExcecaoRegraNegocio("Pesquisas incompatíveis: tipos diferentes ("
                    + a.tipoPesquisa() + " x " + b.tipoPesquisa() + ").");
        }
        if (!ra.perguntaId().equalsIgnoreCase(rb.perguntaId())) {
            throw new ExcecaoRegraNegocio("Pesquisas incompatíveis: os resultados são de perguntas diferentes.");
        }
    }
}
