package telas.analise;

import dominio.analiseEstatistica.Dimensao;
import dominio.analiseEstatistica.FiltroRelatorio;
import dominio.analiseEstatistica.PesquisaResumo;
import dominio.analiseEstatistica.PesquisaResumo.Situacao;
import dominio.analiseEstatistica.Relatorio;
import dominio.analiseEstatistica.Resultado;
import persistencia.analiseEstatistica.FormatoExportacao;
import telas.Contexto;
import telas.Tela;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

public class GestaoAnalise implements Tela {

    // --------------------------------------------------
    // CORES
    // --------------------------------------------------

    private static final String AZUL = "#3B3F58";
    private static final String FUNDO = "#F3F4F6";
    private static final String BORDA = "#D1D5DB";
    private static final String TEXTO_SUAVE = "#64748B";

    // --------------------------------------------------
    // ESTRUTURA GERAL
    // --------------------------------------------------

    private final BorderPane raiz = new BorderPane();
    private final VBox conteudo = new VBox(18);
    private final ScrollPane rolagem = new ScrollPane(conteudo);
    private boolean inicializado = false;

    private final Button btnResultados = new Button("Resultados");
    private final Button btnComparacoes = new Button("Comparações");
    private final Button btnRelatorios = new Button("Relatórios");

    // --------------------------------------------------
    // RESULTADOS
    // --------------------------------------------------

    private final ComboBox<PesquisaResumo> selecaoPesquisa = new ComboBox<>();
    private final TextField campoPergunta = new TextField("Q1");
    private final Button btnProcessar = new Button("Processar pesquisa");

    private final Label situacaoBadge = new Label("—");
    private final Label dicaProcessamento = new Label();
    private final Label totalRespostas = new Label("—");
    private final Label respostasValidas = new Label("—");
    private final Label mensagem = new Label();
    private final VBox distribuicao = new VBox(4);
    private final VBox painelResultado = new VBox(18);

    private final CategoryAxis eixoX = new CategoryAxis();
    private final NumberAxis eixoY = new NumberAxis();
    private final BarChart<String, Number> grafico = new BarChart<>(eixoX, eixoY);

    // --------------------------------------------------
    // COMPARAÇÕES
    // --------------------------------------------------

    private final ComboBox<PesquisaResumo> pesquisaA = new ComboBox<>();
    private final ComboBox<PesquisaResumo> pesquisaB = new ComboBox<>();
    private final TextField perguntaA = new TextField("Q1");
    private final TextField perguntaB = new TextField("Q1");
    private final Button btnComparar = new Button("Comparar pesquisas");
    private final Label dicaComparacao = new Label();
    private final Label mensagemComparacao = new Label();

    // --------------------------------------------------
    // RELATÓRIOS
    // --------------------------------------------------

    private final ComboBox<PesquisaResumo> pesquisaRelatorio = new ComboBox<>();
    private final TextField perguntaRelatorio = new TextField("Q1");
    private final DatePicker dataInicio = new DatePicker();
    private final DatePicker dataFim = new DatePicker();
    private final ComboBox<Dimensao> dimensao = new ComboBox<>();
    private final TextField valorCaracteristica = new TextField();
    private final CheckBox incluirCruzamentos =
            new CheckBox("Incluir cruzamentos entre características");
    private final Button btnGerar = new Button("Gerar relatório");
    private final Label dicaRelatorio = new Label();
    private final Label mensagemRelatorio = new Label();

    // Relatório atual exibido na interface
    private final TextArea areaRelatorio = new TextArea();
    private Relatorio relatorioAtual;

    // --------------------------------------------------
    // INICIALIZAÇÃO
    // --------------------------------------------------

    @Override
    public Parent getRoot() {
        if (!inicializado) {
            inicializado = true;

            raiz.setStyle("-fx-background-color: " + FUNDO + ";");
            raiz.setTop(criarCabecalho());

            rolagem.setFitToWidth(true);
            rolagem.setStyle(
                "-fx-background: " + FUNDO + ";" +
                "-fx-background-color: " + FUNDO + ";"
            );
            raiz.setCenter(rolagem);

            configurarAreaRelatorio();
            configurarGrafico();
            configurarListeners();

            limparFeedback(mensagem);
            limparFeedback(mensagemComparacao);
            limparFeedback(mensagemRelatorio);

            painelResultado.setVisible(false);
            painelResultado.setManaged(false);
        }

        carregarPesquisas(true);
        mostrarAba("Resultados");

        return raiz;
    }

    private HBox criarCabecalho() {
        btnResultados.setOnAction(e -> mostrarAba("Resultados"));
        btnComparacoes.setOnAction(e -> mostrarAba("Comparações"));
        btnRelatorios.setOnAction(e -> mostrarAba("Relatórios"));

        HBox navegacao = new HBox(10, btnResultados, btnComparacoes, btnRelatorios);
        navegacao.setAlignment(Pos.CENTER_LEFT);
        navegacao.setPadding(new Insets(15, 30, 10, 30));
        navegacao.setStyle("-fx-background-color: " + FUNDO + ";");
        return navegacao;
    }

    private void configurarListeners() {
        selecaoPesquisa.valueProperty().addListener(
            (obs, antigo, novo) -> atualizarEstadoBotoes());
        pesquisaA.valueProperty().addListener(
            (obs, antigo, novo) -> atualizarEstadoBotoes());
        pesquisaB.valueProperty().addListener(
            (obs, antigo, novo) -> atualizarEstadoBotoes());
        pesquisaRelatorio.valueProperty().addListener(
            (obs, antigo, novo) -> atualizarEstadoBotoes());
    }

    private void mostrarAba(String aba) {
        conteudo.getChildren().clear();
        conteudo.setPadding(new Insets(24));

        estilizarNavegacao(btnResultados, aba.equals("Resultados"));
        estilizarNavegacao(btnComparacoes, aba.equals("Comparações"));
        estilizarNavegacao(btnRelatorios, aba.equals("Relatórios"));

        switch (aba) {
            case "Resultados" ->
                conteudo.getChildren().add(criarAbaResultados());

            case "Comparações" ->
                conteudo.getChildren().add(criarAbaComparacoes());

            case "Relatórios" ->
                conteudo.getChildren().add(criarAbaRelatorios());
        }

        rolagem.setVvalue(0);
        atualizarEstadoBotoes();
    }

    // --------------------------------------------------
    // ESTILOS REUTILIZÁVEIS
    // --------------------------------------------------

    private void estilizarNavegacao(Button botao, boolean ativo) {
        if (ativo) {
            botao.setStyle("-fx-background-color: #475569; -fx-text-fill: white; -fx-font-weight: bold;");
        } else {
            botao.setStyle("");
        }
    }

    private void configurarBotao(Button botao) {
        botao.setStyle(
            "-fx-background-color: " + AZUL + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 13px;" +
            "-fx-padding: 9px 18px;" +
            "-fx-background-radius: 3px;" +
            "-fx-cursor: hand;"
        );
    }

    private void configurarBotaoSecundario(Button botao) {
        botao.setStyle(
            "-fx-background-color: white;" +
            "-fx-text-fill: " + AZUL + ";" +
            "-fx-font-size: 13px;" +
            "-fx-padding: 8px 16px;" +
            "-fx-background-radius: 3px;" +
            "-fx-border-color: " + AZUL + ";" +
            "-fx-border-radius: 3px;" +
            "-fx-cursor: hand;"
        );
    }

    private Label criarTitulo(String texto) {
        Label titulo = new Label(texto);
        titulo.setMaxWidth(Double.MAX_VALUE);
        titulo.setAlignment(Pos.CENTER);
        titulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1F2937;");
        return titulo;
    }

    private Label criarTextoSuave(String texto) {
        Label rotulo = new Label(texto);
        rotulo.setWrapText(true);
        rotulo.setStyle("-fx-text-fill: " + TEXTO_SUAVE + ";");
        return rotulo;
    }

    private Label criarRotulo(String texto) {
        Label rotulo = new Label(texto);
        rotulo.setStyle(
            "-fx-text-fill: #475569;" +
            "-fx-font-weight: bold;"
        );
        return rotulo;
    }

    private VBox criarCartao(String titulo, Node... filhos) {
        VBox cartao = new VBox(12);
        cartao.setPadding(new Insets(20));
        cartao.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: " + BORDA + ";" +
            "-fx-border-radius: 3px;" +
            "-fx-background-radius: 3px;" +
            "-fx-effect: dropshadow(gaussian, rgba(15,23,42,0.06), 8, 0, 0, 2);"
        );

        if (titulo != null) {
            Label tituloCartao = new Label(titulo);
            tituloCartao.setStyle(
                "-fx-font-size: 16px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: " + AZUL + ";"
            );
            cartao.getChildren().add(tituloCartao);
        }

        cartao.getChildren().addAll(filhos);
        return cartao;
    }

    private VBox criarIndicador(String titulo, Label valor) {
        Label rotulo = new Label(titulo);
        rotulo.setStyle("-fx-text-fill: " + TEXTO_SUAVE + "; -fx-font-size: 12px;");

        valor.setStyle(
            "-fx-font-size: 26px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: " + AZUL + ";"
        );

        VBox indicador = new VBox(4, rotulo, valor);
        indicador.setPadding(new Insets(16, 24, 16, 24));
        indicador.setMinWidth(190);
        indicador.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: " + BORDA + ";" +
            "-fx-border-radius: 3px;" +
            "-fx-background-radius: 3px;" +
            "-fx-effect: dropshadow(gaussian, rgba(15,23,42,0.06), 8, 0, 0, 2);"
        );
        return indicador;
    }

    // --------------------------------------------------
    // MENSAGENS DE FEEDBACK (sucesso, aviso e erro)
    // --------------------------------------------------

    private void mostrarSucesso(Label rotulo, String texto) {
        mostrarFeedback(rotulo, texto, "#475569");   // sucesso neutro, como na Coleta
    }

    private void mostrarAviso(Label rotulo, String texto) {
        mostrarFeedback(rotulo, texto, telas.Estilo.ERRO);
    }

    private void mostrarErro(Label rotulo, String texto) {
        mostrarFeedback(rotulo, texto, telas.Estilo.ERRO);
    }

    private void mostrarFeedback(Label rotulo, String texto, String cor) {
        rotulo.setText(texto);
        rotulo.setWrapText(true);
        rotulo.setMaxWidth(Double.MAX_VALUE);
        rotulo.setPadding(Insets.EMPTY);
        rotulo.setStyle("-fx-text-fill: " + cor + "; -fx-font-size: 13px;");
        rotulo.setVisible(true);
        rotulo.setManaged(true);
    }

    private void limparFeedback(Label rotulo) {
        rotulo.setText("");
        rotulo.setVisible(false);
        rotulo.setManaged(false);
    }

    // --------------------------------------------------
    // SITUAÇÃO DA PESQUISA
    // --------------------------------------------------

    private String rotuloSituacao(Situacao situacao) {
        return switch (situacao) {
            case PLANEJADA -> "Planejada";
            case EM_COLETA -> "Em coleta";
            case ENCERRADA -> "Encerrada";
            case PROCESSADA -> "Processada";
        };
    }

    private String descricaoPesquisa(PesquisaResumo pesquisa) {
        return pesquisa.nome() + " (" + pesquisa.codigo() + ") — "
            + rotuloSituacao(pesquisa.situacao());
    }

    private boolean estaProcessada(PesquisaResumo pesquisa) {
        return pesquisa != null && pesquisa.situacao() == Situacao.PROCESSADA;
    }

    private boolean podeProcessar(PesquisaResumo pesquisa) {
        return pesquisa != null
            && (pesquisa.situacao() == Situacao.ENCERRADA
                || pesquisa.situacao() == Situacao.PROCESSADA);
    }

    private void atualizarBadge(PesquisaResumo pesquisa) {
        String texto;
        String fundo;
        String cor;

        if (pesquisa == null) {
            texto = "—";
            fundo = "#E5E7EB";
            cor = "#374151";
        } else {
            String[] estilo = switch (pesquisa.situacao()) {
                case PLANEJADA -> new String[]{"Planejada", "#E5E7EB", "#374151"};
                case EM_COLETA -> new String[]{"Em coleta", "#FEF3C7", "#92400E"};
                case ENCERRADA -> new String[]{"Encerrada", "#DBEAFE", "#1E40AF"};
                case PROCESSADA -> new String[]{"Processada", "#DCFCE7", "#166534"};
            };
            texto = estilo[0];
            fundo = estilo[1];
            cor = estilo[2];
        }

        situacaoBadge.setText(texto);
        situacaoBadge.setStyle(
            "-fx-background-color: " + fundo + ";" +
            "-fx-text-fill: " + cor + ";" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 3px 12px;" +
            "-fx-background-radius: 3px;"
        );
    }

    private String dicaDeProcessamento(PesquisaResumo pesquisa) {
        if (pesquisa == null) {
            return "Nenhuma pesquisa selecionada.";
        }
        return switch (pesquisa.situacao()) {
            case PLANEJADA, EM_COLETA ->
                "Disponível após o encerramento da coleta.";
            case ENCERRADA ->
                "Pronta para ser processada.";
            case PROCESSADA ->
                "Já processada. Processar novamente atualiza o resultado.";
        };
    }

    /** Habilita ou bloqueia os botões conforme a situação das pesquisas selecionadas. */
    private void atualizarEstadoBotoes() {
        PesquisaResumo selecionada = selecaoPesquisa.getValue();
        atualizarBadge(selecionada);
        btnProcessar.setDisable(!podeProcessar(selecionada));
        dicaProcessamento.setText(dicaDeProcessamento(selecionada));

        boolean comparacaoLiberada =
            estaProcessada(pesquisaA.getValue()) && estaProcessada(pesquisaB.getValue());
        btnComparar.setDisable(!comparacaoLiberada);
        dicaComparacao.setText(comparacaoLiberada
            ? ""
            : "Selecione duas pesquisas já processadas.");

        boolean relatorioLiberado = estaProcessada(pesquisaRelatorio.getValue());
        btnGerar.setDisable(!relatorioLiberado);
        dicaRelatorio.setText(relatorioLiberado
            ? ""
            : "Disponível apenas para pesquisas já processadas.");
    }

    // --------------------------------------------------
    // COMBO DE PESQUISAS
    // --------------------------------------------------

    private void configurarComboPesquisa(ComboBox<PesquisaResumo> combo) {
        combo.setPromptText("Selecione uma pesquisa");
        combo.setPrefWidth(340);

        combo.setCellFactory(lista -> new ListCell<PesquisaResumo>() {
            @Override
            protected void updateItem(PesquisaResumo pesquisa, boolean vazio) {
                super.updateItem(pesquisa, vazio);
                setText(vazio || pesquisa == null
                    ? null
                    : descricaoPesquisa(pesquisa));
            }
        });

        combo.setButtonCell(new ListCell<PesquisaResumo>() {
            @Override
            protected void updateItem(PesquisaResumo pesquisa, boolean vazio) {
                super.updateItem(pesquisa, vazio);
                setText(vazio || pesquisa == null
                    ? null
                    : descricaoPesquisa(pesquisa));
            }
        });
    }

    // --------------------------------------------------
    // ABA RESULTADOS
    // --------------------------------------------------

    private VBox criarAbaResultados() {
        Label titulo = criarTitulo("Resultados das pesquisas");

        Label descricao = criarTextoSuave(
            "Selecione uma pesquisa e informe a pergunta que deseja processar."
        );

        configurarComboPesquisa(selecaoPesquisa);
        campoPergunta.setPromptText("Ex.: Q1");
        campoPergunta.setPrefWidth(90);

        configurarBotao(btnProcessar);
        btnProcessar.setOnAction(e -> processarPesquisa());

        FlowPane formulario = new FlowPane(
            10, 10,
            criarRotulo("Pesquisa:"),
            selecaoPesquisa,
            criarRotulo("Pergunta:"),
            campoPergunta,
            btnProcessar
        );
        formulario.setAlignment(Pos.CENTER_LEFT);

        dicaProcessamento.setStyle("-fx-text-fill: " + TEXTO_SUAVE + ";");

        HBox situacao = new HBox(
            10,
            criarRotulo("Situação:"),
            situacaoBadge,
            dicaProcessamento
        );
        situacao.setAlignment(Pos.CENTER_LEFT);

        VBox cartaoSelecao = criarCartao(
            "Processar pesquisa", formulario, situacao
        );

        HBox indicadores = new HBox(
            16,
            criarIndicador("Total de respostas", totalRespostas),
            criarIndicador("Respostas válidas", respostasValidas)
        );

        VBox cartaoGrafico = criarCartao(
            "Distribuição das respostas válidas (%)", grafico
        );

        VBox cartaoDistribuicao = criarCartao("Detalhamento", distribuicao);

        painelResultado.getChildren().setAll(
            indicadores, cartaoGrafico, cartaoDistribuicao
        );

        return new VBox(
            18,
            titulo,
            descricao,
            cartaoSelecao,
            mensagem,
            painelResultado
        );
    }

    private void processarPesquisa() {
        PesquisaResumo pesquisa = selecaoPesquisa.getValue();
        String pergunta = campoPergunta.getText().trim();

        if (pesquisa == null) {
            mostrarAviso(mensagem, "Selecione uma pesquisa.");
            return;
        }

        if (pergunta.isEmpty()) {
            mostrarAviso(mensagem, "Informe o identificador da pergunta.");
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

            distribuicao.getChildren().clear();

            adicionarSecaoResultados(
                "Respostas válidas",
                resultado.linhasValidas()
            );

            adicionarSecaoResultados(
                "Respostas especiais",
                resultado.linhasEspeciais()
            );

            atualizarGrafico(resultado.linhasValidas());

            painelResultado.setVisible(true);
            painelResultado.setManaged(true);

            mostrarSucesso(
                mensagem,
                "Processamento concluído: " + pesquisa.nome()
                    + " (pergunta " + pergunta + ")."
            );

            // A situação da pesquisa mudou para "Processada": atualiza a tela.
            carregarPesquisas(false);

        } catch (RuntimeException erro) {
            painelResultado.setVisible(false);
            painelResultado.setManaged(false);
            mostrarErro(
                mensagem,
                "Não foi possível processar: " + erro.getMessage()
            );
        }
    }

    private void adicionarSecaoResultados(
        String titulo,
        List<Resultado.LinhaResultado> linhas
    ) {
        Label tituloSecao = new Label(titulo);
        tituloSecao.setPadding(new Insets(8, 0, 4, 0));
        tituloSecao.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: " + AZUL + ";"
        );

        distribuicao.getChildren().add(tituloSecao);

        if (linhas.isEmpty()) {
            distribuicao.getChildren().add(
                criarTextoSuave("Nenhum resultado para exibir.")
            );
            return;
        }

        for (Resultado.LinhaResultado linha : linhas) {
            distribuicao.getChildren().add(criarLinhaResultado(linha));
        }
    }

    private HBox criarLinhaResultado(Resultado.LinhaResultado linha) {
        Label rotulo = new Label(linha.rotulo());
        rotulo.setMinWidth(220);
        rotulo.setPrefWidth(220);

        double progresso = Math.max(0, Math.min(1, linha.percentual() / 100.0));
        ProgressBar barra = new ProgressBar(progresso);
        barra.setPrefWidth(260);
        barra.setStyle("-fx-accent: " + AZUL + ";");

        Label quantidade = new Label(String.valueOf(linha.absoluto()));
        quantidade.setMinWidth(50);
        quantidade.setAlignment(Pos.CENTER_RIGHT);

        Label percentual = new Label(String.format("%.1f%%", linha.percentual()));
        percentual.setMinWidth(60);
        percentual.setStyle("-fx-font-weight: bold; -fx-text-fill: " + AZUL + ";");

        HBox linhaVisual = new HBox(16, rotulo, barra, quantidade, percentual);
        linhaVisual.setAlignment(Pos.CENTER_LEFT);
        linhaVisual.setPadding(new Insets(8, 0, 8, 0));
        linhaVisual.setStyle(
            "-fx-border-color: transparent transparent #E5E7EB transparent;"
        );

        return linhaVisual;
    }

    // --------------------------------------------------
    // GRÁFICO
    // --------------------------------------------------

    private void configurarGrafico() {
        eixoX.setLabel("Opções de resposta");

        eixoY.setLabel("Percentual (%)");
        eixoY.setAutoRanging(false);
        eixoY.setLowerBound(0);
        eixoY.setUpperBound(100);
        eixoY.setTickUnit(20);

        grafico.setLegendVisible(false);
        grafico.setAnimated(false);
        grafico.setCategoryGap(24);
        grafico.setMinHeight(280);
        grafico.setPrefHeight(320);
    }

    private void atualizarGrafico(List<Resultado.LinhaResultado> linhas) {
        grafico.getData().clear();

        XYChart.Series<String, Number> serie = new XYChart.Series<>();

        for (Resultado.LinhaResultado linha : linhas) {
            XYChart.Data<String, Number> ponto =
                new XYChart.Data<>(linha.rotulo(), linha.percentual());

            // Pinta as barras com a cor do tema assim que o nó for criado.
            ponto.nodeProperty().addListener((obs, antigo, no) -> {
                if (no != null) {
                    no.setStyle("-fx-bar-fill: " + AZUL + ";");
                }
            });

            serie.getData().add(ponto);
        }

        grafico.getData().add(serie);
    }

    // --------------------------------------------------
    // ABA COMPARAÇÕES
    // --------------------------------------------------

    private VBox criarAbaComparacoes() {
        Label titulo = criarTitulo("Comparação de pesquisas");

        Label descricao = criarTextoSuave(
            "Compare os resultados de duas pesquisas compatíveis."
        );

        configurarComboPesquisa(pesquisaA);
        configurarComboPesquisa(pesquisaB);

        perguntaA.setPromptText("Ex.: Q1");
        perguntaB.setPromptText("Ex.: Q1");
        perguntaA.setPrefWidth(100);
        perguntaB.setPrefWidth(100);

        VBox grupoA = new VBox(8,
            criarRotulo("Pesquisa A"),
            pesquisaA,
            criarRotulo("Pergunta"),
            perguntaA
        );

        VBox grupoB = new VBox(8,
            criarRotulo("Pesquisa B"),
            pesquisaB,
            criarRotulo("Pergunta"),
            perguntaB
        );

        HBox selecao = new HBox(40, grupoA, grupoB);
        selecao.setAlignment(Pos.TOP_LEFT);

        configurarBotao(btnComparar);
        btnComparar.setOnAction(e -> compararPesquisas());

        dicaComparacao.setStyle("-fx-text-fill: " + TEXTO_SUAVE + ";");

        HBox acao = new HBox(12, btnComparar, dicaComparacao);
        acao.setAlignment(Pos.CENTER_LEFT);

        VBox cartaoSelecao = criarCartao("Pesquisas a comparar", selecao, acao);

        VBox cartaoRelatorio = criarCartao(
            "Resultado da comparação",
            areaRelatorio,
            criarBotoesExportacao()
        );

        return new VBox(
            18,
            titulo,
            descricao,
            cartaoSelecao,
            mensagemComparacao,
            cartaoRelatorio
        );
    }

    private void compararPesquisas() {
        PesquisaResumo a = pesquisaA.getValue();
        PesquisaResumo b = pesquisaB.getValue();

        String pergunta1 = perguntaA.getText().trim();
        String pergunta2 = perguntaB.getText().trim();

        if (a == null || b == null) {
            mostrarAviso(mensagemComparacao, "Selecione as duas pesquisas.");
            return;
        }

        if (pergunta1.isEmpty() || pergunta2.isEmpty()) {
            mostrarAviso(
                mensagemComparacao,
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

            mostrarSucesso(mensagemComparacao, "Comparação gerada com sucesso.");

        } catch (RuntimeException erro) {
            relatorioAtual = null;
            areaRelatorio.clear();
            mostrarErro(
                mensagemComparacao,
                "Não foi possível comparar as pesquisas: " + erro.getMessage()
            );
        }
    }

    // --------------------------------------------------
    // ABA RELATÓRIOS
    // --------------------------------------------------

    private VBox criarAbaRelatorios() {
        Label titulo = criarTitulo("Relatórios estatísticos");

        Label descricao = criarTextoSuave(
            "Gere relatórios com filtros opcionais e exporte o resultado."
        );

        configurarComboPesquisa(pesquisaRelatorio);
        perguntaRelatorio.setPromptText("Ex.: Q1");
        perguntaRelatorio.setPrefWidth(90);

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

        FlowPane linhaPesquisa = new FlowPane(
            10, 10,
            criarRotulo("Pesquisa:"),
            pesquisaRelatorio,
            criarRotulo("Pergunta:"),
            perguntaRelatorio
        );
        linhaPesquisa.setAlignment(Pos.CENTER_LEFT);

        VBox cartaoPesquisa = criarCartao("Pesquisa e pergunta", linhaPesquisa);

        FlowPane linhaPeriodo = new FlowPane(
            10, 10,
            criarRotulo("De:"),
            dataInicio,
            criarRotulo("Até:"),
            dataFim
        );
        linhaPeriodo.setAlignment(Pos.CENTER_LEFT);

        FlowPane linhaCaracteristica = new FlowPane(
            10, 10,
            criarRotulo("Característica:"),
            dimensao,
            valorCaracteristica
        );
        linhaCaracteristica.setAlignment(Pos.CENTER_LEFT);

        VBox cartaoFiltros = criarCartao(
            "Filtros (opcionais)",
            linhaPeriodo,
            linhaCaracteristica,
            incluirCruzamentos
        );

        configurarBotao(btnGerar);
        btnGerar.setOnAction(e -> gerarRelatorio());

        dicaRelatorio.setStyle("-fx-text-fill: " + TEXTO_SUAVE + ";");

        HBox acao = new HBox(12, btnGerar, dicaRelatorio);
        acao.setAlignment(Pos.CENTER_LEFT);

        VBox cartaoRelatorio = criarCartao(
            "Relatório",
            areaRelatorio,
            criarBotoesExportacao()
        );

        return new VBox(
            18,
            titulo,
            descricao,
            cartaoPesquisa,
            cartaoFiltros,
            acao,
            mensagemRelatorio,
            cartaoRelatorio
        );
    }

    private void gerarRelatorio() {
        PesquisaResumo pesquisa = pesquisaRelatorio.getValue();
        String pergunta = perguntaRelatorio.getText().trim();

        if (pesquisa == null) {
            mostrarAviso(mensagemRelatorio, "Selecione uma pesquisa.");
            return;
        }

        if (pergunta.isEmpty()) {
            mostrarAviso(
                mensagemRelatorio,
                "Informe o identificador da pergunta."
            );
            return;
        }

        LocalDate inicio = dataInicio.getValue();
        LocalDate fim = dataFim.getValue();

        if ((inicio == null) != (fim == null)) {
            mostrarAviso(
                mensagemRelatorio,
                "Informe as duas datas do período ou deixe ambas vazias."
            );
            return;
        }

        if (inicio != null && fim.isBefore(inicio)) {
            mostrarAviso(
                mensagemRelatorio,
                "A data final não pode ser anterior à inicial."
            );
            return;
        }

        Dimensao caracteristica = dimensao.getValue();
        String valor = valorCaracteristica.getText().trim();

        if ((caracteristica == null) != valor.isEmpty()) {
            mostrarAviso(
                mensagemRelatorio,
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

            mostrarSucesso(mensagemRelatorio, "Relatório gerado com sucesso.");

        } catch (RuntimeException erro) {
            relatorioAtual = null;
            areaRelatorio.clear();
            mostrarErro(
                mensagemRelatorio,
                "Não foi possível gerar o relatório: " + erro.getMessage()
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
        areaRelatorio.setMinHeight(320);
        areaRelatorio.setMaxWidth(Double.MAX_VALUE);
        areaRelatorio.setPromptText(
            "O relatório gerado será exibido aqui."
        );
        // Fonte monoespaçada: mantém as colunas da tabela alinhadas.
        areaRelatorio.setStyle(
            "-fx-font-family: 'monospaced';" +
            "-fx-font-size: 12px;"
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

        configurarBotaoSecundario(btnPDF);
        configurarBotaoSecundario(btnCSV);

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
                Alert.AlertType.WARNING,
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
                Alert.AlertType.INFORMATION,
                "Relatório salvo em: " + destino.toAbsolutePath()
            );

        } catch (RuntimeException erro) {
            mostrarMensagemExportacao(
                Alert.AlertType.ERROR,
                "Erro ao exportar: " + erro.getMessage()
            );
        }
    }

    private void mostrarMensagemExportacao(Alert.AlertType tipo, String texto) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle("Exportação de relatório");
        alerta.setHeaderText(null);
        alerta.setContentText(texto);
        alerta.showAndWait();
    }

    // --------------------------------------------------
    // CARREGAMENTO DAS PESQUISAS
    // --------------------------------------------------

    /**
     * Carrega as pesquisas nos seletores, mantendo a pesquisa já escolhida.
     * Com mostrarMensagens = false, falhas são ignoradas (usado para apenas
     * atualizar a situação das pesquisas depois de um processamento).
     */
    private void carregarPesquisas(boolean mostrarMensagens) {
        try {
            if (Contexto.analise == null) {
                if (mostrarMensagens) {
                    mostrarErro(
                        mensagem,
                        "Serviço de análise não foi inicializado."
                    );
                }
                return;
            }

            List<PesquisaResumo> pesquisas =
                Contexto.analise.listarPesquisas();

            preencherCombo(selecaoPesquisa, pesquisas);
            preencherCombo(pesquisaA, pesquisas);
            preencherCombo(pesquisaB, pesquisas);
            preencherCombo(pesquisaRelatorio, pesquisas);

            if (mostrarMensagens && pesquisas.isEmpty()) {
                mostrarAviso(mensagem, "Nenhuma pesquisa encontrada.");
            }

        } catch (RuntimeException erro) {
            if (mostrarMensagens) {
                mostrarErro(
                    mensagem,
                    "Erro ao carregar pesquisas: " + erro.getMessage()
                );
            }
        }
    }

    private void preencherCombo(
        ComboBox<PesquisaResumo> combo,
        List<PesquisaResumo> pesquisas
    ) {
        String codigoAnterior = combo.getValue() == null
            ? null
            : combo.getValue().codigo();

        combo.setItems(FXCollections.observableArrayList(pesquisas));

        PesquisaResumo escolhida = null;

        if (codigoAnterior != null) {
            for (PesquisaResumo p : pesquisas) {
                if (p.codigo().equals(codigoAnterior)) {
                    escolhida = p;
                    break;
                }
            }
        }

        if (escolhida == null && !pesquisas.isEmpty()) {
            escolhida = pesquisas.get(0);
        }

        combo.setValue(escolhida);
    }
}
