package telas.administracao;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import telas.Tela;

public class UserPara implements Tela {

    private enum Aba { USUARIOS, PARAMETROS, RELATORIO }

    private final BorderPane raiz = new BorderPane();
    private final Button btnUsuarios = new Button("Gestão de Usuários");
    private final Button btnParametros = new Button("Gestão de Parâmetros");
    private final Button btnRelatorio = new Button("Relatório de Usuários");

    @Override
    public Parent getRoot() {
        btnUsuarios.setOnAction(e -> mostrar(Aba.USUARIOS));
        btnParametros.setOnAction(e -> mostrar(Aba.PARAMETROS));
        btnRelatorio.setOnAction(e -> mostrar(Aba.RELATORIO));

        HBox barra = new HBox(10, btnUsuarios, btnParametros, btnRelatorio);
        barra.setPadding(new Insets(15, 30, 0, 30));
        raiz.setTop(barra);

        mostrar(Aba.USUARIOS);
        return raiz;
    }

    private void mostrar(Aba aba) {
        Tela tela;
        switch (aba) {
            case USUARIOS:   tela = new GestaoUser();    break;
            case PARAMETROS: tela = new GestaoPara();    break;
            default:         tela = new RelatorioUser(); break;
        }

        ScrollPane rolagem = new ScrollPane(tela.getRoot());
        rolagem.setFitToWidth(true);
        rolagem.setStyle("-fx-background-color: transparent;");
        raiz.setCenter(rolagem);

        destacar(btnUsuarios, aba == Aba.USUARIOS);
        destacar(btnParametros, aba == Aba.PARAMETROS);
        destacar(btnRelatorio, aba == Aba.RELATORIO);
    }

    private static void destacar(Button b, boolean ativo) {
        if(ativo){
            b.setStyle("-fx-background-color: #475569; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand");
        }else{
            b.setStyle("");
        }
    }
}