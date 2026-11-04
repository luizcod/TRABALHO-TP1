package excecoes;

/**
 * Lançada ao tentar associar a uma pesquisa um candidato com candidatura inativa,
 * indeferida ou não validada.
 * Regra: "Somente candidaturas em situação permitida para participação em pesquisa poderão ser associadas a uma pesquisa".
 */
public class CandidaturaInvalidaException extends ExcecaoRegraNegocio {
    private static final long serialVersionUID = 1L;

    public CandidaturaInvalidaException(String nomeCandidato, String situacao) {
        super(String.format("A candidatura do candidato '%s' está na situação '%s', portanto não é permitida sua participação em pesquisas eleitorais.",
                nomeCandidato, situacao));
    }
}
