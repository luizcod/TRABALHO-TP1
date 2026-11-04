package excecoes;

/**
 * Lançada ao tentar validar ou iniciar coleta de uma pesquisa do tipo 'Intenção de Voto' sem candidatos cadastrados.
 * Regra: "Se a pesquisa for do tipo 'Intenção de Voto', validar se possui ao menos 1 candidato associado".
 */
public class PesquisaSemCandidatoException extends ExcecaoRegraNegocio {
    private static final long serialVersionUID = 1L;

    public PesquisaSemCandidatoException(String codigoPesquisa) {
        super(String.format("A pesquisa '%s' é do tipo 'Intenção de Voto' e deve possuir pelo menos 1 candidato associado antes de prosseguir.", codigoPesquisa));
    }
}
