package telas.administracao;

import dominio.administracao.Perfil;
import dominio.administracao.RelatorioUsuarios;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import servicos.administracao.UsuarioServico;
import telas.Alertas;
import telas.Contexto;
import telas.Tela;

import java.io.File;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Map;

public class RelatorioUser implements Tela {
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final UsuarioServico servico = Contexto.usuarios;

    private final Label valorTotal = new Label("0");
    private final Label valorAtivos = new Label("0");
    private final Label valorInativos = new Label("0");
    private final Label geradoEm = new Label();

    private final CategoryAxis eixoX = new CategoryAxis();
    private final NumberAxis eixoY = new NumberAxis();
    private final BarChart<String, Number> grafico = new BarChart<>(eixoX, eixoY);
    private final TableView<Map.Entry<Perfil, Integer>> tabela = new TableView<>();

    @Override
    public Parent getRoot() {
        Label titulo = new Label("Relatório de Usuários");
        titulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        HBox linhaTitulo = new HBox(titulo);
        linhaTitulo.setAlignment(Pos.CENTER);

        HBox cartoes = new HBox(15,
                cartao("Total de usuários", valorTotal),
                cartao("Ativos", valorAtivos),
                cartao("Inativos", valorInativos));

        configurarGrafico();
        VBox ladoGrafico = new VBox(6, grafico, new Label(
                "Um usuário com mais de um perfil é contado em cada um deles."));
        HBox.setHgrow(ladoGrafico, Priority.ALWAYS);
        HBox meio = new HBox(20, ladoGrafico, criarTabela());

        Button atualizar = new Button("Atualizar");
        atualizar.setOnAction(e -> Alertas.executar(this::atualizar));
        Button exportar = new Button("Exportar CSV");
        exportar.setOnAction(e -> Alertas.executar(() -> exportar((Node) e.getSource())));

        Region espaco = new Region();
        HBox.setHgrow(espaco, Priority.ALWAYS);
        HBox rodape = new HBox(10, geradoEm, espaco, atualizar, exportar);
        rodape.setAlignment(Pos.CENTER_LEFT);

        VBox raiz = new VBox(18, linhaTitulo, cartoes, meio, rodape);
        raiz.setPadding(new Insets(25, 30, 25, 30));

        Alertas.executar(this::atualizar);
        return raiz;
    }

    // ---------- montagem ----------

    private VBox cartao(String titulo, Label valor) {
        valor.setStyle("-fx-font-size: 30px; -fx-font-weight: bold;");
        VBox c = new VBox(4, valor, new Label(titulo));
        c.setPadding(new Insets(15));
        c.setPrefWidth(190);
        c.setStyle("-fx-background-color: #f2f5fa; -fx-border-color: #d5dbe6;"
                + "-fx-background-radius: 6; -fx-border-radius: 6;");
        return c;
    }

    private void configurarGrafico() {
        grafico.setTitle("Usuários por perfil");
        grafico.setLegendVisible(false);
        grafico.setAnimated(false);
        grafico.setPrefHeight(300);
        eixoY.setAutoRanging(false);          // escala em números inteiros
        eixoY.setLowerBound(0);
        eixoY.setTickUnit(1);
        eixoY.setMinorTickCount(0);
    }

    private TableView<Map.Entry<Perfil, Integer>> criarTabela() {
        TableColumn<Map.Entry<Perfil, Integer>, String> cPerfil = new TableColumn<>("Perfil");
        cPerfil.setCellValueFactory(c -> new SimpleStringProperty(nomeAmigavel(c.getValue().getKey())));
        TableColumn<Map.Entry<Perfil, Integer>, String> cQtd = new TableColumn<>("Usuários");
        cQtd.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getValue())));

        tabela.getColumns().addAll(cPerfil, cQtd);
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabela.setPrefWidth(280);
        tabela.setPrefHeight(150);
        tabela.setMaxHeight(150);
        return tabela;
    }

    // ---------- dados ----------

    private void atualizar() {
        RelatorioUsuarios r = servico.gerarRelatorio();

        valorTotal.setText(String.valueOf(r.getTotal()));
        valorAtivos.setText(String.valueOf(r.getAtivos()));
        valorInativos.setText(String.valueOf(r.getInativos()));
        geradoEm.setText("Gerado em " + r.getGeradoEm().format(FORMATO));

        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        int maximo = 1;
        for (Map.Entry<Perfil, Integer> e : r.getPorPerfil().entrySet()) {
            serie.getData().add(new XYChart.Data<>(nomeAmigavel(e.getKey()), e.getValue()));
            maximo = Math.max(maximo, e.getValue());
        }
        eixoY.setUpperBound(maximo + 1);
        grafico.getData().clear();
        grafico.getData().add(serie);

        tabela.setItems(FXCollections.observableArrayList(new ArrayList<>(r.getPorPerfil().entrySet())));
    }

    private void exportar(Node origem) {
        FileChooser seletor = new FileChooser();
        seletor.setTitle("Exportar relatório de usuários");
        seletor.setInitialFileName("relatorio-usuarios.csv");
        seletor.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivo CSV (*.csv)", "*.csv"));

        File arquivo = seletor.showSaveDialog(origem.getScene().getWindow());
        if (arquivo == null) {
            return;                       // usuário cancelou
        }

        Path destino;
        if(arquivo.getName().toLowerCase().endsWith(".csv")){
            destino = arquivo.toPath();
        } else {
            destino = Path.of(arquivo.getPath() + ".csv");
        }

        servico.exportarRelatorio(destino);
        Alertas.info("Relatório exportado em:\n" + destino);
    }

    private static String nomeAmigavel(Perfil p) {
        String n = p.name();
        return n.charAt(0) + n.substring(1).toLowerCase();    // ADMINISTRADOR -> Administrador
    }
}