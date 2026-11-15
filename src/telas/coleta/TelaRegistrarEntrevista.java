package telas.coleta;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

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
import servicos.administracao.ParametroServico;
import servicos.coleta.ColetaServico;
import servicos.pesquisas.PesquisaService;

/**
 * Tela "Registrar Entrevista" (módulo Coleta e Amostra).
 *
 * Fluxo: escolher pesquisa e entrevistador, informar as características do participante
 * (o participante é cadastrado aqui mesmo, junto com a entrevista), responder as perguntas
 * e concluir. "Salvar rascunho" grava a entrevista aberta; ao clicar de novo em Concluir
 * a mesma entrevista é retomada (não se cria outra).
 */
public class TelaRegistrarEntrevista extends JPanel {

    // ---- cores do layout do Figma ----
    private static final Color AZUL = new Color(0x3B3F58);
    private static final Color FUNDO = new Color(0xF3F4F6);
    private static final Color BORDA = new Color(0xD1D5DB);
    private static final Color ERRO_TEXTO = new Color(0xC0392B);
    private static final Color ERRO_FUNDO = new Color(0xFDECEC);
    private static final Color OK_TEXTO = new Color(0x1E7B3A);
    private static final Color OK_FUNDO = new Color(0xE8F5EC);

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ColetaServico coleta;
    private final PesquisaService pesquisas;
    private final ParametroServico parametros;
    private final Runnable aoSair;

    // cabeçalho da entrevista
    private final JComboBox<Pesquisa> cbPesquisa = new JComboBox<>();
    private final JComboBox<Entrevistador> cbEntrevistador = new JComboBox<>();
    private final JTextField tfData = new JTextField(LocalDate.now().format(DATA));
    private final JTextField tfIdEntrevista = new JTextField();
    private final JLabel lbSituacao = new JLabel();

    // participante
    private final JTextField tfCodParticipante = new JTextField();
    private final JComboBox<Genero> cbGenero = new JComboBox<>(Genero.values());
    private final JComboBox<FaixaEtaria> cbFaixa = new JComboBox<>();
    private final JComboBox<Escolaridade> cbEscolaridade = new JComboBox<>();
    private final JComboBox<Regiao> cbRegiao = new JComboBox<>();
    private final JComboBox<FaixaRenda> cbRenda = new JComboBox<>();

    // perguntas
    private final JPanel painelPerguntas = new JPanel();
    private final List<Pergunta> perguntasAtuais = new ArrayList<>();
    private final List<ButtonGroup> gruposAtuais = new ArrayList<>();

    private final JLabel lbMensagem = new JLabel(" ");
    private final JButton btConcluir = new JButton("Concluir entrevista");
    private final JButton btRascunho = new JButton("Salvar rascunho");
    private final JButton btCancelar = new JButton("Cancelar");

    /** Entrevista já gravada como rascunho (null = ainda não existe). */
    private Entrevista rascunho;

    public TelaRegistrarEntrevista(ColetaServico coleta, PesquisaService pesquisas,
                                   ParametroServico parametros, Runnable aoSair) {
        this.coleta = coleta;
        this.pesquisas = pesquisas;
        this.parametros = parametros;
        this.aoSair = aoSair;
        montarLayout();
        recarregarListas();
    }

    // =====================================================================
    //  Layout
    // =====================================================================

    private void montarLayout() {
        setLayout(new BorderLayout());
        setBackground(FUNDO);
        setBorder(new EmptyBorder(16, 24, 16, 24));

        JLabel titulo = new JLabel("Registrar Entrevista", SwingConstants.CENTER);
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 20f));
        titulo.setForeground(Color.WHITE);
        titulo.setOpaque(true);
        titulo.setBackground(AZUL);
        titulo.setBorder(new EmptyBorder(12, 0, 12, 0));

        JPanel corpo = new CorpoRolavel();
        corpo.setLayout(new BoxLayout(corpo, BoxLayout.Y_AXIS));
        corpo.setBackground(Color.WHITE);
        corpo.setBorder(new EmptyBorder(14, 16, 14, 16));
        corpo.add(montarCabecalho());
        corpo.add(Box.createVerticalStrut(10));
        corpo.add(montarParticipante());
        corpo.add(Box.createVerticalStrut(10));
        corpo.add(montarPerguntas());

        JScrollPane rolagem = new JScrollPane(corpo);
        rolagem.setBorder(new LineBorder(BORDA));
        rolagem.getViewport().setBackground(Color.WHITE);
        rolagem.getVerticalScrollBar().setUnitIncrement(16);

        JPanel rodape = montarRodape();

        JPanel cartao = new JPanel(new BorderLayout());
        cartao.add(titulo, BorderLayout.NORTH);
        cartao.add(rolagem, BorderLayout.CENTER);
        cartao.add(rodape, BorderLayout.SOUTH);
        cartao.setBorder(new LineBorder(BORDA));
        add(cartao, BorderLayout.CENTER);

        tfData.setEditable(false);   // o serviço registra a data de hoje
        lbSituacao.setOpaque(true);
        lbSituacao.setBackground(new Color(0xE5E7EB));
        lbSituacao.setBorder(new EmptyBorder(2, 8, 2, 8));
        lbSituacao.setText("Em coleta");

        cbPesquisa.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean s, boolean f) {
                if (v instanceof Pesquisa p) v = p.getCodigo() + " — " + p.getNome();
                return super.getListCellRendererComponent(l, v, i, s, f);
            }
        });
        cbPesquisa.addActionListener(e -> aoTrocarPesquisa());
    }

    private JPanel montarCabecalho() {
        JPanel p = secao();
        p.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(2, 4, 2, 4);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;

        adicionar(p, c, 0, 0, 2, "Pesquisa", cbPesquisa);
        adicionar(p, c, 2, 0, 1, "Entrevistador", cbEntrevistador);
        adicionar(p, c, 0, 1, 1, "Data", tfData);
        adicionar(p, c, 1, 1, 1, "Id da entrevista", tfIdEntrevista);

        JPanel info = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        info.setOpaque(false);
        info.add(lbSituacao);
        JLabel dica = new JLabel("Apenas pesquisas em coleta estão disponíveis.");
        dica.setForeground(Color.GRAY);
        dica.setFont(dica.getFont().deriveFont(11f));
        info.add(dica);
        c.gridx = 2; c.gridy = 1; c.gridwidth = 1;
        p.add(info, c);
        return p;
    }

    private JPanel montarParticipante() {
        JPanel p = secao();
        p.setBorder(BorderFactory.createTitledBorder(new LineBorder(BORDA), "Dados do participante"));
        p.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(2, 4, 2, 4);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;

        adicionar(p, c, 0, 0, 1, "Código", tfCodParticipante);
        adicionar(p, c, 1, 0, 1, "Gênero", cbGenero);
        adicionar(p, c, 2, 0, 1, "Faixa etária", cbFaixa);
        adicionar(p, c, 0, 1, 1, "Escolaridade", cbEscolaridade);
        adicionar(p, c, 1, 1, 1, "Região", cbRegiao);
        adicionar(p, c, 2, 1, 1, "Faixa de renda", cbRenda);
        return p;
    }

    private JPanel montarPerguntas() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel t = new JLabel("Perguntas da pesquisa");
        t.setFont(t.getFont().deriveFont(Font.BOLD));
        JLabel legenda = new JLabel("* Obrigatória · Selecione uma resposta por pergunta");
        legenda.setForeground(Color.GRAY);
        legenda.setFont(legenda.getFont().deriveFont(11f));
        JPanel topo = new JPanel(new BorderLayout());
        topo.setOpaque(false);
        topo.add(t, BorderLayout.WEST);
        topo.add(legenda, BorderLayout.EAST);
        p.add(topo, BorderLayout.NORTH);

        painelPerguntas.setLayout(new BoxLayout(painelPerguntas, BoxLayout.Y_AXIS));
        painelPerguntas.setOpaque(false);
        p.add(painelPerguntas, BorderLayout.CENTER);
        return p;
    }

    private JPanel montarRodape() {
        lbMensagem.setOpaque(true);
        lbMensagem.setBorder(new EmptyBorder(6, 10, 6, 10));
        lbMensagem.setFont(lbMensagem.getFont().deriveFont(12f));

        btConcluir.setBackground(AZUL);
        btConcluir.setForeground(Color.WHITE);
        btConcluir.setOpaque(true);
        btConcluir.setBorderPainted(false);

        btConcluir.addActionListener(e -> salvar(true));
        btRascunho.addActionListener(e -> salvar(false));
        btCancelar.addActionListener(e -> cancelar());

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 6));
        botoes.setOpaque(false);
        botoes.add(btConcluir);
        botoes.add(btRascunho);
        botoes.add(btCancelar);

        JPanel r = new JPanel(new BorderLayout());
        r.setBackground(Color.WHITE);
        r.setBorder(new EmptyBorder(0, 16, 8, 16));
        r.add(lbMensagem, BorderLayout.NORTH);
        r.add(botoes, BorderLayout.CENTER);
        return r;
    }

    private static JPanel secao() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        return p;
    }

    /** Painel que acompanha a largura da janela (sem barra de rolagem horizontal). */
    private static class CorpoRolavel extends JPanel implements Scrollable {
        public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 16; }
        public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return 64; }
        public boolean getScrollableTracksViewportWidth() { return true; }
        public boolean getScrollableTracksViewportHeight() { return false; }
    }

    private static void adicionar(JPanel p, GridBagConstraints c, int x, int y, int largura,
                                  String rotulo, JComponent campo) {
        JPanel celula = new JPanel(new BorderLayout(0, 2));
        celula.setOpaque(false);
        JLabel l = new JLabel(rotulo);
        l.setFont(l.getFont().deriveFont(11f));
        celula.add(l, BorderLayout.NORTH);
        celula.add(campo, BorderLayout.CENTER);
        c.gridx = x; c.gridy = y; c.gridwidth = largura;
        p.add(celula, c);
    }

    // =====================================================================
    //  Carga de dados
    // =====================================================================

    /** Recarrega pesquisas em coleta, entrevistadores e parâmetros (chame ao abrir a aba Coleta). */
    public final void recarregarListas() {
        try {
            cbPesquisa.removeAllItems();
            for (Pesquisa p : pesquisas.listarPesquisasPorSituacao(SituPesquisa.EM_COLETA)) {
                cbPesquisa.addItem(p);
            }
            cbEntrevistador.removeAllItems();
            coleta.listarEntrevistadores().forEach(cbEntrevistador::addItem);

            preencher(cbFaixa, CategoriaParametro.FAIXA_ETARIA, FaixaEtaria.class);
            preencher(cbEscolaridade, CategoriaParametro.ESCOLARIDADE, Escolaridade.class);
            preencher(cbRegiao, CategoriaParametro.REGIAO, Regiao.class);
            preencher(cbRenda, CategoriaParametro.FAIXA_RENDA, FaixaRenda.class);
            limparMensagem();
        } catch (RuntimeException ex) {
            mostrarErro(ex.getMessage());
        }
        if (cbPesquisa.getItemCount() == 0) {
            mostrarErro("Nenhuma pesquisa está em coleta no momento.");
        }
        aoTrocarPesquisa();
    }

    private <T extends Parametro> void preencher(JComboBox<T> combo, CategoriaParametro cat, Class<T> tipo) {
        combo.removeAllItems();
        for (Parametro p : parametros.listarPorCategoria(cat)) {
            if (tipo.isInstance(p)) combo.addItem(tipo.cast(p));
        }
    }

    private void aoTrocarPesquisa() {
        Pesquisa p = (Pesquisa) cbPesquisa.getSelectedItem();
        painelPerguntas.removeAll();
        perguntasAtuais.clear();
        gruposAtuais.clear();
        rascunho = null;
        travarCabecalho(false);

        if (p != null) {
            int numero = 1;
            for (Pergunta pergunta : p.getPerguntas()) {
                perguntasAtuais.add(pergunta);
                painelPerguntas.add(montarPergunta(numero++, pergunta));
            }
            int proximo = coleta.listarEntrevistas(p.getCodigo()).size() + 1;
            tfIdEntrevista.setText(String.format("ENT-%s-%04d", p.getCodigo(), proximo));
            tfCodParticipante.setText(String.format("P-%04d", proximo));
        } else {
            tfIdEntrevista.setText("");
            tfCodParticipante.setText("");
        }
        painelPerguntas.revalidate();
        painelPerguntas.repaint();
    }

    private JPanel montarPergunta(int numero, Pergunta pergunta) {
        JPanel bloco = new JPanel();
        bloco.setLayout(new BoxLayout(bloco, BoxLayout.Y_AXIS));
        bloco.setOpaque(false);
        bloco.setAlignmentX(Component.LEFT_ALIGNMENT);
        bloco.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xE5E7EB)));

        String marca = pergunta.isObrigatoria() ? " <font color='#C0392B'>*</font>" : "";
        JLabel texto = new JLabel("<html>" + numero + ". " + pergunta.getTexto() + marca + "</html>");
        texto.setBorder(new EmptyBorder(8, 0, 4, 0));
        texto.setAlignmentX(Component.LEFT_ALIGNMENT);
        bloco.add(texto);

        JPanel opcoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        opcoes.setOpaque(false);
        opcoes.setAlignmentX(Component.LEFT_ALIGNMENT);
        ButtonGroup grupo = new ButtonGroup();
        for (OpcaoResposta o : pergunta.getOpcoesResposta()) {
            opcoes.add(radio(o.getTexto(), "O:" + o.getTexto(), grupo));
        }
        opcoes.add(radio("Não sabe", "E:" + TipoEspecial.NAO_SABE.name(), grupo));
        opcoes.add(radio("Não respondeu", "E:" + TipoEspecial.NAO_RESPONDEU.name(), grupo));
        opcoes.add(radio("Nenhuma das opções", "E:" + TipoEspecial.NENHUMA_DAS_OPCOES.name(), grupo));
        bloco.add(opcoes);
        bloco.add(Box.createVerticalStrut(4));

        gruposAtuais.add(grupo);
        return bloco;
    }

    private static JRadioButton radio(String rotulo, String comando, ButtonGroup grupo) {
        JRadioButton r = new JRadioButton(rotulo);
        r.setActionCommand(comando);
        r.setOpaque(false);
        grupo.add(r);
        return r;
    }

    // =====================================================================
    //  Ações
    // =====================================================================

    private void salvar(boolean concluir) {
        limparMensagem();
        Pesquisa pesquisa = (Pesquisa) cbPesquisa.getSelectedItem();
        Entrevistador entrevistador = (Entrevistador) cbEntrevistador.getSelectedItem();
        try {
            if (pesquisa == null) {
                throw new ExcecaoRegraNegocio("Escolha uma pesquisa em coleta.");
            }
            if (entrevistador == null) {
                throw new ExcecaoRegraNegocio("Cadastre e escolha um entrevistador.");
            }
            if (concluir) {
                validarObrigatorias();
            }

            // 1) cria a entrevista (com o participante) apenas na primeira vez
            if (rascunho == null) {
                Participante participante = coleta.criarParticipante(
                        tfCodParticipante.getText(),
                        (Genero) cbGenero.getSelectedItem(),
                        (FaixaEtaria) cbFaixa.getSelectedItem(),
                        (Escolaridade) cbEscolaridade.getSelectedItem(),
                        (Regiao) cbRegiao.getSelectedItem(),
                        (FaixaRenda) cbRenda.getSelectedItem());
                rascunho = coleta.iniciarEntrevista(pesquisa.getCodigo(), tfIdEntrevista.getText(),
                        participante, entrevistador.getCodigo());
                travarCabecalho(true);
            }

            // 2) grava só as respostas que ainda não existem (o domínio recusa repetidas)
            Entrevista atual = buscarAtual(pesquisa);
            for (int i = 0; i < perguntasAtuais.size(); i++) {
                String escolha = escolhaDe(i);
                if (escolha == null || atual.temRespostaPara(perguntasAtuais.get(i))) continue;
                if (escolha.startsWith("O:")) {
                    coleta.responder(pesquisa.getCodigo(), rascunho.getId(), i, escolha.substring(2));
                } else {
                    coleta.responderEspecial(pesquisa.getCodigo(), rascunho.getId(), i,
                            TipoEspecial.valueOf(escolha.substring(2)));
                }
            }

            // 3) conclui, se foi pedido
            if (concluir) {
                coleta.concluirEntrevista(pesquisa.getCodigo(), rascunho.getId());
                mostrarSucesso("Entrevista " + rascunho.getId() + " concluída.");
                novaEntrevista();
            } else {
                mostrarSucesso("Rascunho salvo. Você pode continuar depois.");
            }
        } catch (ExcecaoRegraNegocio ex) {
            mostrarErro(ex.getMessage());
        } catch (RuntimeException ex) {
            mostrarErro("Erro inesperado: " + ex.getMessage());
        }
    }

    private void validarObrigatorias() {
        for (int i = 0; i < perguntasAtuais.size(); i++) {
            if (perguntasAtuais.get(i).isObrigatoria() && escolhaDe(i) == null) {
                throw new ExcecaoRegraNegocio("Responda todas as perguntas obrigatórias antes de concluir.");
            }
        }
    }

    private String escolhaDe(int indice) {
        ButtonModel m = gruposAtuais.get(indice).getSelection();
        return m == null ? null : m.getActionCommand();
    }

    private Entrevista buscarAtual(Pesquisa pesquisa) {
        return coleta.listarEntrevistas(pesquisa.getCodigo()).stream()
                .filter(e -> e.getId().equals(rascunho.getId()))
                .findFirst().orElse(rascunho);
    }

    private void cancelar() {
        if (rascunho != null) {
            int r = JOptionPane.showConfirmDialog(this,
                    "A entrevista " + rascunho.getId() + " está salva como rascunho. Sair mesmo assim?",
                    "Cancelar", JOptionPane.YES_NO_OPTION);
            if (r != JOptionPane.YES_OPTION) return;
        }
        novaEntrevista();
        if (aoSair != null) aoSair.run();
    }

    /** Limpa a tela para a próxima entrevista (mantém a mensagem exibida). */
    private void novaEntrevista() {
        String msg = lbMensagem.getText();
        Color fg = lbMensagem.getForeground();
        Color bg = lbMensagem.getBackground();
        aoTrocarPesquisa();
        lbMensagem.setText(msg);
        lbMensagem.setForeground(fg);
        lbMensagem.setBackground(bg);
    }

    private void travarCabecalho(boolean travar) {
        for (JComponent c : new JComponent[]{cbPesquisa, cbEntrevistador, tfIdEntrevista, tfCodParticipante,
                cbGenero, cbFaixa, cbEscolaridade, cbRegiao, cbRenda}) {
            c.setEnabled(!travar);
        }
    }

    // ---- mensagens ----

    private void mostrarErro(String texto) {
        lbMensagem.setText(texto == null ? "Erro." : texto);
        lbMensagem.setForeground(ERRO_TEXTO);
        lbMensagem.setBackground(ERRO_FUNDO);
    }

    private void mostrarSucesso(String texto) {
        lbMensagem.setText(texto);
        lbMensagem.setForeground(OK_TEXTO);
        lbMensagem.setBackground(OK_FUNDO);
    }

    private void limparMensagem() {
        lbMensagem.setText(" ");
        lbMensagem.setForeground(Color.BLACK);
        lbMensagem.setBackground(Color.WHITE);
    }
}