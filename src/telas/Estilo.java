package telas;

import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;

import java.util.function.Function;

/**
 * Cores, fontes e componentes comuns a todas as telas (JavaFX), para manter o mesmo
 * estilo do protótipo do Figma: paleta neutra, cinza-ardósia.
 * As cores são textos "#RRGGBB" porque são usadas dentro de estilos CSS do JavaFX.
 */
public final class Estilo {
    public static final String FUNDO = "#F3F4F6";
    public static final String ARDOSIA = "#374151";
    public static final String ARDOSIA_BOTAO = "#4B5563";
    public static final String BORDA = "#D1D5DB";
    public static final String TEXTO = "#1F2937";
    public static final String TITULO_CLARO = "#9CA3AF";
    public static final String ERRO = "#C0474B";

    /** Usa Inter se estiver instalada; senão, a próxima fonte sem serifa da lista. */
    private static final String FAMILIA = "'Inter', 'Segoe UI', 'Helvetica Neue', Arial, sans-serif";

    private Estilo() {}

    /** Trecho de CSS com a fonte, o tamanho (px) e o peso. */
    public static String fonte(int tamanho, boolean negrito) {
        return "-fx-font-family: " + FAMILIA + "; -fx-font-size: " + tamanho + "px; -fx-font-weight: "
                + (negrito ? "bold" : "normal") + ";";
    }

    public static Label rotulo(String texto) {
        Label l = new Label(texto);
        l.setStyle(fonte(13, false) + "-fx-text-fill: " + TEXTO + ";");
        return l;
    }

    /** Faixa escura com o título em branco, no topo dos cartões. */
    public static StackPane cabecalhoCartao(String titulo, int tamanho, int altura) {
        Label t = new Label(titulo);
        t.setStyle(fonte(tamanho, true) + "-fx-text-fill: white;");
        StackPane faixa = new StackPane(t);
        faixa.setAlignment(Pos.CENTER);
        faixa.setPrefHeight(altura);
        faixa.setMinHeight(altura);
        faixa.setStyle("-fx-background-color: " + ARDOSIA + ";");
        return faixa;
    }

    public static TextField campoTexto() {
        TextField c = new TextField();
        c.setPrefHeight(36);
        c.setMinHeight(36);
        c.setStyle(fonte(14, false) + "-fx-text-fill: " + TEXTO + "; -fx-background-color: white;"
                + " -fx-border-color: " + BORDA + "; -fx-border-radius: 4; -fx-background-radius: 4;"
                + " -fx-padding: 0 12 0 12;");
        return c;
    }

    /**
     * Lista suspensa no mesmo estilo dos campos de texto. Enquanto nada estiver escolhido
     * mostra "Selecione..." em cinza. O parâmetro diz como escrever cada item.
     */
    public static <T> ComboBox<T> combo(Function<T, String> texto) {
        ComboBox<T> c = new ComboBox<>();
        c.setMaxWidth(Double.MAX_VALUE);
        c.setPrefHeight(36);
        c.setMinHeight(36);
        c.setStyle(fonte(14, false) + "-fx-background-color: white; -fx-border-color: " + BORDA + ";"
                + " -fx-border-radius: 4; -fx-background-radius: 4;");
        c.setConverter(new StringConverter<T>() {
            @Override
            public String toString(T item) {
                return item == null ? "" : texto.apply(item);
            }

            @Override
            public T fromString(String s) {
                return null;
            }
        });
        c.setCellFactory(lista -> new ListCell<T>() {
            @Override
            protected void updateItem(T item, boolean vazio) {
                super.updateItem(item, vazio);
                setText(vazio || item == null ? null : texto.apply(item));
                setStyle(fonte(14, false));
            }
        });
        c.setPromptText("Selecione...");   // o JavaFX mostra este texto, em cinza, enquanto nada estiver escolhido
        return c;
    }

    /** Deixa a lista sem nada escolhido, mostrando "Selecione..." de novo. */
    public static <T> void limpar(ComboBox<T> c) {
        c.getSelectionModel().clearSelection();
        c.setValue(null);
    }

    public static Button botaoPrincipal(String texto) {
        return botao(texto, ARDOSIA_BOTAO, ARDOSIA, "white", null);
    }

    public static Button botaoSecundario(String texto) {
        return botao(texto, "white", FUNDO, TEXTO, BORDA);
    }

    /** Botão com cantos arredondados e efeito ao passar o mouse. */
    private static Button botao(String texto, String normal, String hover, String corTexto, String borda) {
        Button b = new Button(texto);
        String base = fonte(14, true) + "-fx-text-fill: " + corTexto + "; -fx-background-radius: 8;"
                + " -fx-padding: 0 18 0 18;"
                + (borda != null ? " -fx-border-color: " + borda + "; -fx-border-radius: 8;" : "");
        b.setPrefHeight(38);
        b.setMinHeight(38);
        b.setMinWidth(javafx.scene.layout.Region.USE_PREF_SIZE);
        b.setCursor(Cursor.HAND);
        b.setStyle(base + " -fx-background-color: " + normal + ";");
        b.setOnMouseEntered(e -> b.setStyle(base + " -fx-background-color: " + hover + ";"));
        b.setOnMouseExited(e -> b.setStyle(base + " -fx-background-color: " + normal + ";"));
        return b;
    }
}