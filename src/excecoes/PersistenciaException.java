package excecoes;

/**
 * Exceção lançada em caso de falhas na camada de persistência em arquivos (leitura, escrita ou serialização).
 */
public class PersistenciaException extends ExcecaoRegraNegocio {
    private static final long serialVersionUID = 1L;

    public PersistenciaException(String mensagem) {
        super(mensagem);
    }

    public PersistenciaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
