package excecoes;

/**
 * Lançada ao tentar associar o mesmo candidato duas vezes a uma pesquisa.
 * Regra: "Um candidato não pode ser associado duas vezes à mesma pesquisa".
 */
public class CandidatoJaAssociadoException extends ExcecaoRegraNegocio {
    private static final long serialVersionUID = 1L;

    public CandidatoJaAssociadoException(String nomeCandidato, String codigoPesquisa) {
        super(String.format("O candidato '%s' já está associado à pesquisa '%s'.", nomeCandidato, codigoPesquisa));
    }
}
