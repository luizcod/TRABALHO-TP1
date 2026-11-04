package excecoes;

/**
 * Lançada quando um Partido, Candidato ou Pesquisa solicitado não for encontrado no sistema.
 */
public class EntidadeNaoEncontradaException extends ExcecaoRegraNegocio {
    private static final long serialVersionUID = 1L;

    public EntidadeNaoEncontradaException(String mensagem) {
        super(mensagem);
    }
}
