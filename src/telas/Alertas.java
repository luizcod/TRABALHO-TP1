package telas;

import excecoes.ExcecaoRegraNegocio;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public final class Alertas {
    private Alertas() {}

    public static void erro(String mensagem) {
        new Alert(Alert.AlertType.WARNING, mensagem, ButtonType.OK).showAndWait();
    }

    public static void info(String mensagem) {
        new Alert(Alert.AlertType.INFORMATION, mensagem, ButtonType.OK).showAndWait();
    }

    /** Confirmação antes de exclusão definitiva (requisito de usabilidade). */
    public static boolean confirmar(String mensagem) {
        return new Alert(Alert.AlertType.CONFIRMATION, mensagem, ButtonType.YES, ButtonType.NO)
                .showAndWait().filter(b -> b == ButtonType.YES).isPresent();
    }

    /** Executa a ação e mostra a mensagem de qualquer ExcecaoRegraNegocio. */
    public static void executar(Runnable acao) {
        try {
            acao.run();
        } catch (ExcecaoRegraNegocio e) {
            erro(e.getMessage());
        }
    }
}