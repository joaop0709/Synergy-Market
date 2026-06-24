package com.synergymarket.swing;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Tela de Login — primeira janela exibida ao usuário.
 * Autentica via POST /api/auth/login e armazena o token JWT no ApiClient.
 */
public class LoginFrame extends JFrame {

    private final JTextField txtUsername = new JTextField(20);
    private final JPasswordField txtSenha = new JPasswordField(20);
    private final JButton btnLogin       = new JButton("Entrar");
    private final JLabel lblStatus       = new JLabel(" ");

    public LoginFrame() {
        setTitle("Synergy Market — Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        buildUI();
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void buildUI() {
        // Painel principal com padding
        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBorder(new EmptyBorder(32, 40, 32, 40));
        root.setBackground(new Color(245, 245, 248));

        // Cabeçalho
        JLabel title = new JLabel("🛒 Synergy Market", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(new Color(33, 87, 153));

        JLabel subtitle = new JLabel("Sistema de Gestão Comercial", SwingConstants.CENTER);
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitle.setForeground(Color.GRAY);

        JPanel header = new JPanel(new GridLayout(2, 1, 0, 4));
        header.setOpaque(false);
        header.add(title);
        header.add(subtitle);

        // Formulário
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill   = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        form.add(new JLabel("Usuário:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        form.add(txtUsername, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        form.add(new JLabel("Senha:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        form.add(txtSenha, gbc);

        // Botão
        styleButton(btnLogin, new Color(33, 87, 153));
        btnLogin.addActionListener(this::doLogin);

        // Permitir Enter para logar
        getRootPane().setDefaultButton(btnLogin);

        // Status
        lblStatus.setHorizontalAlignment(SwingConstants.CENTER);
        lblStatus.setForeground(Color.RED);

        JPanel bottom = new JPanel(new GridLayout(2, 1, 0, 6));
        bottom.setOpaque(false);
        bottom.add(btnLogin);
        bottom.add(lblStatus);

        root.add(header, BorderLayout.NORTH);
        root.add(form,   BorderLayout.CENTER);
        root.add(bottom, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void doLogin(ActionEvent e) {
        String username = txtUsername.getText().trim();
        String senha    = new String(txtSenha.getPassword());

        if (username.isEmpty() || senha.isEmpty()) {
            lblStatus.setText("Preencha usuário e senha.");
            return;
        }

        btnLogin.setEnabled(false);
        lblStatus.setText("Autenticando...");
        lblStatus.setForeground(Color.DARK_GRAY);

        // Chama a API em thread separada para não travar a UI
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return ApiClient.login(username, senha);
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        dispose();
                        new MainFrame();
                    } else {
                        lblStatus.setText("Usuário ou senha inválidos.");
                        lblStatus.setForeground(Color.RED);
                    }
                } catch (Exception ex) {
                    lblStatus.setText("Erro de conexão com o servidor.");
                    lblStatus.setForeground(Color.RED);
                    ex.printStackTrace();
                } finally {
                    btnLogin.setEnabled(true);
                }
            }
        };
        worker.execute();
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
}
