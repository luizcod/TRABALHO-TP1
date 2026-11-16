package telas;

import dominio.administracao.Perfil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import servicos.administracao.Autenticacao;
import telas.administracao.GestaoPara;
import telas.administracao.GestaoUser;
import telas.administracao.Login;
import telas.administracao.UserPara;
import telas.analise.GestaoAnalise;
import telas.coleta.GestaoCol;
import telas.pesquisa.GestaoPesq;

import java.util.function.Supplier;

public class JanelaPrincipal implements Tela {
    private final BorderPane raiz = new BorderPane();
    private final HBox menu = new HBox(6);
    private final Autenticacao auth = Autenticacao.getInstancia();

    @Override
    public Parent getRoot() {
        montarMenu();

        Region espaco = new Region();
        HBox.setHgrow(espaco, Priority.ALWAYS);

        Label usuario = new Label("Usuário: " + auth.getUsuarioLogado().getNome());
        usuario.setStyle("-fx-text-background-color: #FFFFFF");
        Button sair = new Button("Sair");
        sair.setOnAction(e -> { auth.logout(); App.mostrarLogin();});

        HBox barra = new HBox(10, menu, espaco, usuario, sair);
        barra.setPadding(new Insets(10));
        barra.setAlignment(Pos.CENTER_LEFT);
        barra.setStyle("-fx-background-color: #374151;");

        raiz.setTop(barra);
        raiz.setCenter(new Inicio().getRoot());
        return raiz;
    }

    //Cada seção só aparece para quem tem o perfil
    private void montarMenu() {
        Button inicio = new Button("Início");

        inicio.setMaxWidth(Double.MAX_VALUE);


        inicio.setOnAction(e -> raiz.setCenter(new Inicio().getRoot()));
        menu.getChildren().add(inicio);
        item("Administração", Perfil.ADMINISTRADOR, UserPara::new);
        item("Pesquisas", Perfil.PESQUISADOR, GestaoPesq::new);
        item("Coleta", Perfil.PESQUISADOR, GestaoCol::new);
        item("Análise", Perfil.ANALISTA, GestaoAnalise::new);
    }

    private void secao(String titulo, Perfil exigido) {
        if (!auth.temPermissao(exigido)) return;
        Label l = new Label(titulo);
        l.setStyle("-fx-font-weight: bold; -fx-padding: 10 0 2 0;");
        menu.getChildren().add(l);
    }

    //cria botao no menu que exige permissao
    private void item(String titulo, Perfil exigido, Supplier<Tela> fabrica) {
        if (!auth.temPermissao(exigido)) return;
        Button b = new Button(titulo);
        b.setMaxWidth(Double.MAX_VALUE);
        b.setOnAction(e -> raiz.setCenter(fabrica.get().getRoot()));
        menu.getChildren().add(b);
    }

}

