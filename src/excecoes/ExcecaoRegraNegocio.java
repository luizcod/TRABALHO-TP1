package excecoes;

public class ExcecaoRegraNegocio extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ExcecaoRegraNegocio(String mensagem) {
        super(mensagem);
    }

    public ExcecaoRegraNegocio(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}