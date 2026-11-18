package telas;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.util.function.Consumer;

/**
 * Barra de navegação do topo (a mesma em todas as telas): Início, Pesquisas, Coleta,
 * Análise, Candidatos e "Sair" à direita. O item da seção atual fica destacado.
 * Ao clicar, avisa quem abriu a tela com o nome do item ("Início", "Coleta", ..., "Sair").
 */
public class BarraNavegacao extends HBox {
    public static final String[] ITENS = {"Início", "Pesquisas", "Coleta", "Análise", "Candidatos"};

    public BarraNavegacao(String secaoAtual, Consumer<String> aoNavegar) {
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(8);
        setPadding(new Insets(0, 16, 0, 16));
        setPrefHeight(56);
        setMinHeight(56);
        setStyle("-fx-background-color: " + Estilo.ARDOSIA + ";");

        for (String item : ITENS) {
            getChildren().add(criarItem(item, item.equals(secaoAtual), aoNavegar));
        }
        Region espaco = new Region();
        HBox.setHgrow(espaco, Priority.ALWAYS);
        getChildren().add(espaco);
        getChildren().add(criarItem("Sair", false, aoNavegar));
    }

    /** Botão pequeno com borda fina; preenchido quando é a seção atual ou o mouse está em cima. */
    private static Button criarItem(String texto, boolean ativo, Consumer<String> aoNavegar) {
        Button b = new Button(texto);
        String base = Estilo.fonte(13, false) + "-fx-text-fill: white; -fx-background-radius: 6;"
                + " -fx-border-color: #6B7280; -fx-border-radius: 6; -fx-padding: 4 14 4 14;";
        String normal = base + " -fx-background-color: " + (ativo ? Estilo.ARDOSIA_BOTAO : "transparent") + ";";
        String hover = base + " -fx-background-color: " + Estilo.ARDOSIA_BOTAO + ";";
        b.setStyle(normal);
        b.setCursor(Cursor.HAND);
        b.setFocusTraversable(false);
        b.setOnMouseEntered(e -> b.setStyle(hover));
        b.setOnMouseExited(e -> b.setStyle(normal));
        b.setOnAction(e -> aoNavegar.accept(texto));
        return b;
    }
}