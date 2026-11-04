package excecoes;

import dominio.candidatospartidos.Cargo;

/**
 * Lançada ao tentar associar um candidato a uma pesquisa cujo cargo é diferente do cargo do candidato.
 * Regra: "Um candidato somente poderá ser associado a uma pesquisa quando seu cargo for compatível com o cargo definido para a pesquisa".
 */
public class CargoIncompativelException extends ExcecaoRegraNegocio {
    private static final long serialVersionUID = 1L;

    public CargoIncompativelException(String nomeCandidato, Cargo cargoCandidato, Cargo cargoPesquisa) {
        super(String.format("Cargo incompatível: o candidato '%s' concorre ao cargo de %s, mas a pesquisa é destinada ao cargo de %s.",
                nomeCandidato, cargoCandidato, cargoPesquisa));
    }
}
