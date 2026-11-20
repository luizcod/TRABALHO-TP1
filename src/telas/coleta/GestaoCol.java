package telas.coleta;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import telas.Tela;

import java.util.function.Supplier;

/**
 * Seção "Coleta" do menu principal. Reúne as três telas do módulo em abas:
 * Registrar Entrevista, Acompanhar Pesquisas e Entrevistadores.
 * A tela de cada aba é criada de novo sempre que a aba é aberta, para mostrar dados atualizados
 * (por exemplo, um entrevistador recém-cadastrado já aparece em Registrar Entrevista).
 */

public class GestaoCol implements Tela {
    private final BorderPane raiz = new BorderPane();
    private final Button btnRegistrar = new Button("Registrar Entrevista");
    private final Button btnAcompanhar = new Button("Acompanhar Pesquisas");
    private final Button btnEntrevistadores = new Button("Entrevistadores");

    @Override
    public Parent getRoot() {
        btnRegistrar.setOnAction(e -> mostrar(btnRegistrar, TelaRegistrarEntrevista::new));
        btnAcompanhar.setOnAction(e -> mostrar(btnAcompanhar, TelaAcompanharPesquisas::new));
        btnEntrevistadores.setOnAction(e -> mostrar(btnEntrevistadores, TelaEntrevistadores::new));

        HBox barra = new HBox(10, btnRegistrar, btnAcompanhar, btnEntrevistadores);
        barra.setPadding(new Insets(15, 30, 10, 30));
        raiz.setTop(barra);

        mostrar(btnRegistrar, TelaRegistrarEntrevista::new);   // tela inicial da seção
        return raiz;
    }

    private void mostrar(Button ativo, Supplier<Tela> fabrica) {
        raiz.setCenter(fabrica.get().getRoot());   // as telas já têm o próprio ScrollPane
        for (Button b : new Button[]{btnRegistrar, btnAcompanhar, btnEntrevistadores}) {
            destacar(b, b == ativo);
        }
    }

    private static void destacar(Button b, boolean ativo) {
        if (ativo) {
            b.setStyle("-fx-background-color: #475569; -fx-text-fill: white; -fx-font-weight: bold;");
        } else {
            b.setStyle("");
        }
    }
}