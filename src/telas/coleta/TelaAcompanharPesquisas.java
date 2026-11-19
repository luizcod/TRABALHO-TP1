package telas.coleta;

import telas.Contexto;
import telas.Estilo;
import telas.Tela;
import dominio.pesquisas.MetaAmostral;
import dominio.pesquisas.Pesquisa;
import dominio.pesquisas.PlanoAmostral;
import dominio.pesquisas.RelatorioAcompanhamento;
import excecoes.ExcecaoRegraNegocio;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import servicos.coleta.ColetaServico;
import servicos.pesquisas.PesquisaService;

import java.util.Map;

/**
 * Tela "Acompanhar Pesquisas" (Pesquisador), em JavaFX. Mostra a quantidade prevista, a
 * realizada e o percentual de conclusão da pesquisa escolhida, e as metas do plano amostral
 * por grupo, com destaque para as que ainda estão abaixo do mínimo. Os números vêm de
 * {@link ColetaServico#acompanhar(String)}; a tela só os apresenta.
 */
public class TelaAcompanharPesquisas implements Tela {
    private static final double LARGURA = 860;
    private static final String AVERMELHADO = "#FDF0F0";

    private final ColetaServico coleta;
    private final PesquisaService pesquisas;

    private final BorderPane raiz = new BorderPane();
    private final ComboBox<Pesquisa> comboPesquisa = Estilo.combo(p -> p.getCodigo() + " – " + p.getNome());
    private final Label valorPrevista = valorGrande();
    private final Label valorRealizada = valorGrande();
    private final Label valorPercentual = valorGrande();
    private final Barra barraConclusao = new Barra();
    private final GridPane tabela = new GridPane();
    private final Label mensagem = new Label(" ");

    /** Usa os serviços compartilhados em {@link Contexto}, preenchidos pela Main. */
    public TelaAcompanharPesquisas() {
        this.coleta = Contexto.coleta;
        this.pesquisas = Contexto.pesquisas;

        raiz.setStyle("-fx-background-color: " + Estilo.FUNDO + ";");
        raiz.setCenter(montarArea());

        carregarPesquisas();
        comboPesquisa.valueProperty().addListener((obs, antigo, novo) -> atualizar());
        atualizar();
    }

    @Override
    public Parent getRoot() {
        return raiz;
    }

    // =====================================================================
    // Layout
    // =====================================================================

    private ScrollPane montarArea() {
        HBox fundo = new HBox(montarColuna());
        fundo.setAlignment(Pos.TOP_CENTER);
        fundo.setPadding(new Insets(28));
        fundo.setStyle("-fx-background-color: " + Estilo.FUNDO + ";");

        ScrollPane rolagem = new ScrollPane(fundo);
        rolagem.setFitToWidth(true);
        rolagem.setFitToHeight(true);
        rolagem.setStyle("-fx-background: " + Estilo.FUNDO + "; -fx-background-color: " + Estilo.FUNDO + ";"
                + " -fx-border-color: transparent;");
        return rolagem;
    }

    private VBox montarColuna() {
        Label titulo = new Label("Acompanhar Pesquisas");
        titulo.setStyle(Estilo.fonte(22, true) + "-fx-text-fill: " + Estilo.TITULO_CLARO + ";");

        mensagem.setStyle(Estilo.fonte(13, false));
        mensagem.setTextFill(Color.web(Estilo.ERRO));

        VBox coluna = new VBox(18, titulo, montarSeletor(), montarResumo(), montarCartaoTabela(), mensagem);
        coluna.setPrefWidth(LARGURA);
        coluna.setMaxWidth(LARGURA);
        return coluna;
    }

    private HBox montarSeletor() {
        Label rotulo = Estilo.rotulo("Pesquisa");
        rotulo.setMinWidth(58);

        Button atualizar = Estilo.botaoSecundario("Atualizar");
        atualizar.setPrefWidth(110);
        atualizar.setPrefHeight(36);
        atualizar.setOnAction(e -> atualizar());

        HBox linha = new HBox(12, rotulo, comboPesquisa, atualizar);
        linha.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(comboPesquisa, Priority.ALWAYS);
        return linha;
    }

    private HBox montarResumo() {
        HBox resumo = new HBox(16,
                cartaoResumo("Quantidade prevista", valorPrevista, null),
                cartaoResumo("Quantidade realizada", valorRealizada, null),
                cartaoResumo("Percentual de conclusão", valorPercentual, barraConclusao));
        resumo.setPrefHeight(112);
        return resumo;
    }

    private static VBox cartaoResumo(String titulo, Label valor, Barra barra) {
        Label t = Estilo.rotulo(titulo);
        t.setStyle(Estilo.fonte(13, false) + "-fx-text-fill: #6B7280;");

        VBox c = new VBox(6, t, valor);
        if (barra != null) {
            VBox.setMargin(barra, new Insets(2, 0, 0, 0));
            c.getChildren().add(barra);
        }
        c.setPadding(new Insets(16, 18, 16, 18));
        c.setMinHeight(112);
        c.setMaxWidth(Double.MAX_VALUE);
        c.setPrefWidth(270);
        HBox.setHgrow(c, Priority.ALWAYS);
        c.setStyle("-fx-background-color: white; -fx-border-color: " + Estilo.BORDA + ";");
        return c;
    }

    private static Label valorGrande() {
        Label l = new Label("–");
        l.setStyle(Estilo.fonte(28, true) + "-fx-text-fill: " + Estilo.TEXTO + ";");
        return l;
    }

    private VBox montarCartaoTabela() {
        tabela.setStyle("-fx-background-color: white;");
        double[] larguras = {30, 15, 15, 15, 25};   // % de cada coluna
        for (double l : larguras) {
            ColumnConstraints c = new ColumnConstraints();
            c.setPercentWidth(l);
            tabela.getColumnConstraints().add(c);
        }

        VBox cartao = new VBox(Estilo.cabecalhoCartao("Metas por Grupo", 16, 52), tabela);
        cartao.setStyle("-fx-background-color: white; -fx-border-color: " + Estilo.BORDA + ";");
        return cartao;
    }

    // =====================================================================
    // Dados
    // =====================================================================

    private void carregarPesquisas() {
        try {
            comboPesquisa.getItems().addAll(pesquisas.listarPesquisas());
        } catch (ExcecaoRegraNegocio e) {
            mostrarErro(e.getMessage());
        }
        if (comboPesquisa.getItems().isEmpty()) {
            mostrarErro("Não há pesquisas cadastradas.");
        } else {
            comboPesquisa.getSelectionModel().selectFirst();
        }
    }

    /** Recalcula os números da pesquisa escolhida (ao trocar a pesquisa, ao clicar em Atualizar e ao voltar à janela). */
    public void atualizar() {
        Pesquisa escolhida = comboPesquisa.getValue();
        if (escolhida == null) {
            zerar();
            return;
        }
        mensagem.setText(" ");
        try {
            RelatorioAcompanhamento r = coleta.acompanhar(escolhida.getCodigo());
            valorPrevista.setText(String.valueOf(r.getQuantidadePrevista()));
            valorRealizada.setText(String.valueOf(r.getQuantidadeRealizada()));
            valorPercentual.setText(String.format("%.1f%%", r.getPercentualConclusao()));
            barraConclusao.definir(r.getPercentualConclusao() / 100.0);
            // o plano amostral vem da pesquisa mais recente (o combo guarda a cópia do momento do carregamento)
            Pesquisa atual = pesquisas.buscarPesquisaPorCodigo(escolhida.getCodigo());
            montarTabela(atual.getPlanoAmostral(), r.getEntrevistasPorGrupo());
        } catch (ExcecaoRegraNegocio e) {
            zerar();
            mostrarErro(e.getMessage());
        }
    }

    private void zerar() {
        valorPrevista.setText("–");
        valorRealizada.setText("–");
        valorPercentual.setText("–");
        barraConclusao.definir(0);
        tabela.getChildren().clear();
    }

    private void montarTabela(PlanoAmostral plano, Map<String, Integer> porGrupo) {
        tabela.getChildren().clear();
        String[] titulos = {"Grupo", "Meta mínima", "Realizadas", "Faltam", "Progresso"};
        for (int c = 0; c < titulos.length; c++) {
            tabela.add(celula(titulos[c], c, "#4B5563", true), c, 0);
        }

        int linha = 1;
        if (plano == null || plano.getMetas().isEmpty()) {
            Label vazio = new Label("Esta pesquisa ainda não tem metas no plano amostral.");
            vazio.setStyle(Estilo.fonte(13, false) + "-fx-text-fill: " + Estilo.TITULO_CLARO + ";");
            vazio.setPadding(new Insets(18, 16, 18, 16));
            tabela.add(vazio, 0, 1, titulos.length, 1);
            return;
        }
        for (MetaAmostral meta : plano.getMetas()) {
            int feitas = porGrupo.getOrDefault(meta.getGrupo(), 0);
            int faltam = Math.max(0, meta.getMinimo() - feitas);
            boolean abaixo = !meta.atingiuMeta(feitas);
            String fundo = abaixo ? AVERMELHADO : "white";

            tabela.add(celula(meta.getGrupo(), 0, fundo, false), 0, linha);
            tabela.add(celula(String.valueOf(meta.getMinimo()), 1, fundo, false), 1, linha);
            tabela.add(celula(String.valueOf(feitas), 2, fundo, false), 2, linha);
            tabela.add(celula(abaixo ? String.valueOf(faltam) : "Meta atingida", 3, fundo, false), 3, linha);

            Barra b = new Barra();
            b.definir(meta.getMinimo() == 0 ? 1 : (double) feitas / meta.getMinimo());
            StackPane caixa = new StackPane(b);
            caixa.setPadding(new Insets(0, 16, 0, 12));
            caixa.setPrefHeight(44);
            caixa.setStyle("-fx-background-color: " + fundo + ";"
                    + " -fx-border-color: transparent transparent " + Estilo.BORDA + " transparent;");
            tabela.add(caixa, 4, linha);
            linha++;
        }
    }

    /** Célula de texto. Cabeçalho de coluna: cinza-ardósia; linhas: texto escuro com linha fina embaixo. */
    private static Label celula(String texto, int coluna, String fundo, boolean cabecalho) {
        Label l = new Label(texto);
        l.setMaxWidth(Double.MAX_VALUE);
        l.setMaxHeight(Double.MAX_VALUE);
        l.setPrefHeight(44);
        l.setAlignment(coluna == 0 || coluna == 4 ? Pos.CENTER_LEFT : Pos.CENTER);
        l.setStyle(Estilo.fonte(13, cabecalho) + "-fx-text-fill: " + (cabecalho ? "white" : Estilo.TEXTO) + ";"
                + " -fx-background-color: " + fundo + "; -fx-padding: 0 16 0 16;"
                + (cabecalho ? "" : " -fx-border-color: transparent transparent " + Estilo.BORDA + " transparent;"));
        return l;
    }

    private void mostrarErro(String texto) {
        mensagem.setText(texto);
    }

    // =====================================================================
    // Barra de progresso desenhada com dois retângulos (cinza-ardósia sobre cinza claro)
    // =====================================================================

    private static class Barra extends StackPane {
        private final DoubleProperty fracao = new SimpleDoubleProperty(0);

        Barra() {
            setPrefHeight(10);
            setMinHeight(10);
            setMaxHeight(10);
            setMaxWidth(Double.MAX_VALUE);
            setAlignment(Pos.CENTER_LEFT);
            setStyle("-fx-background-color: #E5E7EB; -fx-background-radius: 5;");

            Region cheio = new Region();
            cheio.setStyle("-fx-background-color: " + Estilo.ARDOSIA_BOTAO + "; -fx-background-radius: 5;");
            cheio.minWidthProperty().bind(widthProperty().multiply(fracao));
            cheio.prefWidthProperty().bind(widthProperty().multiply(fracao));
            cheio.maxWidthProperty().bind(widthProperty().multiply(fracao));
            getChildren().add(cheio);
        }

        void definir(double valor) {
            fracao.set(Math.max(0, Math.min(1, valor)));
        }
    }
}