package com.synergymarket.swing;

import com.fasterxml.jackson.databind.JsonNode;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Painel de Produtos — CRUD completo + alerta visual de estoque baixo.
 */
public class ProdutoPanel extends JPanel {

    private static final int ESTOQUE_MINIMO = 5;

    private final String[] COLUNAS = {"ID", "Nome", "Descrição", "Preço (R$)", "Estoque"};
    private final DefaultTableModel tableModel = new DefaultTableModel(COLUNAS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable tabela = new JTable(tableModel);

    private final JTextField  txtNome      = new JTextField(20);
    private final JTextArea   txtDescricao = new JTextArea(2, 20);
    private final JFormattedTextField txtPreco = new JFormattedTextField();
    private final JSpinner    spnEstoque   = new JSpinner(new SpinnerNumberModel(0, 0, 99999, 1));

    private Long idSelecionado = null;

    public ProdutoPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(new EmptyBorder(12, 12, 12, 12));
        buildUI();
        carregarProdutos();
    }

    private void buildUI() {
        // ---------- Formulário ----------
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Dados do Produto"));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 6, 4, 6);
        g.fill   = GridBagConstraints.HORIZONTAL;

        txtPreco.setValue(0.0);

        addRow(formPanel, g, 0, "Nome *",     txtNome);
        addRow(formPanel, g, 1, "Descrição",  new JScrollPane(txtDescricao));
        addRow(formPanel, g, 2, "Preço *",    txtPreco);
        addRow(formPanel, g, 3, "Estoque",    spnEstoque);

        JButton btnSalvar  = criarBotao("Salvar",  new Color(34, 139, 34));
        JButton btnLimpar  = criarBotao("Limpar",  new Color(90, 90, 90));
        JButton btnExcluir = criarBotao("Excluir", new Color(180, 40, 40));

        btnSalvar.addActionListener(e  -> salvar());
        btnLimpar.addActionListener(e  -> limparForm());
        btnExcluir.addActionListener(e -> excluir());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnPanel.add(btnSalvar);
        btnPanel.add(btnLimpar);
        btnPanel.add(btnExcluir);

        JPanel topArea = new JPanel(new BorderLayout(0, 6));
        topArea.add(formPanel, BorderLayout.CENTER);
        topArea.add(btnPanel,  BorderLayout.SOUTH);

        // ---------- Tabela com alerta de estoque ----------
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.getTableHeader().setReorderingAllowed(false);

        // Células com estoque baixo ficam vermelhas
        tabela.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                int qtd = v != null ? Integer.parseInt(v.toString()) : 0;
                if (!sel) comp.setBackground(qtd <= ESTOQUE_MINIMO ? new Color(255, 200, 200) : Color.WHITE);
                return comp;
            }
        });

        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) preencherFormulario();
        });

        JButton btnAtualizar = criarBotao("↻ Atualizar Lista", new Color(33, 87, 153));
        btnAtualizar.addActionListener(e -> carregarProdutos());

        JLabel legendaEstoque = new JLabel("  ⚠ Células vermelhas = estoque ≤ " + ESTOQUE_MINIMO + " unidades");
        legendaEstoque.setFont(new Font("SansSerif", Font.ITALIC, 11));
        legendaEstoque.setForeground(new Color(160, 60, 60));

        JPanel tableTop = new JPanel(new BorderLayout());
        tableTop.add(legendaEstoque, BorderLayout.WEST);
        tableTop.add(btnAtualizar,   BorderLayout.EAST);

        JPanel tableArea = new JPanel(new BorderLayout(0, 4));
        tableArea.setBorder(BorderFactory.createTitledBorder("Lista de Produtos"));
        tableArea.add(tableTop, BorderLayout.NORTH);
        tableArea.add(new JScrollPane(tabela), BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topArea, tableArea);
        split.setDividerLocation(210);
        add(split, BorderLayout.CENTER);
    }

    // -------------------------------------------------------------------------
    // Operações da API
    // -------------------------------------------------------------------------

    private void carregarProdutos() {
        SwingWorker<Void, Void> w = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                var response = ApiClient.get("/produtos");
                tableModel.setRowCount(0);
                JsonNode arr = ApiClient.getMapper().readTree(response.body());
                for (JsonNode n : arr) {
                    tableModel.addRow(new Object[]{
                            n.path("id").asLong(),
                            n.path("nome").asText(),
                            n.path("descricao").asText(),
                            String.format("%.2f", n.path("preco").asDouble()),
                            n.path("quantidadeEstoque").asInt()
                    });
                }
                return null;
            }

            @Override
            protected void done() {
                try { get(); }
                catch (Exception ex) { mostrarErro("Erro ao carregar produtos: " + ex.getMessage()); }
            }
        };
        w.execute();
    }

    private void salvar() {
        if (txtNome.getText().trim().isEmpty()) { mostrarErro("Nome é obrigatório."); return; }

        Map<String, Object> body = new HashMap<>();
        body.put("nome",               txtNome.getText().trim());
        body.put("descricao",          txtDescricao.getText().trim());
        body.put("preco",              txtPreco.getValue());
        body.put("quantidadeEstoque",  spnEstoque.getValue());

        SwingWorker<Integer, Void> w = new SwingWorker<>() {
            @Override
            protected Integer doInBackground() throws Exception {
                if (idSelecionado == null) {
                    return ApiClient.post("/produtos", body).statusCode();
                } else {
                    return ApiClient.put("/produtos/" + idSelecionado, body).statusCode();
                }
            }

            @Override
            protected void done() {
                try {
                    int status = get();
                    if (status == 200 || status == 201) {
                        JOptionPane.showMessageDialog(ProdutoPanel.this, "Produto salvo!");
                        limparForm();
                        carregarProdutos();
                    } else {
                        mostrarErro("Erro ao salvar produto (HTTP " + status + ").");
                    }
                } catch (Exception ex) { mostrarErro(ex.getMessage()); }
            }
        };
        w.execute();
    }

    private void excluir() {
        if (idSelecionado == null) { mostrarErro("Selecione um produto na tabela."); return; }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Excluir o produto selecionado?", "Confirmação",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        SwingWorker<Integer, Void> w = new SwingWorker<>() {
            @Override
            protected Integer doInBackground() throws Exception {
                return ApiClient.delete("/produtos/" + idSelecionado).statusCode();
            }

            @Override
            protected void done() {
                try {
                    int s = get();
                    if (s == 200 || s == 204) {
                        JOptionPane.showMessageDialog(ProdutoPanel.this, "Produto excluído.");
                        limparForm();
                        carregarProdutos();
                    } else {
                        mostrarErro("Erro ao excluir (HTTP " + s + ").");
                    }
                } catch (Exception ex) { mostrarErro(ex.getMessage()); }
            }
        };
        w.execute();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void preencherFormulario() {
        int row = tabela.getSelectedRow();
        if (row < 0) return;
        idSelecionado = (Long) tableModel.getValueAt(row, 0);
        txtNome.setText((String) tableModel.getValueAt(row, 1));
        txtDescricao.setText((String) tableModel.getValueAt(row, 2));
        txtPreco.setValue(Double.parseDouble(tableModel.getValueAt(row, 3).toString()));
        spnEstoque.setValue(tableModel.getValueAt(row, 4));
    }

    private void limparForm() {
        idSelecionado = null;
        txtNome.setText("");
        txtDescricao.setText("");
        txtPreco.setValue(0.0);
        spnEstoque.setValue(0);
        tabela.clearSelection();
    }

    private void addRow(JPanel p, GridBagConstraints g, int row, String label, JComponent field) {
        g.gridx = 0; g.gridy = row; g.weightx = 0;
        p.add(new JLabel(label), g);
        g.gridx = 1; g.weightx = 1;
        p.add(field, g);
    }

    private JButton criarBotao(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void mostrarErro(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Erro", JOptionPane.ERROR_MESSAGE);
    }
}
