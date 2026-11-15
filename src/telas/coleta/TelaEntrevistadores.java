package telas.coleta;

import dominio.coleta.Entrevistador;
import excecoes.ExcecaoRegraNegocio;
import servicos.coleta.ColetaServico;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Tela "Gerenciar Entrevistadores" (módulo Coleta e Amostra).
 * Cadastrar, listar, editar o nome e remover. O código é imutável depois de cadastrado.
 */
public class TelaEntrevistadores extends JPanel {

    private static final Color AZUL = new Color(0x3B3F58);
    private static final Color FUNDO = new Color(0xF3F4F6);
    private static final Color BORDA = new Color(0xD1D5DB);
    private static final Color ERRO_TEXTO = new Color(0xC0392B);
    private static final Color ERRO_FUNDO = new Color(0xFDECEC);
    private static final Color OK_TEXTO = new Color(0x1E7B3A);
    private static final Color OK_FUNDO = new Color(0xE8F5EC);

    private final ColetaServico coleta;

    private final JTextField tfCodigo = new JTextField();
    private final JTextField tfNome = new JTextField();
    private final JButton btSalvar = new JButton("Cadastrar");
    private final JButton btNovo = new JButton("Novo");
    private final JButton btEditar = new JButton("Editar selecionado");
    private final JButton btRemover = new JButton("Remover selecionado");
    private final JLabel lbMensagem = new JLabel(" ");

    private final DefaultTableModel modelo = new DefaultTableModel(new Object[]{"Código", "Nome"}, 0) {
        @Override
        public boolean isCellEditable(int row, int col) { return false; }
    };
    private final JTable tabela = new JTable(modelo);

    /** Código do entrevistador em edição (null = cadastrando um novo). */
    private String emEdicao;

    public TelaEntrevistadores(ColetaServico coleta) {
        this.coleta = coleta;
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

        JLabel titulo = new JLabel("Gerenciar Entrevistadores", SwingConstants.CENTER);
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 20f));
        titulo.setForeground(Color.WHITE);
        titulo.setOpaque(true);
        titulo.setBackground(AZUL);
        titulo.setBorder(new EmptyBorder(12, 0, 12, 0));

        JPanel corpo = new JPanel(new BorderLayout(0, 12));
        corpo.setBackground(Color.WHITE);
        corpo.setBorder(new EmptyBorder(14, 16, 14, 16));
        corpo.add(montarFormulario(), BorderLayout.NORTH);
        corpo.add(montarTabela(), BorderLayout.CENTER);

        lbMensagem.setOpaque(true);
        lbMensagem.setBorder(new EmptyBorder(6, 10, 6, 10));
        lbMensagem.setFont(lbMensagem.getFont().deriveFont(12f));
        JPanel rodape = new JPanel(new BorderLayout());
        rodape.setBackground(Color.WHITE);
        rodape.setBorder(new EmptyBorder(0, 16, 12, 16));
        rodape.add(lbMensagem, BorderLayout.CENTER);

        JPanel cartao = new JPanel(new BorderLayout());
        cartao.setBorder(new LineBorder(BORDA));
        cartao.add(titulo, BorderLayout.NORTH);
        cartao.add(corpo, BorderLayout.CENTER);
        cartao.add(rodape, BorderLayout.SOUTH);
        add(cartao, BorderLayout.CENTER);

        btSalvar.setBackground(AZUL);
        btSalvar.setForeground(Color.WHITE);
        btSalvar.setOpaque(true);
        btSalvar.setBorderPainted(false);

        btSalvar.addActionListener(e -> salvar());
        btNovo.addActionListener(e -> novo());
        btEditar.addActionListener(e -> editarSelecionado());
        btRemover.addActionListener(e -> removerSelecionado());
    }

    private JPanel montarFormulario() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createTitledBorder(new LineBorder(BORDA), "Dados do entrevistador"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(2, 6, 2, 6);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0; c.gridy = 0; c.weightx = 0.3;
        p.add(rotulado("Código", tfCodigo), c);
        c.gridx = 1; c.weightx = 0.7;
        p.add(rotulado("Nome", tfNome), c);

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        botoes.setOpaque(false);
        botoes.add(btSalvar);
        botoes.add(btNovo);
        c.gridx = 0; c.gridy = 1; c.gridwidth = 2; c.weightx = 1;
        p.add(botoes, c);
        return p;
    }

    private JPanel montarTabela() {
        tabela.setRowHeight(24);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.getTableHeader().setReorderingAllowed(false);
        tabela.getColumnModel().getColumn(0).setMaxWidth(160);
        tabela.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) editarSelecionado();
            }
        });

        JScrollPane rolagem = new JScrollPane(tabela);
        rolagem.setBorder(new LineBorder(BORDA));
        rolagem.getViewport().setBackground(Color.WHITE);

        JPanel acoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        acoes.setOpaque(false);
        acoes.add(btEditar);
        acoes.add(btRemover);

        JLabel t = new JLabel("Entrevistadores cadastrados");
        t.setFont(t.getFont().deriveFont(Font.BOLD));

        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setOpaque(false);
        p.add(t, BorderLayout.NORTH);
        p.add(rolagem, BorderLayout.CENTER);
        p.add(acoes, BorderLayout.SOUTH);
        return p;
    }

    private static JPanel rotulado(String rotulo, JComponent campo) {
        JPanel celula = new JPanel(new BorderLayout(0, 2));
        celula.setOpaque(false);
        JLabel l = new JLabel(rotulo);
        l.setFont(l.getFont().deriveFont(11f));
        celula.add(l, BorderLayout.NORTH);
        celula.add(campo, BorderLayout.CENTER);
        return celula;
    }

    // =====================================================================
    //  Dados e ações
    // =====================================================================

    /** Recarrega a tabela (chame ao abrir a aba Coleta). */
    public final void recarregarListas() {
        modelo.setRowCount(0);
        try {
            List<Entrevistador> lista = coleta.listarEntrevistadores();
            for (Entrevistador e : lista) {
                modelo.addRow(new Object[]{e.getCodigo(), e.getNome()});
            }
        } catch (RuntimeException ex) {
            mostrarErro(ex.getMessage());
        }
    }

    private void salvar() {
        limparMensagem();
        try {
            if (emEdicao == null) {
                Entrevistador novo = coleta.cadastrarEntrevistador(tfCodigo.getText(), tfNome.getText());
                recarregarListas();
                novo();
                mostrarSucesso("Entrevistador " + novo.getCodigo() + " cadastrado.");
            } else {
                coleta.editarEntrevistador(emEdicao, tfNome.getText());
                String codigo = emEdicao;
                recarregarListas();
                novo();
                mostrarSucesso("Entrevistador " + codigo + " atualizado.");
            }
        } catch (ExcecaoRegraNegocio ex) {
            mostrarErro(ex.getMessage());
        } catch (RuntimeException ex) {
            mostrarErro("Erro inesperado: " + ex.getMessage());
        }
    }

    private void editarSelecionado() {
        String codigo = codigoSelecionado();
        if (codigo == null) return;
        limparMensagem();
        emEdicao = codigo;
        tfCodigo.setText(codigo);
        tfCodigo.setEnabled(false);   // o código não muda
        tfNome.setText((String) modelo.getValueAt(tabela.getSelectedRow(), 1));
        btSalvar.setText("Salvar alterações");
        tfNome.requestFocus();
    }

    private void removerSelecionado() {
        String codigo = codigoSelecionado();
        if (codigo == null) return;
        int r = JOptionPane.showConfirmDialog(this,
                "Remover o entrevistador " + codigo + "?", "Remover", JOptionPane.YES_NO_OPTION);
        if (r != JOptionPane.YES_OPTION) return;
        limparMensagem();
        try {
            coleta.removerEntrevistador(codigo);
            recarregarListas();
            novo();
            mostrarSucesso("Entrevistador " + codigo + " removido.");
        } catch (ExcecaoRegraNegocio ex) {
            mostrarErro(ex.getMessage());
        } catch (RuntimeException ex) {
            mostrarErro("Erro inesperado: " + ex.getMessage());
        }
    }

    /** Volta o formulário para o modo "cadastrar". */
    private void novo() {
        emEdicao = null;
        tfCodigo.setText("");
        tfCodigo.setEnabled(true);
        tfNome.setText("");
        btSalvar.setText("Cadastrar");
        tabela.clearSelection();
    }

    private String codigoSelecionado() {
        int linha = tabela.getSelectedRow();
        if (linha < 0) {
            mostrarErro("Selecione um entrevistador na tabela.");
            return null;
        }
        return (String) modelo.getValueAt(linha, 0);
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