package excecoes;

import dominio.pesquisas.SituPesquisa;

/**
 * Lançada ao tentar alterar ou editar os dados de uma pesquisa que esteja com status ENCERRADA ou PROCESSADA.
 * Regra: "Se a SituPesquisa estiver ENCERRADA ou PROCESSADA, bloquear edições/alterações lançando exceção".
 */
public class PesquisaBloqueadaException extends ExcecaoRegraNegocio {
    private static final long serialVersionUID = 1L;

    public PesquisaBloqueadaException(String codigoPesquisa, SituPesquisa situacao) {
        super(String.format("A pesquisa '%s' está na situação '%s' e seus dados estão bloqueados para alteração/edição.",
                codigoPesquisa, situacao));
    }
}
