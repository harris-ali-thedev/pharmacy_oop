package com.pharmacy.ui;

import com.pharmacy.util.AuditLogger;
import com.pharmacy.util.SessionManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Audit log viewer — Admin only.
 * Shows all recorded system events from the log file.
 * Demonstrates: File reading (AuditLogger), Swing table, Access control
 */
public class AuditLogPanel extends JPanel {

    private JTable            table;
    private DefaultTableModel model;
    private JTextField        filterField;

    public AuditLogPanel() {
        setLayout(new BorderLayout());
        setBackground(SwingUtils.BG);

        if (!SessionManager.getInstance().isAdmin()) {
            JPanel p = new JPanel(new GridBagLayout());
            p.setBackground(SwingUtils.BG);
            JLabel l = new JLabel("\uD83D\uDD12  Access Denied — Administrators only.");
            l.setFont(new Font("Segoe UI", Font.BOLD, 18));
            l.setForeground(SwingUtils.DANGER);
            p.add(l);
            add(p, BorderLayout.CENTER);
            return;
        }
        buildUI();
        load(null);
    }

    private void buildUI() {
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, SwingUtils.BORDER),
            BorderFactory.createEmptyBorder(16, 24, 12, 24)));
        header.add(SwingUtils.titleLabel("\uD83D\uDCDD  Audit Log"), BorderLayout.WEST);
        JButton refreshBtn = SwingUtils.outlineButton("\u21BB Refresh");
        refreshBtn.addActionListener(e -> load(filterField.getText()));
        header.add(refreshBtn, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Toolbar
        filterField = SwingUtils.styledField("Filter by keyword...");
        filterField.setPreferredSize(new Dimension(300, 34));
        JButton filterBtn = SwingUtils.primaryButton("Filter");
        JButton clearBtn  = SwingUtils.outlineButton("Clear");
        filterBtn.addActionListener(e -> load(filterField.getText()));
        filterField.addActionListener(e -> load(filterField.getText()));
        clearBtn.addActionListener(e -> { filterField.setText(""); load(null); });

        JPanel toolbar = SwingUtils.hBox(filterField, filterBtn, clearBtn);
        toolbar.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        // Table
        String[] cols = {"Timestamp", "Action", "Description"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        SwingUtils.styleTable(table);
        table.getColumnModel().getColumn(0).setPreferredWidth(160);
        table.getColumnModel().getColumn(0).setMaxWidth(200);
        table.getColumnModel().getColumn(1).setPreferredWidth(140);
        table.getColumnModel().getColumn(1).setMaxWidth(200);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(SwingUtils.BORDER));

        JLabel hint = new JLabel("Showing latest 500 entries. All times are local system time.");
        hint.setFont(SwingUtils.FONT_SMALL);
        hint.setForeground(SwingUtils.TEXT_GRAY);

        JPanel center = SwingUtils.card();
        center.setLayout(new BorderLayout(0, 8));
        center.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));
        center.add(toolbar, BorderLayout.NORTH);
        center.add(scroll,  BorderLayout.CENTER);
        center.add(hint,    BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);
    }

    private void load(String keyword) {
        model.setRowCount(0);
        List<String> lines = AuditLogger.readAll();

        // Show newest first
        for (int i = lines.size() - 1; i >= Math.max(0, lines.size() - 500); i--) {
            String line = lines.get(i);
            if (keyword != null && !keyword.isBlank() &&
                !line.toLowerCase().contains(keyword.toLowerCase())) continue;

            // Parse: [timestamp] ACTION | description
            try {
                String ts    = line.substring(1, line.indexOf(']'));
                String rest  = line.substring(line.indexOf(']') + 2);
                int    pipe  = rest.indexOf('|');
                String action = pipe >= 0 ? rest.substring(0, pipe).trim() : rest;
                String desc   = pipe >= 0 ? rest.substring(pipe + 1).trim() : "";
                model.addRow(new Object[]{ts, action, desc});
            } catch (Exception ex) {
                model.addRow(new Object[]{"", "RAW", line});
            }
        }
    }
}
