package telas.administracao;

import dominio.administracao.Perfil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import servicos.administracao.Autenticacao;
import telas.App;
import telas.Alertas;
import telas.Tela;

public class Login implements Tela {
    private final TextField campoEmail = new TextField();
    private final PasswordField campoSenha = new PasswordField();

    @Override
    public Parent getRoot() {
        Label titulo = new Label("Sistema de Análise de Pesquisas Eleitorais");
        titulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #FFFFFF; -fx-background-color: #1F2937; -fx-padding: 30px");
        titulo.setAlignment(Pos.CENTER);
        titulo.setMaxWidth(Double.MAX_VALUE);

        Label nomeLogin = new Label("Login");
        nomeLogin.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-padding: 30px");
        nomeLogin.setAlignment(Pos.CENTER);

        GridPane form = new GridPane();
        form.setHgap(20);
        form.setVgap(20);
        Label email = new Label("E-mail");
        Label senha = new Label("Senha");
        campoEmail.setStyle("-fx-pref-width: 300px; -fx-padding: 6px;");
        campoSenha.setStyle("-fx-pref-width: 300px; -fx-padding: 6px;");
        form.addRow(0, email, campoEmail);
        form.addRow(1, senha, campoSenha);
        form.setAlignment(Pos.CENTER);

        Button entrar = new Button("Entrar");
        entrar.setDefaultButton(true);                 // Enter aciona o botão
        entrar.setOnAction(e -> Alertas.executar(this::entrar));
        entrar.setStyle("-fx-font-size: 14px; -fx-font-weight: 800; -fx-text-fill: #FFFFFF; -fx-background-color: #1F2937; -fx-padding: 10");
        entrar.setOnMouseEntered(e -> entrar.setStyle("-fx-cursor: hand; -fx-font-size: 14px; -fx-font-weight: 800; -fx-text-fill: #FFFFFF; -fx-background-color: #1F2937; -fx-padding: 10"));
        VBox.setMargin(entrar, new Insets(30, 0, 0, 0));

        BorderPane raiz = new BorderPane();
        VBox topo = new VBox(10,titulo,nomeLogin, form, entrar);
        raiz.setTop(topo);
        topo.setAlignment(Pos.CENTER);
        return raiz;
    }

    private void entrar() {
        Autenticacao.getInstancia().login(campoEmail.getText(), campoSenha.getText());
        App.mostrarPrincipal();
    }
}