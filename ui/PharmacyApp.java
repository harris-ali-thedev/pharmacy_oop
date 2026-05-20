package com.pharmacy.ui;

import javax.swing.*;
import java.awt.*;

public class PharmacyApp {

    private static JFrame loginFrame;

    public static void main(String[] args) {
        // Configure look and feel BEFORE anything touches Swing
        configureLookAndFeel();

        // All Swing work must happen on the Event Dispatch Thread
        SwingUtilities.invokeLater(PharmacyApp::showLogin);
    }

    public static void showLogin() {
        if (loginFrame != null) {
            loginFrame.dispose();
        }

        loginFrame = new JFrame("MediCare PMS — Login");
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setSize(820, 560);
        loginFrame.setMinimumSize(new Dimension(680, 520));
        loginFrame.setLocationRelativeTo(null);
        loginFrame.setResizable(true);

        LoginPanel loginPanel = new LoginPanel(() -> {
            // Callback: login succeeded — open main window
            SwingUtilities.invokeLater(() -> {
                loginFrame.dispose();
                loginFrame = null;
                MainFrame main = new MainFrame();
                main.setVisible(true);
            });
        });

        loginFrame.setContentPane(loginPanel);
        loginFrame.setVisible(true);
    }

    private static void configureLookAndFeel() {
        try {
            // Try Nimbus for a modern look; fall back to system default
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    // Customize Nimbus colour overrides
                    UIManager.put("nimbusBase",               SwingUtils.PRIMARY);
                    UIManager.put("nimbusBlueGrey",           new Color(100, 116, 139));
                    UIManager.put("control",                  SwingUtils.BG);
                    UIManager.put("text",                     SwingUtils.TEXT_DARK);
                    UIManager.put("Table.alternateRowColor",  new Color(248, 250, 252));
                    return;
                }
            }
            // Fall back to system L&F
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Use default Metal L&F — silently ignore
        }

        // Global font defaults
        Font defaultFont = new Font("Segoe UI", Font.PLAIN, 13);
        java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof Font) {
                UIManager.put(key, defaultFont.deriveFont(((Font) value).getStyle(),
                    ((Font) value).getSize()));
            }
        }
    }
}
