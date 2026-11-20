package telas.administracao;

import dominio.administracao.Perfil;
import dominio.administracao.Usuario;
import dominio.administracao.ValidadorUsuario;
import excecoes.ExcecaoRegraNegocio;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import servicos.administracao.UsuarioServico;
import telas.Alertas;
import telas.Contexto;
import telas.Tela;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class GestaoUser implements Tela {
    private static final int POR_PAGINA = 6;

    private final UsuarioServico servico = Contexto.usuarios;

    private final TextField campoBusca = new TextField();
    private final ComboBox<String> comboPerfil = new ComboBox<>();
    private final ComboBox<String> comboStatus = new ComboBox<>();
    private final TableView<Usuario> tabela = new TableView<>();
    private final Label resumo = new Label();
    private final HBox paginacao = new HBox(6);

    private List<Usuario> filtrados = new ArrayList<>();
    private int pagina = 0;


    @Override
    public Parent getRoot() {
        Label titulo = new Label("Gestão de Usuários");
        titulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        HBox linhaTitulo = new HBox(titulo);
        linhaTitulo.setAlignment(Pos.CENTER);


        campoBusca.setPromptText("Buscar por nome ou e-mail");
        campoBusca.setPrefWidth(390);

        comboPerfil.getItems().add("Todos os perfis");

        for (Perfil p : Perfil.values()){
            comboPerfil.getItems().add(nomeAmigavel(p));
        }

        comboPerfil.getSelectionModel().selectFirst();
        comboPerfil.setPrefWidth(208);

        comboStatus.getItems().addAll("Todos os status", "Ativos", "Inativos");
        comboStatus.getSelectionModel().selectFirst();
        comboStatus.setPrefWidth(180);

        Button novo = new Button("Novo Usuário");
        novo.setOnAction(e -> abrirDialogo(null));

        Region espaco = new Region();
        HBox.setHgrow(espaco, Priority.ALWAYS);
        HBox filtros = new HBox(15,
                campo("Buscar usuário", campoBusca),
                campo("Perfil", comboPerfil),
                campo("Status", comboStatus),
                espaco, novo);
        filtros.setAlignment(Pos.BOTTOM_LEFT);

        Label cabecalho = new Label("Usuários cadastrados");
        cabecalho.setMaxWidth(Double.MAX_VALUE);

        VBox cartao = new VBox(cabecalho, criarTabela(), criarRodape());

        VBox raiz = new VBox(18, linhaTitulo, filtros, cartao);
        raiz.setPadding(new Insets(25, 30, 25, 30));

        // ligações feitas por último, para a tela já estar montada quando disparam
        campoBusca.textProperty().addListener((o, a, n) -> filtrar());
        comboPerfil.setOnAction(e -> filtrar());
        comboStatus.setOnAction(e -> filtrar());

        Alertas.executar(this::carregar);
        return raiz;
    }

    private VBox campo(String rotulo, Control controle) {
        Label l = new Label(rotulo);
        return new VBox(4, l, controle);
    }

    private TableView<Usuario> criarTabela() {
        TableColumn<Usuario, String> cNome = new TableColumn<>("Nome");
        cNome.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNome()));
        cNome.setPrefWidth(200);

        TableColumn<Usuario, String> cEmail = new TableColumn<>("E-mail");
        cEmail.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEmail()));
        cEmail.setPrefWidth(280);

        TableColumn<Usuario, String> cPerfil = new TableColumn<>("Perfil");
        cPerfil.setCellValueFactory(c -> new SimpleStringProperty(formatarPerfis(c.getValue().getPerfis())));
        cPerfil.setPrefWidth(160);

        TableColumn<Usuario, Boolean> cStatus = new TableColumn<>("Status");
        cStatus.setCellValueFactory(c -> new SimpleBooleanProperty(c.getValue().getAtivo()));
        cStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean ativo, boolean vazio) {
                super.updateItem(ativo, vazio);
                if (vazio || ativo == null) {
                    setGraphic(null);
                    return;
                }
                Label badge = new Label(ativo ? "Ativo" : "Inativo");
                setGraphic(badge);
            }
        });
        cStatus.setPrefWidth(110);

        TableColumn<Usuario, Void> cAcoes = new TableColumn<>("Ações");
        cAcoes.setCellFactory(col -> new TableCell<>() {
            private final Button btnEditar = new Button("Editar");
            private final Button btnAlternar = new Button();
            private final Button btnExcluir = new Button("Excluir");
            private final HBox caixa = new HBox(6, btnEditar, btnAlternar, btnExcluir);

            {
                btnEditar.setOnAction(e -> abrirDialogo(usuarioDaLinha()));
                btnAlternar.setOnAction(e -> Alertas.executar(() -> alternarStatus(usuarioDaLinha())));
                btnExcluir.setOnAction(e -> Alertas.executar(() -> excluir(usuarioDaLinha())));
            }

            private Usuario usuarioDaLinha() {
                return getTableRow().getItem();
            }

            @Override
            protected void updateItem(Void v, boolean vazio) {
                super.updateItem(v, vazio);
                Usuario u = (vazio || getTableRow() == null) ? null : getTableRow().getItem();
                if (u == null) {
                    setGraphic(null);
                    return;
                }
                btnAlternar.setText(u.getAtivo() ? "Desativar" : "Ativar");
                setGraphic(caixa);
            }
        });
        cAcoes.setPrefWidth(270);

        tabela.getColumns().addAll(cNome, cEmail, cPerfil, cStatus, cAcoes);
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabela.setFixedCellSize(48);
        tabela.setPrefHeight(42 + POR_PAGINA * 48);
        tabela.setPlaceholder(new Label("Nenhum usuário encontrado."));
        return tabela;
    }

    private HBox criarRodape() {
        Region espaco = new Region();
        HBox.setHgrow(espaco, Priority.ALWAYS);
        HBox rodape = new HBox(resumo, espaco, paginacao);
        rodape.setAlignment(Pos.CENTER_LEFT);
        return rodape;
    }

    //ddados e paginação

    private void filtrar() {           //pra voltar pra 1 pag
        pagina = 0;
        Alertas.executar(this::carregar);
    }

    private void carregar() {                     //mostra pag atual
        filtrados = new ArrayList<>(servico.pesquisar(campoBusca.getText(), perfilSelecionado(), statusSelecionado()));
        filtrados.sort(Comparator.comparing(Usuario::getNome, String.CASE_INSENSITIVE_ORDER));
        pagina = Math.min(pagina, totalPaginas() - 1);
        mostrarPagina();
    }

    private void mostrarPagina() {
        int de = pagina * POR_PAGINA;
        int ate = Math.min(filtrados.size(), de + POR_PAGINA);
        tabela.setItems(FXCollections.observableArrayList(filtrados.subList(de, ate)));
        atualizarRodape();
    }

    private int totalPaginas() {
        return Math.max(1, (int) Math.ceil(filtrados.size() / (double) POR_PAGINA));
    }

    private void atualizarRodape() {
        int total = filtrados.size();
        int inicio = total == 0 ? 0 : pagina * POR_PAGINA + 1;
        int fim = Math.min(total, (pagina + 1) * POR_PAGINA);
        resumo.setText("Exibindo " + inicio + "–" + fim + " de " + total + " usuários");

        paginacao.getChildren().clear();
        Button anterior = new Button("Anterior");
        anterior.setDisable(pagina == 0);
        anterior.setOnAction(e -> irParaPagina(pagina - 1));
        paginacao.getChildren().add(anterior);

        for (int i = 0; i < totalPaginas(); i++) {
            final int alvo = i;
            Button b = new Button(String.valueOf(i + 1));
            b.setOnAction(e -> irParaPagina(alvo));
            paginacao.getChildren().add(b);
        }

        Button proxima = new Button("Próxima");
        proxima.setDisable(pagina >= totalPaginas() - 1);
        proxima.setStyle("-fx-cursor: hand");
        proxima.setOnAction(e -> irParaPagina(pagina + 1));
        paginacao.getChildren().add(proxima);
    }

    private void irParaPagina(int alvo) {
        pagina = alvo;
        mostrarPagina();
    }

    private Perfil perfilSelecionado() {
        int i = comboPerfil.getSelectionModel().getSelectedIndex();
        return i <= 0 ? null : Perfil.values()[i - 1];      // 0 = "Todos os perfis"
    }

    private Boolean statusSelecionado() {
        switch (comboStatus.getSelectionModel().getSelectedIndex()) {
            case 1:  return true;
            case 2:  return false;
            default: return null;                            // "Todos os status"
        }
    }

    private void alternarStatus(Usuario u) {
        if (u.getAtivo()) servico.desativar(u.getId()); else servico.ativar(u.getId());
        carregar();
    }

    private void excluir(Usuario u) {
        if (Alertas.confirmar("Excluir definitivamente o usuário " + u.getNome() + "?")) {
            servico.excluir(u.getId());
            carregar();
        }
    }

   // existente == null -> novo usuário. se não -> edição
    private void abrirDialogo(Usuario existente) {
        Dialog<ButtonType> dialogo = new Dialog<>();
        dialogo.setTitle(existente == null ? "Novo usuário" : "Editar usuário");
        ButtonType salvarTipo = new ButtonType("Salvar", ButtonBar.ButtonData.OK_DONE);
        dialogo.getDialogPane().getButtonTypes().addAll(salvarTipo, ButtonType.CANCEL);

        TextField nome = new TextField(existente == null ? "" : existente.getNome());
        TextField email = new TextField(existente == null ? "" : existente.getEmail());
        PasswordField senha = new PasswordField();
        senha.setPromptText(existente == null ? "Mínimo 8 caracteres, com letras e números" : "Deixe vazio para manter a atual");

        Map<Perfil, CheckBox> checks = new EnumMap<>(Perfil.class);
        VBox caixaPerfis = new VBox(4);
        for (Perfil p : Perfil.values()) {
            CheckBox c = new CheckBox(nomeAmigavel(p));
            c.setSelected(existente != null && existente.temPerfil(p));
            checks.put(p, c);
            caixaPerfis.getChildren().add(c);
        }

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(15));
        form.addRow(0, new Label("Nome:"), nome);
        form.addRow(1, new Label("E-mail:"), email);
        form.addRow(2, new Label("Senha:"), senha);
        form.addRow(3, new Label("Perfis:"), caixaPerfis);
        nome.setPrefWidth(320);
        dialogo.getDialogPane().setContent(form);

        Button botaoSalvar = (Button) dialogo.getDialogPane().lookupButton(salvarTipo);
        botaoSalvar.addEventFilter(ActionEvent.ACTION, ev -> {
            try {
                Set<Perfil> perfis = EnumSet.noneOf(Perfil.class);
                checks.forEach((p, c) -> { if (c.isSelected()) perfis.add(p); });

                if (existente == null) {
                    servico.cadastrar(nome.getText(), email.getText(), senha.getText(), perfis);
                } else {
                    if (!senha.getText().isBlank()) ValidadorUsuario.validarSenha(senha.getText()); // antes de alterar
                    servico.editar(existente.getId(), nome.getText(), email.getText(), perfis);
                    if (!senha.getText().isBlank()) servico.alterarSenha(existente.getId(), senha.getText());
                }
            } catch (ExcecaoRegraNegocio e) {
                Alertas.erro(e.getMessage());
                ev.consume();
            }
        });

        dialogo.showAndWait()
                .filter(b -> b == salvarTipo)
                .ifPresent(b -> Alertas.executar(this::carregar));
    }

    private static String nomeAmigavel(Perfil p) {
        String n = p.name();
        return n.charAt(0) + n.substring(1).toLowerCase();    // ADMINISTRADOR -> Administrador
    }

    private static String formatarPerfis(Set<Perfil> perfis) {
        return perfis.stream().map(GestaoUser::nomeAmigavel).collect(Collectors.joining(", "));
    }
}