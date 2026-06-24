package com.synergymarket.swing;

import com.fasterxml.jackson.databind.JsonNode;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.Iterator;

/**
 * Painel de Relatórios — lista todas as vendas e calcula totais por período.
 * Permite filtrar por texto (cliente ou data) e exportar para CSV.
 */
public class RelatorioPanel extends JPanel {

    private final String[] COLUNAS = {"ID Venda", "Data", "Cliente", "Total (R$)", "Itens", "Usuário"};
    private final DefaultTableModel tableModel = new DefaultTableModel(COLUNAS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable tabela = new JTable(tableModel);
    private final JLabel lblTotalGeral = new JLabel("Total geral: R$ 0,00");
    private final JTextField txtFiltro = new JTextField(20);

    // Guarda os dados brutos para filtro local
    private final java.util.List<Object[]> dadosCompletos = new java.util.ArrayList<>();

    public RelatorioPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(new EmptyBorder(12, 12, 12, 12));
        buildUI();
        carregarVendas();
    }

    private void buildUI() {
        // ---------- Barra de filtro e ações ----------
        JPanel barraTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        barraTop.setBorder(BorderFactory.createTitledBorder("Filtros e Ações"));

        barraTop.add(new JLabel("Buscar (cliente/data):"));
        barraTop.add(txtFiltro);

        JButton btnFiltrar    = criarBotao("🔍 Filtrar",         new Color(33, 87, 153));
        JButton btnLimpar     = criarBotao("✕ Limpar Filtro",    new Color(90, 90, 90));
        JButton btnAtualizar  = criarBotao("↻ Atualizar",        new Color(60, 120, 60));
        JButton btnExportarCSV = criarBotao("⬇ Exportar CSV",   new Color(120, 60, 140));

        btnFiltrar.addActionListener(e    -> aplicarFiltro());
        btnLimpar.addActionListener(e     -> { txtFiltro.setText(""); aplicarFiltro(); });
        btnAtualizar.addActionListener(e  -> carregarVendas());
        btnExportarCSV.addActionListener(e -> exportarCSV());

        barraTop.add(btnFiltrar);
        barraTop.add(btnLimpar);
        barraTop.add(btnAtualizar);
        barraTop.add(btnExportarCSV);

        // ---------- Tabela ----------
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.getTableHeader().setReorderingAllowed(false);
        tabela.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        // Alinha valores monetários à direita
        DefaultTableCellRenderer rightAlign = new DefaultTableCellRenderer();
        rightAlign.setHorizontalAlignment(SwingConstants.RIGHT);
        tabela.getColumnModel().getColumn(3).setCellRenderer(rightAlign);

        JPanel tableArea = new JPanel(new BorderLayout(0, 6));
        tableArea.setBorder(BorderFactory.createTitledBorder("Histórico de Vendas"));
        tableArea.add(new JScrollPane(tabela), BorderLayout.CENTER);

        // Rodapé com total
        lblTotalGeral.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblTotalGeral.setForeground(new Color(33, 87, 153));
        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rodape.add(lblTotalGeral);
        tableArea.add(rodape, BorderLayout.SOUTH);

        add(barraTop,   BorderLayout.NORTH);
        add(tableArea,  BorderLayout.CENTER);
    }

    // -------------------------------------------------------------------------
    // Carga de dados
    // -------------------------------------------------------------------------

    private void carregarVendas() {
        SwingWorker<Void, Void> w = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                var response = ApiClient.get("/vendas");
                JsonNode arr = ApiClient.getMapper().readTree(response.body());

                dadosCompletos.clear();

                for (JsonNode venda : arr) {
                    long   id         = venda.path("id").asLong();
                    String data       = venda.path("dataVenda").asText("—").replace("T", " ");
                    String cliente    = venda.path("cliente").path("nome").asText("—");
                    double total      = venda.path("valorTotal").asDouble();
                    String usuario    = venda.path("usuario").path("username").asText("—");

                    // Conta itens
                    int numItens = 0;
                    JsonNode itens = venda.path("itens");
                    if (itens.isArray()) {
                        Iterator<JsonNode> it = itens.elements();
                        while (it.hasNext()) { it.next(); numItens++; }
                    }

                    dadosCompletos.add(new Object[]{
                            id,
                            data,
                            cliente,
                            String.format("%.2f", total),
                            numItens,
                            usuario
                    });
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    aplicarFiltro();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(RelatorioPanel.this,
                            "Erro ao carregar vendas: " + ex.getMessage(),
                            "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        w.execute();
    }

    // -------------------------------------------------------------------------
    // Filtro local e total
    // -------------------------------------------------------------------------

    private void aplicarFiltro() {
        String filtro = txtFiltro.getText().trim().toLowerCase();
        tableModel.setRowCount(0);
        double soma = 0;

        for (Object[] row : dadosCompletos) {
            String cliente = row[2].toString().toLowerCase();
            String data    = row[1].toString().toLowerCase();

            if (filtro.isEmpty() || cliente.contains(filtro) || data.contains(filtro)) {
                tableModel.addRow(row);
                soma += Double.parseDouble(row[3].toString());
            }
        }

        lblTotalGeral.setText(String.format("Total filtrado: R$ %.2f  (%d venda(s))",
                soma, tableModel.getRowCount()));
    }

    // -------------------------------------------------------------------------
    // Exportar CSV
    // -------------------------------------------------------------------------

    private void exportarCSV() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Nenhum dado para exportar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new java.io.File("relatorio_vendas.csv"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        java.io.File file = fc.getSelectedFile();
        try (java.io.PrintWriter pw = new java.io.PrintWriter(
                new java.io.OutputStreamWriter(
                        new java.io.FileOutputStream(file), java.nio.charset.StandardCharsets.UTF_8))) {

            // Cabeçalho
            pw.println("ID Venda;Data;Cliente;Total (R$);Itens;Usuário");

            // Linhas
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < tableModel.getColumnCount(); j++) {
                    if (j > 0) sb.append(";");
                    sb.append(tableModel.getValueAt(i, j));
                }
                pw.println(sb);
            }

            JOptionPane.showMessageDialog(this,
                    "CSV exportado com sucesso!\n" + file.getAbsolutePath(),
                    "Exportação", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao exportar: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
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
