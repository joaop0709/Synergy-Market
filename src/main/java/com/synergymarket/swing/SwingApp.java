package com.synergymarket.swing;

import javax.swing.*;

/**
 * Ponto de entrada da interface gráfica Swing.
 *
 * Como rodar:
 *   1. Certifique-se de que o backend Spring Boot está rodando em localhost:8080
 *   2. Execute esta classe diretamente pela sua IDE
 *      (Run > SwingApp.main) ou via Maven:
 *      mvn exec:java -Dexec.mainClass="com.synergymarket.swing.SwingApp"
 */
public class SwingApp {

    public static void main(String[] args) {
        // Usa o look and feel do sistema operacional
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        // Toda interação com Swing deve rodar na Event Dispatch Thread
        SwingUtilities.invokeLater(LoginFrame::new);
    }
}
