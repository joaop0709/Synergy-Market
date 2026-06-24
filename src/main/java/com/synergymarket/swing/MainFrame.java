package com.synergymarket.swing;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Janela principal após o login.
 * Usa JTabbedPane para navegar entre os módulos do sistema.
 */
public class MainFrame extends JFrame {

    public MainFrame() {
        setTitle("Synergy Market — " + ApiClient.getLoggedUser()
                + " (" + ApiClient.getLoggedPerfil() + ")");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 620));
        buildUI();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void buildUI() {
        // Barra superior
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(33, 87, 153));
        topBar.setBorder(new EmptyBorder(8, 16, 8, 16));

        JLabel lblTitle = new JLabel("🛒 Synergy Market");
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblUser = new JLabel("Usuário: " + ApiClient.getLoggedUser());
        lblUser.setForeground(new Color(200, 220, 255));
        lblUser.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JButton btnLogout = new JButton("Sair");
        btnLogout.setBackground(new Color(180, 60, 60));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFocusPainted(false);
        btnLogout.setBorderPainted(false);
        btnLogout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(e -> {
            ApiClient.logout();
            dispose();
            new LoginFrame();
        });

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(lblUser);
        right.add(btnLogout);

        topBar.add(lblTitle, BorderLayout.WEST);
        topBar.add(right, BorderLayout.EAST);

        // Abas
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("SansSerif", Font.PLAIN, 13));

        tabs.addTab("🧑 Clientes",    new ClientePanel());
        tabs.addTab("📦 Produtos",    new ProdutoPanel());
        tabs.addTab("🛒 Nova Venda",  new VendaPanel());
        tabs.addTab("📊 Relatórios",  new RelatorioPanel());

        // Layout raiz
        JPanel root = new JPanel(new BorderLayout());
        root.add(topBar, BorderLayout.NORTH);
        root.add(tabs,   BorderLayout.CENTER);

        setContentPane(root);
    }
}
