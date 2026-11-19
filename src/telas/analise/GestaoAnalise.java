package telas.analise;

import dominio.analiseEstatistica.Dimensao;
import dominio.analiseEstatistica.FiltroRelatorio;
import dominio.analiseEstatistica.PesquisaResumo;
import dominio.analiseEstatistica.Relatorio;
import dominio.analiseEstatistica.Resultado;
import persistencia.analiseEstatistica.FormatoExportacao;
import excecoes.ExcecaoRegraNegocio;
import telas.Contexto;
import telas.Tela;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.FileChooser;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

public class GestaoAnalise implements Tela {

    private final BorderPane raiz = new BorderPane();
    private final VBox conteudo = new VBox(15);

    private final Button btnResultados = new Button("Resultados");
    private final Button btnComparacoes = new Button("Comparações");
    private final Button btnRelatorios = new Button("Relatórios");

    // Resultados
    private final ComboBox<PesquisaResumo> selecaoPesquisa = new ComboBox<>();
    private final TextField campoPergunta = new TextField("Q1");
    private final Button btnProcessar = new Button("Processar pesquisa");

    private final Label situacaoPesquisa = new Label();
    private final Label totalRespostas = new Label("—");
    private final Label respostasValidas = new Label("—");
    private final Label mensagem = new Label();
    private final VBox distribuicao = new VBox(10);

    // Comparações
    private final ComboBox<PesquisaResumo> pesquisaA = new ComboBox<>();
    private final ComboBox<PesquisaResumo> pesquisaB = new ComboBox<>();
    private final TextField perguntaA = new TextField("Q1");
    private final TextField perguntaB = new TextField("Q1");
    private final Label mensagemComparacao = new Label();

    // Relatórios
    private final ComboBox<PesquisaResumo> pesquisaRelatorio = new ComboBox<>();
    private final TextField perguntaRelatorio = new TextField("Q1");
    private final DatePicker dataInicio = new DatePicker();
    private final DatePicker dataFim = new DatePicker();
    private final ComboBox<Dimensao> dimensao = new ComboBox<>();
    private final TextField valorCaracteristica = new TextField();
    private final CheckBox incluirCruzamentos =
            new CheckBox("Incluir cruzamentos entre características");

    private final Label mensagemRelatorio = new Label();

    // Relatório atual exibido na interface
    private final TextArea areaRelatorio = new TextArea();
    private Relatorio relatorioAtual;

    private static final String AZUL = "#3B3F58";
    private static final String FUNDO = "#F3F4F6";
    private static final String BORDA = "#D1D5DB";

    @Override
    public Parent getRoot() {
        raiz.setStyle("-fx-background-color: " + FUNDO + ";");
        raiz.setTop(criarCabecalho());

        configurarAreaRelatorio();
        mostrarAba("Resultados");
        carregarPesquisas();

        return raiz;
    }

    private VBox criarCabecalho() {
        Label titulo = new Label("Análise Estatística");
        titulo.setStyle(
            "-fx-font-size: 24px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: " + AZUL + ";"
        );

        Label descricao = new Label(
            "Processamento de pesquisas, resultados e relatórios"
        );
        descricao.setStyle("-fx-text-fill: #64748B;");

        HBox navegacao = new HBox(10);
        navegacao.setAlignment(Pos.CENTER_LEFT);
        navegacao.setPadding(new Insets(15, 0, 0, 0));

        configurarBotao(btnResultados);
        configurarBotao(btnComparacoes);
        configurarBotao(btnRelatorios);

        btnResultados.setOnAction(e -> mostrarAba("Resultados"));
        btnComparacoes.setOnAction(e -> mostrarAba("Comparações"));
        btnRelatorios.setOnAction(e -> mostrarAba("Relatórios"));

        navegacao.getChildren().addAll(
            btnResultados, btnComparacoes, btnRelatorios
        );

        VBox cabecalho = new VBox(8, titulo, descricao, navegacao);
        cabecalho.setPadding(new Insets(24));
        cabecalho.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: transparent transparent " + BORDA + " transparent;"
        );

        return cabecalho;
    }

    private void mostrarAba(String aba) {
        conteudo.getChildren().clear();
        conteudo.setPadding(new Insets(24));

        switch (aba) {
            case "Resultados" ->
                conteudo.getChildren().add(criarAbaResultados());

            case "Comparações" ->
                conteudo.getChildren().add(criarAbaComparacoes());

            case "Relatórios" ->
                conteudo.getChildren().add(criarAbaRelatorios());
        }

        raiz.setCenter(conteudo);
    }

    private void configurarBotao(Button botao) {
        botao.setStyle(
            "-fx-background-color: " + AZUL + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 13px;" +
            "-fx-padding: 9px 18px;" +
            "-fx-background-radius: 5px;" +
            "-fx-cursor: hand;"
        );
    }

    private Label criarTitulo(String texto) {
        Label titulo = new Label(texto);
        titulo.setStyle(
            "-fx-font-size: 20px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: " + AZUL + ";"
        );
        return titulo;
    }

    private void configurarComboPesquisa(
        ComboBox<PesquisaResumo> combo
    ) {
        combo.setPromptText("Selecione uma pesquisa");
        combo.setPrefWidth(280);

        combo.setCellFactory(lista -> new ListCell<PesquisaResumo>() {
            @Override
            protected void updateItem(PesquisaResumo pesquisa, boolean vazio) {
                super.updateItem(pesquisa, vazio);
                setText(vazio || pesquisa == null
                    ? null
                    : pesquisa.nome() + " (" + pesquisa.codigo() + ")");
            }
        });

        combo.setButtonCell(new ListCell<PesquisaResumo>() {
            @Override
            protected void updateItem(PesquisaResumo pesquisa, boolean vazio) {
                super.updateItem(pesquisa, vazio);
                setText(vazio || pesquisa == null
                    ? null
                    : pesquisa.nome() + " (" + pesquisa.codigo() + ")");
            }
        });
    }

    // --------------------------------------------------
    // ABA RESULTADOS
    // --------------------------------------------------

    private VBox criarAbaResultados() {
        Label titulo = criarTitulo("Resultados das pesquisas");

        Label descricao = new Label(
            "Selecione uma pesquisa e informe a pergunta que deseja processar."
        );

        configurarComboPesquisa(selecaoPesquisa);
        campoPergunta.setPromptText("Ex.: Q1");
        campoPergunta.setPrefWidth(100);

        configurarBotao(btnProcessar);
        btnProcessar.setOnAction(e -> processarPesquisa());

        HBox formulario = new HBox(
            10,
            new Label("Pesquisa:"),
            selecaoPesquisa,
            new Label("Pergunta:"),
            campoPergunta,
            btnProcessar
        );
        formulario.setAlignment(Pos.CENTER_LEFT);

        situacaoPesquisa.setStyle("-fx-text-fill: #64748B;");

        Label tituloTotal = new Label("Total de respostas");
        Label tituloValidas = new Label("Respostas válidas");

        HBox indicadores = new HBox(30,
            new VBox(5, tituloTotal, totalRespostas),
            new VBox(5, tituloValidas, respostasValidas)
        );

        totalRespostas.setStyle(
            "-fx-font-size: 22px; -fx-font-weight: bold;"
        );
        respostasValidas.setStyle(
            "-fx-font-size: 22px; -fx-font-weight: bold;"
        );

        mensagem.setWrapText(true);
        distribuicao.getChildren().clear();

        return new VBox(
            18,
            titulo,
            descricao,
            formulario,
            situacaoPesquisa,
            indicadores,
            mensagem,
            distribuicao
        );
    }

    private void processarPesquisa() {
        PesquisaResumo pesquisa =
            selecaoPesquisa.getSelectionModel().getSelectedItem();

        String pergunta = campoPergunta.getText().trim();

        if (pesquisa == null) {
            mensagem.setText("Selecione uma pesquisa.");
            return;
        }

        if (pergunta.isEmpty()) {
            mensagem.setText("Informe o identificador da pergunta.");
            return;
        }

        try {
            Resultado resultado = Contexto.analise.processarPesquisa(
                pesquisa.codigo(),
                pergunta
            );

            totalRespostas.setText(
                String.valueOf(resultado.totalRespostas())
            );

            respostasValidas.setText(
                String.valueOf(resultado.totalValidas())
            );

            situacaoPesquisa.setText(
                "Pesquisa processada: " + pesquisa.nome()
            );

            mensagem.setText("Processamento concluído com sucesso.");

            distribuicao.getChildren().clear();

            adicionarSecaoResultados(
                "Respostas válidas",
                resultado.linhasValidas()
            );

            adicionarSecaoResultados(
                "Respostas especiais",
                resultado.linhasEspeciais()
            );

        } catch (RuntimeException erro) {
            mensagem.setText(
                "Não foi possível processar: " + erro.getMessage()
            );
        }
    }

    private void adicionarSecaoResultados(
        String titulo,
        List<Resultado.LinhaResultado> linhas
    ) {
        Label tituloSecao = new Label(titulo);
        tituloSecao.setStyle(
            "-fx-font-size: 16px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: " + AZUL + ";"
        );

        distribuicao.getChildren().add(tituloSecao);

        if (linhas.isEmpty()) {
            distribuicao.getChildren().add(
                new Label("Nenhum resultado para exibir.")
            );
            return;
        }

        for (Resultado.LinhaResultado linha : linhas) {
            Label rotulo = new Label(linha.rotulo());
            Label quantidade = new Label(
                String.valueOf(linha.absoluto())
            );
            Label percentual = new Label(
                String.format("%.1f%%", linha.percentual())
            );

            HBox linhaVisual = new HBox(
                20, rotulo, quantidade, percentual
            );

            linhaVisual.setAlignment(Pos.CENTER_LEFT);
            linhaVisual.setPadding(new Insets(8));
            linhaVisual.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 5px;"
            );

            distribuicao.getChildren().add(linhaVisual);
        }
    }

    // --------------------------------------------------
    // ABA COMPARAÇÕES
    // --------------------------------------------------

    private VBox criarAbaComparacoes() {
        Label titulo = criarTitulo("Comparação de pesquisas");

        Label descricao = new Label(
            "Compare os resultados de duas pesquisas compatíveis."
        );

        configurarComboPesquisa(pesquisaA);
        configurarComboPesquisa(pesquisaB);

        perguntaA.setPromptText("Ex.: Q1");
        perguntaB.setPromptText("Ex.: Q1");
        perguntaA.setPrefWidth(100);
        perguntaB.setPrefWidth(100);

        VBox grupoA = new VBox(8,
            new Label("Pesquisa A"),
            pesquisaA,
            new Label("Pergunta"),
            perguntaA
        );

        VBox grupoB = new VBox(8,
            new Label("Pesquisa B"),
            pesquisaB,
            new Label("Pergunta"),
            perguntaB
        );

        HBox selecao = new HBox(30, grupoA, grupoB);
        selecao.setAlignment(Pos.CENTER_LEFT);

        Button btnComparar = new Button("Comparar pesquisas");
        configurarBotao(btnComparar);
        btnComparar.setOnAction(e -> compararPesquisas());

        mensagemComparacao.setWrapText(true);

        return new VBox(
            18,
            titulo,
            descricao,
            selecao,
            btnComparar,
            mensagemComparacao,
            areaRelatorio,
            criarBotoesExportacao()
        );
    }

    private void compararPesquisas() {
        PesquisaResumo a = pesquisaA.getValue();
        PesquisaResumo b = pesquisaB.getValue();

        String pergunta1 = perguntaA.getText().trim();
        String pergunta2 = perguntaB.getText().trim();

        if (a == null || b == null) {
            mensagemComparacao.setText(
                "Selecione as duas pesquisas."
            );
            return;
        }

        if (pergunta1.isEmpty() || pergunta2.isEmpty()) {
            mensagemComparacao.setText(
                "Informe as perguntas das duas pesquisas."
            );
            return;
        }

        try {
            relatorioAtual = Contexto.analise.compararPesquisas(
                a.codigo(),
                pergunta1,
                b.codigo(),
                pergunta2
            );

            exibirRelatorio(relatorioAtual);

            mensagemComparacao.setText(
                "Comparação gerada com sucesso."
            );

        } catch (RuntimeException erro) {
            relatorioAtual = null;
            areaRelatorio.clear();
            mensagemComparacao.setText(
                "Não foi possível comparar as pesquisas: "
                + erro.getMessage()
            );
        }
    }

    // --------------------------------------------------
    // ABA RELATÓRIOS
    // --------------------------------------------------

    private VBox criarAbaRelatorios() {
        Label titulo = criarTitulo("Relatórios estatísticos");

        Label descricao = new Label(
            "Gere relatórios com filtros opcionais e exporte o resultado."
        );

        configurarComboPesquisa(pesquisaRelatorio);
        perguntaRelatorio.setPromptText("Ex.: Q1");
        perguntaRelatorio.setPrefWidth(100);

        dataInicio.setPromptText("Data inicial");
        dataFim.setPromptText("Data final");

        dimensao.setItems(
            FXCollections.observableArrayList(Dimensao.values())
        );
        dimensao.setPromptText("Característica");

        dimensao.setCellFactory(lista -> new ListCell<Dimensao>() {
            @Override
            protected void updateItem(Dimensao item, boolean vazio) {
                super.updateItem(item, vazio);
                setText(vazio || item == null ? null : item.rotulo());
            }
        });

        dimensao.setButtonCell(new ListCell<Dimensao>() {
            @Override
            protected void updateItem(Dimensao item, boolean vazio) {
                super.updateItem(item, vazio);
                setText(vazio || item == null ? null : item.rotulo());
            }
        });

        valorCaracteristica.setPromptText("Valor da característica");
        valorCaracteristica.setPrefWidth(200);

        HBox linhaPesquisa = new HBox(
            10,
            new Label("Pesquisa:"),
            pesquisaRelatorio,
            new Label("Pergunta:"),
            perguntaRelatorio
        );
        linhaPesquisa.setAlignment(Pos.CENTER_LEFT);

        HBox linhaPeriodo = new HBox(
            10,
            new Label("De:"),
            dataInicio,
            new Label("Até:"),
            dataFim
        );
        linhaPeriodo.setAlignment(Pos.CENTER_LEFT);

        HBox linhaCaracteristica = new HBox(
            10,
            dimensao,
            valorCaracteristica
        );
        linhaCaracteristica.setAlignment(Pos.CENTER_LEFT);

        Button btnGerar = new Button("Gerar relatório");
        configurarBotao(btnGerar);
        btnGerar.setOnAction(e -> gerarRelatorio());

        mensagemRelatorio.setWrapText(true);

        return new VBox(
            18,
            titulo,
            descricao,
            linhaPesquisa,
            linhaPeriodo,
            linhaCaracteristica,
            incluirCruzamentos,
            btnGerar,
            mensagemRelatorio,
            areaRelatorio,
            criarBotoesExportacao()
        );
    }

    private void gerarRelatorio() {
        PesquisaResumo pesquisa = pesquisaRelatorio.getValue();
        String pergunta = perguntaRelatorio.getText().trim();

        if (pesquisa == null) {
            mensagemRelatorio.setText("Selecione uma pesquisa.");
            return;
        }

        if (pergunta.isEmpty()) {
            mensagemRelatorio.setText(
                "Informe o identificador da pergunta."
            );
            return;
        }

        LocalDate inicio = dataInicio.getValue();
        LocalDate fim = dataFim.getValue();

        if ((inicio == null) != (fim == null)) {
            mensagemRelatorio.setText(
                "Informe as duas datas do período ou deixe ambas vazias."
            );
            return;
        }

        if (inicio != null && fim.isBefore(inicio)) {
            mensagemRelatorio.setText(
                "A data final não pode ser anterior à inicial."
            );
            return;
        }

        Dimensao caracteristica = dimensao.getValue();
        String valor = valorCaracteristica.getText().trim();

        if ((caracteristica == null) != valor.isEmpty()) {
            mensagemRelatorio.setText(
                "Selecione uma característica e informe seu valor."
            );
            return;
        }

        try {
            FiltroRelatorio.Builder construtor =
                FiltroRelatorio.builder();

            if (inicio != null) {
                construtor.periodo(inicio, fim);
            }

            if (caracteristica != null) {
                construtor.caracteristica(caracteristica, valor);
            }

            FiltroRelatorio filtro = construtor.construir();

            relatorioAtual = Contexto.analise.gerarRelatorio(
                pesquisa.codigo(),
                pergunta,
                filtro,
                incluirCruzamentos.isSelected()
            );

            exibirRelatorio(relatorioAtual);

            mensagemRelatorio.setText(
                "Relatório gerado com sucesso."
            );

        } catch (RuntimeException erro) {
            relatorioAtual = null;
            areaRelatorio.clear();
            mensagemRelatorio.setText(
                "Não foi possível gerar o relatório: "
                + erro.getMessage()
            );
        }
    }

    // --------------------------------------------------
    // EXIBIÇÃO E EXPORTAÇÃO
    // --------------------------------------------------

    private void configurarAreaRelatorio() {
        areaRelatorio.setEditable(false);
        areaRelatorio.setWrapText(false);
        areaRelatorio.setPrefRowCount(18);
        areaRelatorio.setPrefColumnCount(80);
        areaRelatorio.setPromptText(
            "O relatório gerado será exibido aqui."
        );
    }

    private void exibirRelatorio(Relatorio relatorio) {
        areaRelatorio.setText(
            String.join(
                System.lineSeparator(),
                relatorio.linhasTexto()
            )
        );
    }

    private HBox criarBotoesExportacao() {
        Button btnPDF = new Button("Exportar PDF");
        Button btnCSV = new Button("Exportar CSV");

        configurarBotao(btnPDF);
        configurarBotao(btnCSV);

        btnPDF.setOnAction(e ->
            exportarRelatorio(FormatoExportacao.PDF)
        );

        btnCSV.setOnAction(e ->
            exportarRelatorio(FormatoExportacao.CSV)
        );

        HBox botoes = new HBox(10, btnPDF, btnCSV);
        botoes.setAlignment(Pos.CENTER_LEFT);

        return botoes;
    }

    private void exportarRelatorio(FormatoExportacao formato) {
        if (relatorioAtual == null) {
            mostrarMensagemExportacao(
                "Gere um relatório ou comparação antes de exportar."
            );
            return;
        }

        FileChooser seletor = new FileChooser();

        String extensao = formato == FormatoExportacao.PDF
            ? "*.pdf"
            : "*.csv";

        String nomeExtensao = formato == FormatoExportacao.PDF
            ? "PDF"
            : "CSV";

        seletor.setTitle("Salvar relatório");
        seletor.setInitialFileName("relatorio." +
            (formato == FormatoExportacao.PDF ? "pdf" : "csv"));

        seletor.getExtensionFilters().add(
            new FileChooser.ExtensionFilter(
                nomeExtensao + " (*." +
                (formato == FormatoExportacao.PDF ? "pdf" : "csv") + ")",
                extensao
            )
        );

        File arquivo = seletor.showSaveDialog(
            raiz.getScene().getWindow()
        );

        if (arquivo == null) {
            return;
        }

        try {
            Path destino = arquivo.toPath();

            if (!arquivo.getName().toLowerCase().endsWith(
                formato == FormatoExportacao.PDF ? ".pdf" : ".csv"
            )) {
                destino = Path.of(
                    arquivo.getAbsolutePath() +
                    (formato == FormatoExportacao.PDF ? ".pdf" : ".csv")
                );
            }

            Contexto.analise.exportar(
                relatorioAtual,
                formato,
                destino
            );

            mostrarMensagemExportacao(
                "Relatório salvo em: " + destino.toAbsolutePath()
            );

        } catch (RuntimeException erro) {
            mostrarMensagemExportacao(
                "Erro ao exportar: " + erro.getMessage()
            );
        }
    }

    private void mostrarMensagemExportacao(String texto) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle("Exportação de relatório");
        alerta.setHeaderText(null);
        alerta.setContentText(texto);
        alerta.showAndWait();
    }

    // --------------------------------------------------
    // CARREGAMENTO DAS PESQUISAS
    // --------------------------------------------------

    private void carregarPesquisas() {
        try {
            if (Contexto.analise == null) {
                mensagem.setText(
                    "Serviço de análise não foi inicializado."
                );
                return;
            }

            List<PesquisaResumo> pesquisas =
                Contexto.analise.listarPesquisas();

            var itens = FXCollections.observableArrayList(pesquisas);

            selecaoPesquisa.setItems(itens);
            pesquisaA.setItems(FXCollections.observableArrayList(pesquisas));
            pesquisaB.setItems(FXCollections.observableArrayList(pesquisas));
            pesquisaRelatorio.setItems(FXCollections.observableArrayList(pesquisas));

            if (!pesquisas.isEmpty()) {
                selecaoPesquisa.getSelectionModel().selectFirst();
                pesquisaA.getSelectionModel().selectFirst();
                pesquisaB.getSelectionModel().selectFirst();
                pesquisaRelatorio.getSelectionModel().selectFirst();

                situacaoPesquisa.setText(
                    pesquisas.size() + " pesquisa(s) encontrada(s)."
                );
            } else {
                situacaoPesquisa.setText(
                    "Nenhuma pesquisa encontrada."
                );
            }

        } catch (RuntimeException erro) {
            mensagem.setText(
                "Erro ao carregar pesquisas: " + erro.getMessage()
            );
        }
    }
}
