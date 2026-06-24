package com.synergymarket.swing;

import com.fasterxml.jackson.databind.JsonNode;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Painel de Clientes — lista, cria, edita e remove clientes via API REST.
 */
public class ClientePanel extends JPanel {

    // Tabela
    private final String[] COLUNAS = {"ID", "Nome", "CPF", "E-mail", "Telefone", "Endereço"};
    private final DefaultTableModel tableModel = new DefaultTableModel(COLUNAS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable tabela = new JTable(tableModel);

    // Campos de formulário
    private final JTextField txtNome      = new JTextField(20);
    private final JTextField txtCpf       = new JTextField(14);
    private final JTextField txtEmail     = new JTextField(20);
    private final JTextField txtTelefone  = new JTextField(15);
    private final JTextField txtEndereco  = new JTextField(30);

    // ID do registro selecionado (null = novo)
    private Long idSelecionado = null;

    public ClientePanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(new EmptyBorder(12, 12, 12, 12));
        buildUI();
        carregarClientes();
    }

    private void buildUI() {
        // ---------- Formulário ----------
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Dados do Cliente"));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 6, 4, 6);
        g.fill   = GridBagConstraints.HORIZONTAL;

        addRow(formPanel, g, 0, "Nome *",     txtNome);
        addRow(formPanel, g, 1, "CPF *",      txtCpf);
        addRow(formPanel, g, 2, "E-mail *",   txtEmail);
        addRow(formPanel, g, 3, "Telefone",   txtTelefone);
        addRow(formPanel, g, 4, "Endereço",   txtEndereco);

        // Botões do formulário
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

        // ---------- Tabela ----------
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.getTableHeader().setReorderingAllowed(false);
        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) preencherFormulario();
        });

        JButton btnAtualizar = criarBotao("↻ Atualizar Lista", new Color(33, 87, 153));
        btnAtualizar.addActionListener(e -> carregarClientes());

        JPanel tableTop = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        tableTop.add(btnAtualizar);

        JPanel tableArea = new JPanel(new BorderLayout(0, 4));
        tableArea.setBorder(BorderFactory.createTitledBorder("Lista de Clientes"));
        tableArea.add(tableTop, BorderLayout.NORTH);
        tableArea.add(new JScrollPane(tabela), BorderLayout.CENTER);

        // ---------- Layout geral ----------
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topArea, tableArea);
        split.setDividerLocation(220);
        split.setResizeWeight(0.35);
        add(split, BorderLayout.CENTER);
    }

    // -------------------------------------------------------------------------
    // Operações da API
    // -------------------------------------------------------------------------

    private void carregarClientes() {
        SwingWorker<Void, Void> w = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                var response = ApiClient.get("/clientes");
                tableModel.setRowCount(0);
                JsonNode arr = ApiClient.getMapper().readTree(response.body());
                for (JsonNode n : arr) {
                    tableModel.addRow(new Object[]{
                            n.path("id").asLong(),
                            n.path("nome").asText(),
                            n.path("cpf").asText(),
                            n.path("email").asText(),
                            n.path("telefone").asText(),
                            n.path("endereco").asText()
                    });
                }
                return null;
            }

            @Override
            protected void done() {
                try { get(); }
                catch (Exception ex) { mostrarErro("Erro ao carregar clientes: " + ex.getMessage()); }
            }
        };
        w.execute();
    }

    private void salvar() {
        if (!validarCampos()) return;

        Map<String, String> body = new HashMap<>();
        body.put("nome",     txtNome.getText().trim());
        body.put("cpf",      txtCpf.getText().trim());
        body.put("email",    txtEmail.getText().trim());
        body.put("telefone", txtTelefone.getText().trim());
        body.put("endereco", txtEndereco.getText().trim());

        SwingWorker<Integer, Void> w = new SwingWorker<>() {
            @Override
            protected Integer doInBackground() throws Exception {
                if (idSelecionado == null) {
                    return ApiClient.post("/clientes", body).statusCode();
                } else {
                    return ApiClient.put("/clientes/" + idSelecionado, body).statusCode();
                }
            }

            @Override
            protected void done() {
                try {
                    int status = get();
                    if (status == 200 || status == 201) {
                        JOptionPane.showMessageDialog(ClientePanel.this,
                                "Cliente salvo com sucesso!", "Sucesso",
                                JOptionPane.INFORMATION_MESSAGE);
                        limparForm();
                        carregarClientes();
                    } else {
                        mostrarErro("Erro ao salvar cliente (HTTP " + status + ").");
                    }
                } catch (Exception ex) { mostrarErro(ex.getMessage()); }
            }
        };
        w.execute();
    }

    private void excluir() {
        if (idSelecionado == null) { mostrarErro("Selecione um cliente na tabela."); return; }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Excluir o cliente selecionado?", "Confirmação",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        SwingWorker<Integer, Void> w = new SwingWorker<>() {
            @Override
            protected Integer doInBackground() throws Exception {
                return ApiClient.delete("/clientes/" + idSelecionado).statusCode();
            }

            @Override
            protected void done() {
                try {
                    int status = get();
                    if (status == 204 || status == 200) {
                        JOptionPane.showMessageDialog(ClientePanel.this, "Cliente excluído.");
                        limparForm();
                        carregarClientes();
                    } else {
                        mostrarErro("Erro ao excluir cliente (HTTP " + status + ").");
                    }
                } catch (Exception ex) { mostrarErro(ex.getMessage()); }
            }
        };
        w.execute();
    }

    // -------------------------------------------------------------------------
    // Helpers de UI
    // -------------------------------------------------------------------------

    private void preencherFormulario() {
        int row = tabela.getSelectedRow();
        if (row < 0) return;
        idSelecionado = (Long) tableModel.getValueAt(row, 0);
        txtNome.setText((String) tableModel.getValueAt(row, 1));
        txtCpf.setText((String) tableModel.getValueAt(row, 2));
        txtEmail.setText((String) tableModel.getValueAt(row, 3));
        txtTelefone.setText((String) tableModel.getValueAt(row, 4));
        txtEndereco.setText((String) tableModel.getValueAt(row, 5));
    }

    private void limparForm() {
        idSelecionado = null;
        txtNome.setText("");
        txtCpf.setText("");
        txtEmail.setText("");
        txtTelefone.setText("");
        txtEndereco.setText("");
        tabela.clearSelection();
    }

    private boolean validarCampos() {
        if (txtNome.getText().trim().isEmpty()) { mostrarErro("Nome é obrigatório."); return false; }
        if (txtCpf.getText().trim().isEmpty())  { mostrarErro("CPF é obrigatório.");  return false; }
        if (txtEmail.getText().trim().isEmpty()) { mostrarErro("E-mail é obrigatório."); return false; }
        return true;
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
