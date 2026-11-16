package telas;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import telas.administracao.GestaoPara;
import telas.administracao.GestaoUser;
import telas.administracao.Login;

public class App extends Application {
    private static Stage palco;

    @Override
    public void start(Stage p) {
        palco = p;
        palco.setTitle("Sistema de Análise de Pesquisas Eleitorais");
        mostrarLogin();
        palco.show();
    }

    public static void mostrarLogin() {
        trocar(new Login().getRoot(), 600, 460);
    }

    public static void mostrarPrincipal() {
        trocar(new JanelaPrincipal().getRoot(), 1000, 650);
    }

    private static void trocar(Parent raiz, double largura, double altura) {
        palco.setScene(new Scene(raiz, largura, altura));
        palco.centerOnScreen();
    }
}