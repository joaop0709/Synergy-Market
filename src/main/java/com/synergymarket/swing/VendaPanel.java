package com.synergymarket.swing;

import com.fasterxml.jackson.databind.JsonNode;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Painel de Nova Venda — seleciona cliente, adiciona itens e finaliza a venda.
 * Descontar estoque e calcular o total é responsabilidade do backend.
 */
public class VendaPanel extends JPanel {

    // Combos de seleção
    private final JComboBox<String> cmbCliente = new JComboBox<>();
    private final JComboBox<String> cmbProduto = new JComboBox<>();
    private final JSpinner spnQtd = new JSpinner(new SpinnerNumberModel(1, 1, 9999, 1));

    // Itens da venda em construção
    private final String[] COL_ITENS = {"Produto ID", "Produto", "Qtd", "Preço Unit.", "Subtotal"};
    private final DefaultTableModel itensModel = new DefaultTableModel(COL_ITENS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable tabelaItens = new JTable(itensModel);
    private final JLabel lblTotal = new JLabel("Total: R$ 0,00");

    // Cache de IDs
    private final Map<String, Long>   clienteIds = new LinkedHashMap<>();
    private final Map<String, Long>   produtoIds = new LinkedHashMap<>();
    private final Map<String, Double> produtoPrecos = new HashMap<>();

    public VendaPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(new EmptyBorder(12, 12, 12, 12));
        buildUI();
        carregarDados();
    }

    private void buildUI() {
        // ---------- Topo: seleção de cliente ----------
        JPanel clientePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        clientePanel.setBorder(BorderFactory.createTitledBorder("1. Selecione o Cliente"));
        clientePanel.add(new JLabel("Cliente:"));
        cmbCliente.setPreferredSize(new Dimension(280, 28));
        clientePanel.add(cmbCliente);

        // ---------- Meio: adicionar item ----------
        JPanel itemPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        itemPanel.setBorder(BorderFactory.createTitledBorder("2. Adicionar Item"));
        itemPanel.add(new JLabel("Produto:"));
        cmbProduto.setPreferredSize(new Dimension(220, 28));
        itemPanel.add(cmbProduto);
        itemPanel.add(new JLabel("Qtd:"));
        itemPanel.add(spnQtd);

        JButton btnAddItem = criarBotao("+ Adicionar", new Color(33, 87, 153));
        btnAddItem.addActionListener(e -> adicionarItem());
        JButton btnRemItem = criarBotao("− Remover Selecionado", new Color(140, 40, 40));
        btnRemItem.addActionListener(e -> removerItem());
        itemPanel.add(btnAddItem);
        itemPanel.add(btnRemItem);

        // ---------- Tabela de itens ----------
        JPanel tabelaPanel = new JPanel(new BorderLayout(0, 4));
        tabelaPanel.setBorder(BorderFactory.createTitledBorder("3. Itens da Venda"));
        tabelaPanel.add(new JScrollPane(tabelaItens), BorderLayout.CENTER);

        lblTotal.setFont(new Font("SansSerif", Font.BOLD, 15));
        lblTotal.setForeground(new Color(33, 87, 153));
        JPanel totalRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        totalRow.add(lblTotal);
        tabelaPanel.add(totalRow, BorderLayout.SOUTH);

        // ---------- Botão finalizar ----------
        JButton btnFinalizar = criarBotao("✔ Finalizar Venda", new Color(34, 139, 34));
        btnFinalizar.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnFinalizar.addActionListener(e -> finalizarVenda());

        JButton btnLimpar = criarBotao("✕ Limpar", new Color(90, 90, 90));
        btnLimpar.addActionListener(e -> limpar());

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        bottomPanel.add(btnLimpar);
        bottomPanel.add(btnFinalizar);

        // ---------- Layout ----------
        JPanel topSection = new JPanel(new GridLayout(2, 1, 0, 4));
        topSection.add(clientePanel);
        topSection.add(itemPanel);

        add(topSection,   BorderLayout.NORTH);
        add(tabelaPanel,  BorderLayout.CENTER);
        add(bottomPanel,  BorderLayout.SOUTH);
    }

    // -------------------------------------------------------------------------
    // Carga de dados
    // -------------------------------------------------------------------------

    private void carregarDados() {
        SwingWorker<Void, Void> w = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                // Clientes
                var respC = ApiClient.get("/clientes");
                JsonNode arrC = ApiClient.getMapper().readTree(respC.body());
                for (JsonNode n : arrC) {
                    String label = n.path("nome").asText() + " - " + n.path("cpf").asText();
                    clienteIds.put(label, n.path("id").asLong());
                }

                // Produtos
                var respP = ApiClient.get("/produtos");
                JsonNode arrP = ApiClient.getMapper().readTree(respP.body());
                for (JsonNode n : arrP) {
                    String label = n.path("nome").asText();
                    long pid = n.path("id").asLong();
                    produtoIds.put(label, pid);
                    produtoPrecos.put(label, n.path("preco").asDouble());
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    clienteIds.forEach((k, v) -> cmbCliente.addItem(k));
                    produtoIds.forEach((k, v) -> cmbProduto.addItem(k));
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(VendaPanel.this,
                            "Erro ao carregar dados: " + ex.getMessage(),
                            "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        w.execute();
    }

    // -------------------------------------------------------------------------
    // Manipulação de itens
    // -------------------------------------------------------------------------

    private void adicionarItem() {
        String prodLabel = (String) cmbProduto.getSelectedItem();
        if (prodLabel == null) return;

        Long   prodId  = produtoIds.get(prodLabel);
        Double preco   = produtoPrecos.get(prodLabel);
        int    qtd     = (int) spnQtd.getValue();
        double subtotal = preco * qtd;

        // Verifica duplicata e soma a quantidade
        for (int i = 0; i < itensModel.getRowCount(); i++) {
            if (itensModel.getValueAt(i, 0).equals(prodId)) {
                int qtdAtual = (int) itensModel.getValueAt(i, 2);
                int novaQtd = qtdAtual + qtd;
                itensModel.setValueAt(novaQtd, i, 2);
                itensModel.setValueAt(String.format("%.2f", preco * novaQtd), i, 4);
                recalcularTotal();
                return;
            }
        }

        itensModel.addRow(new Object[]{
                prodId,
                prodLabel,
                qtd,
                String.format("%.2f", preco),
                String.format("%.2f", subtotal)
        });
        recalcularTotal();
    }

    private void removerItem() {
        int row = tabelaItens.getSelectedRow();
        if (row >= 0) {
            itensModel.removeRow(row);
            recalcularTotal();
        }
    }

    private void recalcularTotal() {
        double total = 0;
        for (int i = 0; i < itensModel.getRowCount(); i++) {
            total += Double.parseDouble(itensModel.getValueAt(i, 4).toString());
        }
        lblTotal.setText(String.format("Total: R$ %.2f", total));
    }

    // -------------------------------------------------------------------------
    // Finalizar venda
    // -------------------------------------------------------------------------

    private void finalizarVenda() {
        if (cmbCliente.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Selecione um cliente.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (itensModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Adicione ao menos um item.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String clienteLabel = (String) cmbCliente.getSelectedItem();
        Long clienteId = clienteIds.get(clienteLabel);

        // Monta o body da venda conforme a API espera
        List<Map<String, Object>> itens = new ArrayList<>();
        for (int i = 0; i < itensModel.getRowCount(); i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("produtoId",  itensModel.getValueAt(i, 0));
            item.put("quantidade", itensModel.getValueAt(i, 2));
            itens.add(item);
        }

        Map<String, Object> body = new HashMap<>();
        body.put("clienteId", clienteId);
        body.put("itens", itens);

        SwingWorker<Integer, Void> w = new SwingWorker<>() {
            @Override
            protected Integer doInBackground() throws Exception {
                return ApiClient.post("/vendas", body).statusCode();
            }

            @Override
            protected void done() {
                try {
                    int status = get();
                    if (status == 200 || status == 201) {
                        JOptionPane.showMessageDialog(VendaPanel.this,
                                "Venda registrada com sucesso! ✔",
                                "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                        limpar();
                    } else {
                        JOptionPane.showMessageDialog(VendaPanel.this,
                                "Erro ao registrar venda (HTTP " + status + ").",
                                "Erro", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(VendaPanel.this,
                            "Erro: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        w.execute();
    }

    private void limpar() {
        itensModel.setRowCount(0);
        lblTotal.setText("Total: R$ 0,00");
        spnQtd.setValue(1);
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
}
