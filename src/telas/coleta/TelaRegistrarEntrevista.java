package telas.coleta;

import telas.BarraNavegacao;
import telas.Estilo;
import dominio.administracao.CategoriaParametro;
import dominio.administracao.Escolaridade;
import dominio.administracao.FaixaEtaria;
import dominio.administracao.FaixaRenda;
import dominio.administracao.Parametro;
import dominio.administracao.Regiao;
import dominio.coleta.Entrevista;
import dominio.coleta.Entrevistador;
import dominio.coleta.Genero;
import dominio.coleta.Participante;
import dominio.coleta.TipoEspecial;
import dominio.pesquisas.OpcaoResposta;
import dominio.pesquisas.Pergunta;
import dominio.pesquisas.Pesquisa;
import dominio.pesquisas.SituPesquisa;
import excecoes.ExcecaoRegraNegocio;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import servicos.administracao.ParametroServico;
import servicos.coleta.ColetaServico;
import servicos.pesquisas.PesquisaService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Tela "Registrar Entrevista" (Pesquisador), em JavaFX. Reúne pesquisa, entrevistador, dados do
 * participante e as respostas do questionário, e usa o {@link ColetaServico} para criar,
 * responder e concluir a entrevista. A tela só coleta e mostra dados: todas as regras de
 * negócio continuam no serviço.
 */
public class TelaRegistrarEntrevista {
    private static final double LARGURA_CARTAO = 720;

    private final ColetaServico coleta;
    private final PesquisaService pesquisas;
    private final ParametroServico parametros;
    private final Consumer<String> aoNavegar;

    private final BorderPane raiz = new BorderPane();

    private final ComboBox<Pesquisa> comboPesquisa = Estilo.combo(p -> p.getCodigo() + " – " + p.getNome());
    private final ComboBox<Entrevistador> comboEntrevistador = Estilo.combo(e -> e.getCodigo() + " – " + e.getNome());
    private final TextField campoId = Estilo.campoTexto();
    private final TextField campoData = Estilo.campoTexto();

    private final TextField campoCodigoParticipante = Estilo.campoTexto();
    private final ComboBox<Genero> comboGenero = Estilo.combo(Genero::getDescricao);
    private final ComboBox<FaixaEtaria> comboFaixaEtaria = Estilo.combo(Parametro::getDescricao);
    private final ComboBox<Escolaridade> comboEscolaridade = Estilo.combo(Parametro::getDescricao);
    private final ComboBox<Regiao> comboRegiao = Estilo.combo(Parametro::getDescricao);
    private final ComboBox<FaixaRenda> comboFaixaRenda = Estilo.combo(Parametro::getDescricao);

    private final VBox painelPerguntas = new VBox(18);
    private final List<ToggleGroup> gruposPerguntas = new ArrayList<>();   // um grupo por pergunta, na ordem do questionário
    private final Label mensagem = new Label(" ");
    private final Button botaoConcluir = Estilo.botaoPrincipal("Concluir entrevista");
    private final Button botaoCancelar = Estilo.botaoSecundario("Cancelar");

    /** Entrevista já criada neste formulário ("PESQUISA|ID") mas ainda não concluída. */
    private String entrevistaEmAndamento;

    /** @param aoNavegar recebe o nome do item clicado na barra ("Início", "Pesquisas", ..., "Sair"). */
    public TelaRegistrarEntrevista(ColetaServico coleta, PesquisaService pesquisas,
                                   ParametroServico parametros, Consumer<String> aoNavegar) {
        this.coleta = coleta;
        this.pesquisas = pesquisas;
        this.parametros = parametros;
        this.aoNavegar = aoNavegar;

        raiz.setStyle("-fx-background-color: " + Estilo.FUNDO + ";");
        raiz.setTop(new BarraNavegacao("Coleta", aoNavegar));
        raiz.setCenter(montarAreaRolavel());

        carregarListas();
        campoData.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        carregarPerguntas();

        comboPesquisa.valueProperty().addListener((obs, antigo, novo) -> aoTrocarPesquisa());
        botaoConcluir.setOnAction(e -> concluir());
        botaoCancelar.setOnAction(e -> cancelar());
    }

    /** Conteúdo da tela, para colocar numa Scene. */
    public Parent getRoot() {
        return raiz;
    }

    /** Mostra a tela numa janela 1280x720. */
    public void abrir(Stage stage) {
        stage.setTitle("Registrar Entrevista");
        stage.setScene(new Scene(raiz, 1280, 720));
        stage.show();
    }

    // =====================================================================
    // Layout
    // =====================================================================

    private ScrollPane montarAreaRolavel() {
        HBox fundo = new HBox(montarCartao());
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

    private VBox montarCartao() {
        VBox corpo = new VBox(8);
        corpo.setPadding(new Insets(26, 32, 26, 32));
        corpo.setStyle("-fx-background-color: white;");
        corpo.getChildren().addAll(montarFormulario(), montarSecaoPerguntas(), montarRodape());

        VBox cartao = new VBox(Estilo.cabecalhoCartao("Registrar Entrevista", 17, 56), corpo);
        cartao.setPrefWidth(LARGURA_CARTAO);
        cartao.setMaxWidth(LARGURA_CARTAO);
        cartao.setStyle("-fx-background-color: white; -fx-border-color: " + Estilo.BORDA + ";");
        return cartao;
    }

    private GridPane montarFormulario() {
        GridPane form = new GridPane();
        form.setHgap(16);
        form.setVgap(14);
        ColumnConstraints rotulos = new ColumnConstraints(150);
        rotulos.setHalignment(HPos.RIGHT);
        ColumnConstraints campos = new ColumnConstraints();
        campos.setHgrow(Priority.ALWAYS);
        form.getColumnConstraints().addAll(rotulos, campos);

        int y = 0;
        linha(form, y++, "Pesquisa", comboPesquisa);
        linha(form, y++, "Entrevistador", montarEntrevistador());
        linha(form, y++, "Id da entrevista", campoId);
        campoData.setEditable(false);
        campoData.setFocusTraversable(false);
        campoData.setStyle(campoData.getStyle() + " -fx-background-color: #F9FAFB;");
        linha(form, y++, "Data", campoData);

        secao(form, y++, "Participante");
        linha(form, y++, "Código", campoCodigoParticipante);
        linha(form, y++, "Gênero", comboGenero);
        linha(form, y++, "Faixa etária", comboFaixaEtaria);
        linha(form, y++, "Escolaridade", comboEscolaridade);
        linha(form, y++, "Região", comboRegiao);
        linha(form, y++, "Faixa de renda", comboFaixaRenda);
        return form;
    }

    /** Lista de entrevistadores com um botão para cadastrar um novo sem sair da tela. */
    private HBox montarEntrevistador() {
        Button novo = Estilo.botaoSecundario("+ Novo");
        novo.setOnAction(e -> novoEntrevistador());
        HBox linha = new HBox(8, comboEntrevistador, novo);
        HBox.setHgrow(comboEntrevistador, Priority.ALWAYS);
        return linha;
    }

    private static void linha(GridPane form, int y, String texto, Node campo) {
        Label rotulo = Estilo.rotulo(texto);
        form.add(rotulo, 0, y);
        if (campo instanceof Region) {
            ((Region) campo).setMaxWidth(Double.MAX_VALUE);
        }
        form.add(campo, 1, y);
    }

    private static void secao(GridPane form, int y, String texto) {
        Label cab = cabecalhoSecao(texto);
        GridPane.setMargin(cab, new Insets(10, 0, 2, 0));
        form.add(cab, 0, y, 2, 1);
    }

    private static Label cabecalhoSecao(String texto) {
        Label l = new Label(texto);
        l.setMaxWidth(Double.MAX_VALUE);
        l.setStyle(Estilo.fonte(14, true) + "-fx-text-fill: " + Estilo.ARDOSIA + ";"
                + " -fx-border-color: transparent transparent " + Estilo.BORDA + " transparent;"
                + " -fx-padding: 0 0 6 0;");
        return l;
    }

    private VBox montarSecaoPerguntas() {
        painelPerguntas.setAlignment(Pos.TOP_LEFT);
        VBox secao = new VBox(14, cabecalhoSecao("Perguntas"), painelPerguntas);
        secao.setPadding(new Insets(8, 0, 0, 0));
        return secao;
    }

    private VBox montarRodape() {
        mensagem.setStyle(Estilo.fonte(13, false));
        mensagem.setWrapText(true);
        mensagem.setMaxWidth(560);
        mensagem.setTextAlignment(TextAlignment.CENTER);
        mensagem.setAlignment(Pos.CENTER);
        mensagem.setTextFill(Color.web(Estilo.ERRO));

        botaoConcluir.setPrefWidth(190);
        botaoCancelar.setPrefWidth(110);
        HBox botoes = new HBox(12, botaoConcluir, botaoCancelar);
        botoes.setAlignment(Pos.CENTER);

        VBox rodape = new VBox(12, mensagem, botoes);
        rodape.setAlignment(Pos.CENTER);
        rodape.setPadding(new Insets(14, 0, 0, 0));
        return rodape;
    }

    // =====================================================================
    // Dados das listas e das perguntas
    // =====================================================================

    /** Preenche as listas com os dados reais (pesquisas em coleta, entrevistadores e parâmetros). */
    private void carregarListas() {
        try {
            comboPesquisa.getItems().addAll(pesquisas.listarPesquisasPorSituacao(SituPesquisa.EM_COLETA));
            comboEntrevistador.getItems().addAll(coleta.listarEntrevistadores());
            comboGenero.getItems().addAll(Genero.values());
            for (Parametro p : parametros.listarPorCategoria(CategoriaParametro.FAIXA_ETARIA)) comboFaixaEtaria.getItems().add((FaixaEtaria) p);
            for (Parametro p : parametros.listarPorCategoria(CategoriaParametro.ESCOLARIDADE)) comboEscolaridade.getItems().add((Escolaridade) p);
            for (Parametro p : parametros.listarPorCategoria(CategoriaParametro.REGIAO)) comboRegiao.getItems().add((Regiao) p);
            for (Parametro p : parametros.listarPorCategoria(CategoriaParametro.FAIXA_RENDA)) comboFaixaRenda.getItems().add((FaixaRenda) p);
        } catch (ExcecaoRegraNegocio e) {
            mostrarErro(e.getMessage());
        }
        limparSelecoes();

        if (comboPesquisa.getItems().isEmpty()) {
            mostrarErro("Não há pesquisas em coleta. Mude a situação de uma pesquisa para \"Em Coleta\".");
        } else if (comboFaixaEtaria.getItems().isEmpty() || comboEscolaridade.getItems().isEmpty()
                || comboRegiao.getItems().isEmpty() || comboFaixaRenda.getItems().isEmpty()) {
            mostrarErro("Cadastre faixas etárias, escolaridades, regiões e faixas de renda em Parâmetros Gerais.");
        }
    }

    /** Deixa todas as listas sem nada escolhido (mostram "Selecione..."). */
    private void limparSelecoes() {
        Estilo.limpar(comboPesquisa);
        Estilo.limpar(comboEntrevistador);
        limparParticipante();
    }

    private void limparParticipante() {
        campoCodigoParticipante.setText("");
        Estilo.limpar(comboGenero);
        Estilo.limpar(comboFaixaEtaria);
        Estilo.limpar(comboEscolaridade);
        Estilo.limpar(comboRegiao);
        Estilo.limpar(comboFaixaRenda);
    }

    private void aoTrocarPesquisa() {
        entrevistaEmAndamento = null;
        Pesquisa p = comboPesquisa.getValue();
        campoId.setText(p == null ? "" : sugerirId(p));
        carregarPerguntas();
        limparMensagem();
    }

    /** Sugere o próximo id livre (E001, E002...) para a pesquisa; o usuário pode trocar. */
    private String sugerirId(Pesquisa pesquisa) {
        Set<String> usados = coleta.listarEntrevistas(pesquisa.getCodigo()).stream()
                .map(Entrevista::getId).collect(Collectors.toSet());
        int n = usados.size() + 1;
        String id;
        do {
            id = String.format("E%03d", n++);
        } while (usados.contains(id));
        return id;
    }

    /** Monta as perguntas da pesquisa escolhida: opções do questionário + 3 respostas especiais. */
    private void carregarPerguntas() {
        painelPerguntas.getChildren().clear();
        gruposPerguntas.clear();

        Pesquisa pesquisa = comboPesquisa.getValue();
        if (pesquisa == null) {
            painelPerguntas.getChildren().add(textoCinza("Selecione uma pesquisa para ver as perguntas."));
        } else if (pesquisa.getPerguntas().isEmpty()) {
            painelPerguntas.getChildren().add(textoCinza("Esta pesquisa ainda não tem perguntas cadastradas."));
        } else {
            List<Pergunta> perguntas = pesquisa.getPerguntas();
            for (int i = 0; i < perguntas.size(); i++) {
                painelPerguntas.getChildren().add(montarPergunta(i + 1, perguntas.get(i)));
            }
        }
    }

    private static Label textoCinza(String texto) {
        Label l = new Label(texto);
        l.setStyle(Estilo.fonte(13, false) + "-fx-text-fill: " + Estilo.TITULO_CLARO + ";");
        return l;
    }

    private VBox montarPergunta(int numero, Pergunta pergunta) {
        Label enunciado = new Label(numero + ". " + pergunta.getTexto() + (pergunta.isObrigatoria() ? " *" : ""));
        enunciado.setWrapText(true);
        enunciado.setStyle(Estilo.fonte(13, true) + "-fx-text-fill: " + Estilo.TEXTO + ";");

        VBox bloco = new VBox(6, enunciado);
        ToggleGroup grupo = new ToggleGroup();
        for (OpcaoResposta opcao : pergunta.getOpcoesResposta()) {
            bloco.getChildren().add(radio(opcao.getTexto(), Estilo.TEXTO, opcao.getTexto(), grupo));
        }

        // respostas especiais, na mesma linha e em cinza, para não se confundirem com as opções
        String cinza = "#6B7280";
        HBox especiais = new HBox(14,
                radio("Não sabe", cinza, TipoEspecial.NAO_SABE, grupo),
                radio("Não respondeu", cinza, TipoEspecial.NAO_RESPONDEU, grupo),
                radio("Nenhuma das opções", cinza, TipoEspecial.NENHUMA_DAS_OPCOES, grupo));
        especiais.setPadding(new Insets(4, 0, 0, 0));
        bloco.getChildren().add(especiais);

        gruposPerguntas.add(grupo);
        return bloco;
    }

    /** Botão de rádio que guarda em "userData" o texto da opção ou o TipoEspecial. */
    private static RadioButton radio(String texto, String cor, Object valor, ToggleGroup grupo) {
        RadioButton r = new RadioButton(texto);
        r.setToggleGroup(grupo);
        r.setUserData(valor);
        r.setCursor(Cursor.HAND);
        r.setStyle(Estilo.fonte(13, false) + "-fx-text-fill: " + cor + ";");
        return r;
    }

    /** Valor marcado na pergunta (texto da opção ou TipoEspecial); null se nada foi marcado. */
    private static Object valorMarcado(ToggleGroup grupo) {
        Toggle t = grupo.getSelectedToggle();
        return t == null ? null : t.getUserData();
    }

    // =====================================================================
    // Ações
    // =====================================================================

    private void concluir() {
        limparMensagem();
        Pesquisa pesquisa = comboPesquisa.getValue();
        Entrevistador entrevistador = comboEntrevistador.getValue();
        String id = campoId.getText().trim();

        // conferências simples feitas antes de gravar qualquer coisa
        if (pesquisa == null) { mostrarErro("Selecione a pesquisa."); return; }
        if (entrevistador == null) { mostrarErro("Selecione o entrevistador."); return; }
        if (id.isEmpty()) { mostrarErro("Informe o id da entrevista."); return; }
        if (campoCodigoParticipante.getText().isBlank() || comboGenero.getValue() == null
                || comboFaixaEtaria.getValue() == null || comboEscolaridade.getValue() == null
                || comboRegiao.getValue() == null || comboFaixaRenda.getValue() == null) {
            mostrarErro("Preencha o código e todas as características do participante.");
            return;
        }
        List<Pergunta> perguntas = pesquisa.getPerguntas();
        for (int i = 0; i < perguntas.size(); i++) {
            if (perguntas.get(i).isObrigatoria() && valorMarcado(gruposPerguntas.get(i)) == null) {
                mostrarErro("Responda todas as perguntas obrigatórias antes de concluir (pergunta " + (i + 1) + ").");
                return;
            }
        }

        try {
            String codigo = pesquisa.getCodigo();
            String chave = codigo.toUpperCase() + "|" + id.toUpperCase();

            // se uma tentativa anterior já criou a entrevista (e falhou depois), continua nela
            if (!chave.equals(entrevistaEmAndamento)) {
                Participante participante = coleta.criarParticipante(
                        campoCodigoParticipante.getText(),
                        comboGenero.getValue(),
                        comboFaixaEtaria.getValue(),
                        comboEscolaridade.getValue(),
                        comboRegiao.getValue(),
                        comboFaixaRenda.getValue());
                coleta.iniciarEntrevista(codigo, id, participante, entrevistador.getCodigo());
                entrevistaEmAndamento = chave;
            }

            Entrevista criada = coleta.listarEntrevistas(codigo).stream()
                    .filter(e -> e.getId().equalsIgnoreCase(id)).findFirst().orElse(null);
            for (int i = 0; i < perguntas.size(); i++) {
                Object valor = valorMarcado(gruposPerguntas.get(i));
                if (valor == null || (criada != null && criada.temRespostaPara(perguntas.get(i)))) continue;
                if (valor instanceof TipoEspecial) {
                    coleta.responderEspecial(codigo, id, i, (TipoEspecial) valor);
                } else {
                    coleta.responder(codigo, id, i, (String) valor);
                }
            }
            coleta.concluirEntrevista(codigo, id);

            entrevistaEmAndamento = null;
            mostrarSucesso("Entrevista " + id.toUpperCase() + " concluída com sucesso.");
            prepararProximaEntrevista(pesquisa);
        } catch (ExcecaoRegraNegocio e) {
            mostrarErro(e.getMessage());   // ex.: participante já entrevistado, quantidade prevista atingida
        }
    }

    /** Depois de concluir: mantém pesquisa e entrevistador e limpa o resto para a próxima. */
    private void prepararProximaEntrevista(Pesquisa pesquisa) {
        campoId.setText(sugerirId(pesquisa));
        limparParticipante();
        carregarPerguntas();
    }

    private void cancelar() {
        entrevistaEmAndamento = null;
        limparSelecoes();
        campoId.setText("");
        carregarPerguntas();
        limparMensagem();
        aoNavegar.accept("Início");
    }

    private void novoEntrevistador() {
        TextField codigo = Estilo.campoTexto();
        TextField nome = Estilo.campoTexto();
        VBox conteudo = new VBox(6, Estilo.rotulo("Código"), codigo, Estilo.rotulo("Nome"), nome);
        conteudo.setPadding(new Insets(16));
        conteudo.setPrefWidth(300);

        Dialog<ButtonType> dialogo = new Dialog<>();
        dialogo.setTitle("Novo entrevistador");
        dialogo.getDialogPane().setContent(conteudo);
        dialogo.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> resposta = dialogo.showAndWait();
        if (resposta.isEmpty() || resposta.get() != ButtonType.OK) return;
        try {
            Entrevistador novo = coleta.cadastrarEntrevistador(codigo.getText(), nome.getText());
            comboEntrevistador.getItems().add(novo);
            comboEntrevistador.setValue(novo);
            limparMensagem();
        } catch (ExcecaoRegraNegocio e) {
            mostrarErro(e.getMessage());
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