package telas.coleta;

import telas.BarraNavegacao;
import telas.Estilo;
import dominio.coleta.Entrevistador;
import excecoes.ExcecaoRegraNegocio;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import servicos.coleta.ColetaServico;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Tela "Entrevistadores" (Pesquisador), em JavaFX. À esquerda, o formulário de cadastro
 * (código e nome); à direita, a tabela com busca e o botão de remover. As regras (código
 * repetido, entrevistador com entrevistas) ficam no {@link ColetaServico}.
 */
public class TelaEntrevistadores {
    private final ColetaServico coleta;

    private final BorderPane raiz = new BorderPane();
    private final ObservableList<Entrevistador> todos = FXCollections.observableArrayList();
    private final FilteredList<Entrevistador> filtrados = new FilteredList<>(todos, e -> true);
    private final TableView<Entrevistador> tabela = new TableView<>(filtrados);

    private final TextField campoCodigo = Estilo.campoTexto();
    private final TextField campoNome = Estilo.campoTexto();
    private final TextField campoBusca = Estilo.campoTexto();
    private final Label mensagem = new Label(" ");

    /** @param aoNavegar recebe o nome do item clicado na barra ("Início", "Pesquisas", ..., "Sair"). */
    public TelaEntrevistadores(ColetaServico coleta, Consumer<String> aoNavegar) {
        this.coleta = coleta;

        raiz.setStyle("-fx-background-color: " + Estilo.FUNDO + ";");
        raiz.setTop(new BarraNavegacao("Coleta", aoNavegar));
        raiz.setCenter(montarArea());

        campoBusca.setPromptText("Buscar por código ou nome");
        campoBusca.textProperty().addListener((obs, antigo, texto) -> filtrar(texto));
        recarregar();
    }

    /** Conteúdo da tela, para colocar numa Scene. */
    public Parent getRoot() {
        return raiz;
    }

    /** Mostra a tela numa janela 1280x720. */
    public void abrir(Stage stage) {
        stage.setTitle("Entrevistadores");
        stage.setScene(new Scene(raiz, 1280, 720));
        stage.show();
    }

    // =====================================================================
    // Layout
    // =====================================================================

    private ScrollPane montarArea() {
        Label titulo = new Label("Entrevistadores");
        titulo.setStyle(Estilo.fonte(22, true) + "-fx-text-fill: " + Estilo.TITULO_CLARO + ";");

        HBox cartoes = new HBox(20, montarFormulario(), montarLista());
        VBox coluna = new VBox(18, titulo, cartoes, mensagem);
        coluna.setPrefWidth(900);
        coluna.setMaxWidth(900);

        mensagem.setWrapText(true);
        mensagem.setStyle(Estilo.fonte(13, false));
        mensagem.setTextFill(Color.web(Estilo.ERRO));

        HBox fundo = new HBox(coluna);
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

    private VBox montarFormulario() {
        Button cadastrar = Estilo.botaoPrincipal("Cadastrar");
        cadastrar.setOnAction(e -> cadastrar());
        campoNome.setOnAction(e -> cadastrar());   // Enter no campo Nome também cadastra

        Label aviso = new Label("O código não pode ser alterado após o cadastro.");
        aviso.setWrapText(true);
        aviso.setStyle(Estilo.fonte(12, false) + "-fx-text-fill: #6B7280;");

        VBox corpo = new VBox(6,
                Estilo.rotulo("Código"), campoCodigo,
                espaco(8), Estilo.rotulo("Nome"), campoNome,
                espaco(10), aviso,
                espaco(10), cadastrar);
        corpo.setPadding(new Insets(22, 22, 22, 22));
        cadastrar.setMaxWidth(Double.MAX_VALUE);

        VBox cartao = new VBox(Estilo.cabecalhoCartao("Novo Entrevistador", 15, 52), corpo);
        cartao.setPrefWidth(300);
        cartao.setMinWidth(300);
        cartao.setMaxWidth(300);
        cartao.setStyle("-fx-background-color: white; -fx-border-color: " + Estilo.BORDA + ";");
        cartao.setAlignment(Pos.TOP_CENTER);
        return cartao;
    }

    private VBox montarLista() {
        configurarTabela();

        VBox corpo = new VBox(12, campoBusca, tabela);
        corpo.setPadding(new Insets(18));
        VBox.setVgrow(tabela, Priority.ALWAYS);

        VBox cartao = new VBox(Estilo.cabecalhoCartao("Entrevistadores cadastrados", 15, 52), corpo);
        VBox.setVgrow(corpo, Priority.ALWAYS);
        HBox.setHgrow(cartao, Priority.ALWAYS);
        cartao.setStyle("-fx-background-color: white; -fx-border-color: " + Estilo.BORDA + ";");
        return cartao;
    }

    @SuppressWarnings("deprecation")
    private void configurarTabela() {
        TableColumn<Entrevistador, String> colCodigo = new TableColumn<>("Código");
        colCodigo.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getCodigo()));
        TableColumn<Entrevistador, String> colNome = new TableColumn<>("Nome");
        colNome.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getNome()));

        TableColumn<Entrevistador, Void> colAcoes = new TableColumn<>("Ações");
        colAcoes.setSortable(false);
        colAcoes.setCellFactory(coluna -> new TableCell<Entrevistador, Void>() {
            private final Button remover = Estilo.botaoSecundario("Remover");

            {
                remover.setPrefHeight(30);
                remover.setMinHeight(30);
                remover.setOnAction(e -> remover(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean vazio) {
                super.updateItem(item, vazio);
                setGraphic(vazio ? null : remover);
            }
        });

        tabela.getColumns().add(colCodigo);
        tabela.getColumns().add(colNome);
        tabela.getColumns().add(colAcoes);
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabela.setPlaceholder(new Label("Nenhum entrevistador encontrado."));
        tabela.setStyle(Estilo.fonte(13, false));
        tabela.setMinHeight(300);
    }

    private static VBox espaco(double altura) {
        VBox v = new VBox();
        v.setMinHeight(altura);
        v.setPrefHeight(altura);
        return v;
    }

    // =====================================================================
    // Ações
    // =====================================================================

    private void recarregar() {
        try {
            todos.setAll(coleta.listarEntrevistadores());
        } catch (ExcecaoRegraNegocio e) {
            mostrarErro(e.getMessage());
        }
    }

    private void filtrar(String texto) {
        String busca = texto == null ? "" : texto.trim().toLowerCase();
        filtrados.setPredicate(e -> busca.isEmpty()
                || e.getCodigo().toLowerCase().contains(busca)
                || e.getNome().toLowerCase().contains(busca));
    }

    private void cadastrar() {
        limparMensagem();
        try {
            Entrevistador novo = coleta.cadastrarEntrevistador(campoCodigo.getText(), campoNome.getText());
            todos.add(novo);
            campoCodigo.clear();
            campoNome.clear();
            campoCodigo.requestFocus();
            mostrarSucesso("Entrevistador " + novo.getCodigo() + " cadastrado com sucesso.");
        } catch (ExcecaoRegraNegocio e) {
            mostrarErro(e.getMessage());   // campo vazio ou código repetido
        }
    }

    private void remover(Entrevistador entrevistador) {
        limparMensagem();
        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION,
                "Remover o entrevistador " + entrevistador.getCodigo() + " – " + entrevistador.getNome() + "?",
                ButtonType.YES, ButtonType.NO);
        confirmacao.setTitle("Remover entrevistador");
        confirmacao.setHeaderText(null);
        Optional<ButtonType> resposta = confirmacao.showAndWait();
        if (resposta.isEmpty() || resposta.get() != ButtonType.YES) return;
        try {
            coleta.removerEntrevistador(entrevistador.getCodigo());
            todos.remove(entrevistador);
            mostrarSucesso("Entrevistador removido.");
        } catch (ExcecaoRegraNegocio e) {
            mostrarErro(e.getMessage());   // ex.: o entrevistador possui entrevistas
        }
    }

    // =====================================================================
    // Mensagens
    // =====================================================================

    private void mostrarErro(String texto) {
        mensagem.setTextFill(Color.web(Estilo.ERRO));
        mensagem.setText(texto);
    }

    private void mostrarSucesso(String texto) {
        mensagem.setTextFill(Color.web(Estilo.ARDOSIA));
        mensagem.setText(texto);
    }

    private void limparMensagem() {
        mensagem.setText(" ");
    }
}