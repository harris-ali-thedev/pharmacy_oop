package com.pharmacy.ui;

import javax.swing.*;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public final class SwingUtils {
    public static final Color PRIMARY = new Color(20, 83, 114);
    public static final Color PRIMARY_2 = new Color(15, 118, 110);
    public static final Color ACCENT = new Color(234, 88, 12);
    public static final Color SUCCESS = new Color(22, 163, 74);
    public static final Color DANGER = new Color(220, 38, 38);
    public static final Color WARNING = new Color(202, 138, 4);
    public static final Color BG = new Color(244, 247, 251);
    public static final Color CARD_BG = Color.WHITE;
    public static final Color TEXT_DARK = new Color(17, 24, 39);
    public static final Color TEXT_GRAY = new Color(91, 103, 118);
    public static final Color BORDER = new Color(218, 225, 234);
    public static final Color SIDEBAR = new Color(8, 35, 48);
    public static final Color SIDEBAR_HOVER = new Color(17, 64, 83);
    public static final Color SOFT_BLUE = new Color(232, 246, 250);
    public static final Color SOFT_GREEN = new Color(232, 248, 241);
    public static final Color SOFT_ORANGE = new Color(255, 244, 232);

    public static final Font FONT_TITLE = new Font("Segue UI", Font.BOLD, 22);
    public static final Font FONT_HEADING = new Font("Segue UI", Font.BOLD, 15);
    public static final Font FONT_BODY = new Font("Segue UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL = new Font("Segue UI", Font.PLAIN, 11);
    public static final Font FONT_MONO = new Font("Consolas", Font.PLAIN, 12);

    private SwingUtils() {}

    public static JButton primaryButton(String text) {
        return styledButton(text, PRIMARY, Color.WHITE);
    }

    public static JButton successButton(String text) {
        return styledButton(text, SUCCESS, Color.WHITE);
    }

    public static JButton dangerButton(String text) {
        return styledButton(text, DANGER, Color.WHITE);
    }

    public static JButton warningButton(String text) {
        return styledButton(text, WARNING, Color.WHITE);
    }

    public static JButton outlineButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(PRIMARY);
        btn.setBackground(Color.WHITE);
        btn.setOpaque(true);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(175, 194, 204), 1, true),
            BorderFactory.createEmptyBorder(8, 16, 8, 16)));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(SOFT_BLUE); }
            @Override public void mouseExited(MouseEvent e) { btn.setBackground(Color.WHITE); }
        });
        return btn;
    }

    private static JButton styledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(fg);
        btn.setBackground(bg);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(9, 18, 9, 18));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        Color hover = bg.darker();
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(hover); }
            @Override public void mouseExited(MouseEvent e) { btn.setBackground(bg); }
        });
        return btn;
    }

    public static JLabel titleLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_TITLE);
        l.setForeground(TEXT_DARK);
        return l;
    }

    public static JLabel headingLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_HEADING);
        l.setForeground(TEXT_DARK);
        return l;
    }

    public static JLabel bodyLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_BODY);
        l.setForeground(TEXT_DARK);
        return l;
    }

    public static JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(TEXT_GRAY);
        return l;
    }

    public static JLabel badge(String text, Color bg) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setOpaque(true);
        l.setBackground(bg);
        l.setForeground(Color.WHITE);
        l.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        return l;
    }

    public static JTextField styledField(String prompt) {
        JTextField tf = new JTextField();
        tf.setToolTipText(prompt);
        tf.setFont(FONT_BODY);
        tf.setForeground(TEXT_DARK);
        tf.setBackground(Color.WHITE);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        tf.setPreferredSize(new Dimension(220, 38));
        return tf;
    }

    public static JPasswordField styledPasswordField() {
        JPasswordField pf = new JPasswordField();
        pf.setFont(FONT_BODY);
        pf.setForeground(TEXT_DARK);
        pf.setBackground(Color.WHITE);
        pf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        pf.setPreferredSize(new Dimension(220, 38));
        return pf;
    }

    public static JComboBox<String> styledCombo(String... items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(FONT_BODY);
        cb.setBackground(Color.WHITE);
        cb.setBorder(BorderFactory.createLineBorder(BORDER, 1, true));
        return cb;
    }

    public static JPanel card() {
        JPanel p = new JPanel();
        p.setBackground(CARD_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1, true),
            BorderFactory.createEmptyBorder(18, 22, 18, 22)));
        return p;
    }

    public static JPanel statCard(String value, String label, Color accent) {
        JPanel card = card();
        card.setLayout(new GridLayout(2, 1, 4, 4));
        JLabel valLbl = new JLabel(value, SwingConstants.CENTER);
        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 26));
        valLbl.setForeground(accent);
        JLabel nameLbl = new JLabel(label, SwingConstants.CENTER);
        nameLbl.setFont(FONT_SMALL);
        nameLbl.setForeground(TEXT_GRAY);
        card.add(valLbl);
        card.add(nameLbl);
        return card;
    }

    public static JPanel pageHeader(String title, String subtitle, JButton action) {
        JPanel h = new JPanel(new BorderLayout(12, 0));
        h.setBackground(Color.WHITE);
        h.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
            BorderFactory.createEmptyBorder(18, 24, 16, 24)));
        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(titleLabel(title));
        if (subtitle != null && !subtitle.isBlank()) {
            JLabel sub = bodyLabel(subtitle);
            sub.setForeground(TEXT_GRAY);
            text.add(Box.createVerticalStrut(3));
            text.add(sub);
        }
        h.add(text, BorderLayout.WEST);
        if (action != null) h.add(action, BorderLayout.EAST);
        return h;
    }

    public static JScrollPane cleanScroll(Component component) {
        JScrollPane scroll = new JScrollPane(component);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(BG);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    public static void styleTable(JTable table) {
        table.setFont(FONT_BODY);
        table.setRowHeight(36);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(210, 242, 238));
        table.setSelectionForeground(TEXT_DARK);
        table.setBackground(CARD_BG);
        table.setGridColor(BORDER);
        table.setShowHorizontalLines(true);
        table.setFillsViewportHeight(true);
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(239, 245, 249));
        header.setForeground(TEXT_GRAY);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
        header.setPreferredSize(new Dimension(0, 38));
    }

    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void showInfo(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    public static boolean confirm(Component parent, String message) {
        return JOptionPane.showConfirmDialog(parent, message, "Confirm",
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    public static String inputDialog(Component parent, String prompt, String initial) {
        return JOptionPane.showInputDialog(parent, prompt, initial);
    }

    public static JPanel hBox(Component... comps) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        p.setOpaque(false);
        for (Component c : comps) p.add(c);
        return p;
    }

    public static JPanel separator() {
        JPanel p = new JPanel();
        p.setPreferredSize(new Dimension(1, 1));
        p.setBackground(BORDER);
        return p;
    }
}
