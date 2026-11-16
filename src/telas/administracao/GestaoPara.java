package telas.administracao;

import dominio.administracao.CategoriaParametro;
import dominio.administracao.FaixaEtaria;
import dominio.administracao.FaixaRenda;
import dominio.administracao.Parametro;
import excecoes.ExcecaoRegraNegocio;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import servicos.administracao.ParametroServico;
import telas.Alertas;
import telas.Contexto;
import telas.Tela;

import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GestaoPara implements Tela {

    /** Cada aba do mockup: textos da tela e se a categoria tem limites (mínimo/máximo). */
    private enum Aba {
        FAIXA_ETARIA("Faixa Etária", CategoriaParametro.FAIXA_ETARIA, true,
                "Nova faixa etária", "Editar faixa etária",
                "faixa etária cadastrada", "faixas etárias cadastradas"),
        ESCOLARIDADE("Escolaridade", CategoriaParametro.ESCOLARIDADE, false,
                "Nova escolaridade", "Editar escolaridade",
                "escolaridade cadastrada", "escolaridades cadastradas"),
        REGIAO("Região", CategoriaParametro.REGIAO, false,
                "Nova região", "Editar região",
                "região cadastrada", "regiões cadastradas"),
        FAIXA_RENDA("Faixa de Renda", CategoriaParametro.FAIXA_RENDA, true,
                "Nova faixa de renda", "Editar faixa de renda",
                "faixa de renda cadastrada", "faixas de renda cadastradas"),
        TIPO_PESQUISA("Tipo de Pesquisa", CategoriaParametro.TIPO_PESQUISA, false,
                "Novo tipo de pesquisa", "Editar tipo de pesquisa",
                "tipo de pesquisa cadastrado", "tipos de pesquisa cadastrados");

        final String rotulo;
        final CategoriaParametro categoria;
        final boolean usaLimites;
        final String tituloNovo, tituloEditar, rodapeSingular, rodapePlural;

        Aba(String rotulo, CategoriaParametro categoria, boolean usaLimites,
            String tituloNovo, String tituloEditar, String rodapeSingular, String rodapePlural) {
            this.rotulo = rotulo;
            this.categoria = categoria;
            this.usaLimites = usaLimites;
            this.tituloNovo = tituloNovo;
            this.tituloEditar = tituloEditar;
            this.rodapeSingular = rodapeSingular;
            this.rodapePlural = rodapePlural;
        }
    }

    private final ParametroServico servico = Contexto.parametros;

    private final TableView<Parametro> tabela = new TableView<>();
    private final Label rodapeTabela = new Label();

    private final Label tituloForm = new Label();
    private final TextField campoDescricao = new TextField();
    private final TextField campoMinimo = new TextField();
    private final TextField campoMaximo = new TextField();
    private final VBox blocoMinimo = new VBox(4);
    private final VBox blocoMaximo = new VBox(4);
    private final Label mensagemErro = new Label();

    private Aba abaAtual;
    private Parametro emEdicao;            // null = cadastrando um novo


    @Override
    public Parent getRoot() {
        Label titulo = new Label("Parâmetros Gerais");
        titulo.setStyle("-fx-font-weight: bold; -fx-font-size: 16px ");
        HBox linhaTitulo = new HBox(titulo);
        linhaTitulo.setAlignment(Pos.CENTER);

        Label cabecalho = new Label("Parâmetros Gerais");
        cabecalho.setMaxWidth(Double.MAX_VALUE);

        ToggleGroup grupo = new ToggleGroup();
        HBox barraAbas = new HBox(6);
        for (Aba a : Aba.values()) {
            ToggleButton botao = new ToggleButton(a.rotulo);
            botao.setToggleGroup(grupo);
            botao.setUserData(a);
            barraAbas.getChildren().add(botao);
        }

        HBox conteudo = new HBox(0, criarLadoTabela(), criarFormulario());
        conteudo.setPadding(new Insets(25));

        VBox cartao = new VBox(cabecalho, barraAbas, conteudo);

        VBox raiz = new VBox(18, linhaTitulo, cartao);
        raiz.setPadding(new Insets(25, 30, 25, 30));

        // Ligação feita por último, com a tela já montada
        grupo.selectedToggleProperty().addListener((obs, antigo, novo) -> {
            if (novo == null) {                      // não deixa ficar sem aba selecionada
                grupo.selectToggle(antigo);
                return;
            }
            mudarAba((Aba) novo.getUserData());
        });
        grupo.selectToggle(grupo.getToggles().get(0));
        return raiz;
    }

    private VBox criarLadoTabela() {
        TableColumn<Parametro, Void> cCodigo = new TableColumn<>("Código");
        cCodigo.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Void v, boolean vazio) {
                super.updateItem(v, vazio);
                setText(vazio ? null : String.format("%03d", getIndex() + 1));   // posição na lista
            }
        });
        cCodigo.setPrefWidth(90);

        TableColumn<Parametro, String> cDescricao = new TableColumn<>("Descrição");
        cDescricao.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDescricao()));
        cDescricao.setPrefWidth(300);

        TableColumn<Parametro, Void> cAcoes = new TableColumn<>("Ações");
        cAcoes.setCellFactory(col -> new TableCell<>() {
            private final Button btnEditar = new Button("Editar");
            private final Button btnExcluir = new Button("Excluir");
            private final HBox caixa = new HBox(6, btnEditar, btnExcluir);

            {
                btnEditar.setOnAction(e -> editar(getTableRow().getItem()));
                btnExcluir.setOnAction(e -> excluir(getTableRow().getItem()));
            }

            @Override
            protected void updateItem(Void v, boolean vazio) {
                super.updateItem(v, vazio);
                boolean semLinha = vazio || getTableRow() == null || getTableRow().getItem() == null;
                setGraphic(semLinha ? null : caixa);
            }
        });
        cAcoes.setPrefWidth(190);

        tabela.getColumns().addAll(cCodigo, cDescricao, cAcoes);
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabela.setFixedCellSize(48);
        tabela.setPrefHeight(42 + 6 * 48);
        tabela.setPlaceholder(new Label("Nenhum parâmetro cadastrado."));

        rodapeTabela.setMaxWidth(Double.MAX_VALUE);

        VBox caixa = new VBox(tabela, rodapeTabela);
        HBox.setHgrow(caixa, Priority.ALWAYS);
        return caixa;
    }

    private VBox criarFormulario() {
        campoDescricao.setPrefWidth(360);
        blocoMinimo.getChildren().addAll(rotulo("Valor mínimo"), campoMinimo);
        blocoMaximo.getChildren().addAll(rotulo("Valor máximo"), campoMaximo);

        Button salvar = new Button("Salvar");
        salvar.setOnAction(e -> salvar());

        Button limpar = new Button("Limpar");
        limpar.setOnAction(e -> limparFormulario());

        mensagemErro.setWrapText(true);
        mensagemErro.setMaxWidth(360);

        VBox form = new VBox(14, tituloForm,
                new VBox(4, rotulo("Descrição"), campoDescricao),
                blocoMinimo, blocoMaximo,
                new HBox(12, salvar, limpar),
                mensagemErro);
        form.setPrefWidth(420);
        form.setMinWidth(420);
        HBox.setMargin(form, new Insets(0, 0, 0, 28));
        return form;
    }

    private Label rotulo(String texto) {
        Label l = new Label(texto);
        return l;
    }

    // ---------- comportamento ----------

    private void mudarAba(Aba aba) {
        if (aba == abaAtual) return;
        abaAtual = aba;

        blocoMinimo.setVisible(aba.usaLimites);
        blocoMinimo.setManaged(aba.usaLimites);
        blocoMaximo.setVisible(aba.usaLimites);
        blocoMaximo.setManaged(aba.usaLimites);
        campoDescricao.setDisable(aba.usaLimites);     // nas faixas, a descrição é gerada pelos limites
        campoDescricao.setPromptText(aba.usaLimites ? "Gerada automaticamente a partir dos valores" : "");
        if (aba == Aba.FAIXA_ETARIA) {
            campoMinimo.setPromptText("Idade mínima, ex.: 18");
            campoMaximo.setPromptText("Idade máxima (vazio = sem limite)");
        } else if (aba == Aba.FAIXA_RENDA) {
            campoMinimo.setPromptText("Em salários mínimos, ex.: 0");
            campoMaximo.setPromptText("Em salários mínimos (vazio = sem limite)");
        }

        limparFormulario();
        Alertas.executar(this::carregar);
    }

    private void carregar() {
        List<Parametro> lista = new ArrayList<>(servico.listarPorCategoria(abaAtual.categoria));
        lista.sort(comparadorDaAba());
        tabela.setItems(FXCollections.observableArrayList(lista));
        int n = lista.size();
        rodapeTabela.setText(n + " " + (n == 1 ? abaAtual.rodapeSingular : abaAtual.rodapePlural));
    }

    private Comparator<Parametro> comparadorDaAba() {
        switch (abaAtual) {
            case FAIXA_ETARIA: return Comparator.comparingInt(p -> ((FaixaEtaria) p).getIdadeMinima());
            case FAIXA_RENDA:  return Comparator.comparingDouble(p -> ((FaixaRenda) p).getRendaMinima());
            default:           return Comparator.comparing(Parametro::getDescricao, String.CASE_INSENSITIVE_ORDER);
        }
    }

    private void salvar() {
        executar(() -> {
            if (abaAtual == Aba.FAIXA_ETARIA) {
                int min = lerInteiro(campoMinimo.getText(), "mínimo", true);
                int max = campoMaximo.getText().isBlank()
                        ? FaixaEtaria.SEM_LIMITE : lerInteiro(campoMaximo.getText(), "máximo", false);
                if (emEdicao == null) servico.cadastrarFaixaEtaria(min, max);
                else servico.editarFaixaEtaria(emEdicao.getId(), min, max);

            } else if (abaAtual == Aba.FAIXA_RENDA) {
                double min = lerDecimal(campoMinimo.getText(), "mínimo", true);
                double max = campoMaximo.getText().isBlank()
                        ? FaixaRenda.SEM_LIMITE : lerDecimal(campoMaximo.getText(), "máximo", false);
                if (emEdicao == null) servico.cadastrarFaixaRenda(min, max);
                else servico.editarFaixaRenda(emEdicao.getId(), min, max);

            } else {
                if (emEdicao == null) servico.cadastrar(abaAtual.categoria, campoDescricao.getText());
                else servico.editarDescricao(emEdicao.getId(), campoDescricao.getText());
            }
            limparFormulario();
            carregar();
        });
    }

    private void editar(Parametro p) {
        if (p == null) return;
        mensagemErro.setText("");
        emEdicao = p;
        tituloForm.setText(abaAtual.tituloEditar);
        campoDescricao.setText(p.getDescricao());
        if (p instanceof FaixaEtaria) {
            FaixaEtaria f = (FaixaEtaria) p;
            campoMinimo.setText(String.valueOf(f.getIdadeMinima()));
            campoMaximo.setText(f.getIdadeMaxima() == FaixaEtaria.SEM_LIMITE
                    ? "" : String.valueOf(f.getIdadeMaxima()));
        } else if (p instanceof FaixaRenda) {
            FaixaRenda f = (FaixaRenda) p;
            campoMinimo.setText(formatar(f.getRendaMinima()));
            campoMaximo.setText(f.getRendaMaxima() == FaixaRenda.SEM_LIMITE
                    ? "" : formatar(f.getRendaMaxima()));
        }
    }

    private void excluir(Parametro p) {
        if (p == null) return;
        if (!Alertas.confirmar("Excluir definitivamente \"" + p.getDescricao() + "\"?")) return;
        executar(() -> {
            servico.excluir(p.getId());            // recusa se estiver em uso
            if (emEdicao != null && emEdicao.getId().equals(p.getId())) limparFormulario();
            carregar();
        });
    }

    private void limparFormulario() {
        emEdicao = null;
        tituloForm.setText(abaAtual.tituloNovo);
        campoDescricao.clear();
        campoMinimo.clear();
        campoMaximo.clear();
        mensagemErro.setText("");
    }

    /** Erros de regra aparecem em vermelho abaixo do formulário, como no mockup. */
    private void executar(Runnable acao) {
        mensagemErro.setText("");
        try {
            acao.run();
        } catch (ExcecaoRegraNegocio e) {
            mensagemErro.setText(e.getMessage());
        }
    }

    // ---------- leitura de números ----------

    private int lerInteiro(String texto, String nome, boolean obrigatorio) {
        if (texto == null || texto.isBlank()) {
            throw new ExcecaoRegraNegocio("Informe o valor " + nome + ".");
        }
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            throw new ExcecaoRegraNegocio("O valor " + nome + " deve ser um número inteiro.");
        }
    }

    private double lerDecimal(String texto, String nome, boolean obrigatorio) {
        if (texto == null || texto.isBlank()) {
            throw new ExcecaoRegraNegocio("Informe o valor " + nome + ".");
        }
        try {
            return Double.parseDouble(texto.trim().replace(',', '.'));    // aceita vírgula
        } catch (NumberFormatException e) {
            throw new ExcecaoRegraNegocio("O valor " + nome + " deve ser um número.");
        }
    }

    private static String formatar(double v) {
        return v == Math.rint(v) ? String.valueOf((long) v) : String.valueOf(v).replace('.', ',');
    }
}
