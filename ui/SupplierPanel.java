package com.pharmacy.ui;

import com.pharmacy.generics.Result;
import com.pharmacy.model.Supplier;
import com.pharmacy.service.InventoryService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Supplier management with gradient header, KPI cards, filter toolbar,
 * striped table with status badges, and styled add/edit dialog.
 */
public class SupplierPanel extends JPanel {

    private static final int PAD = 20;

    private final InventoryService service = InventoryService.getInstance();

    private JTable table;
    private DefaultTableModel model;
    private JTextField searchField;
    private JLabel statusLabel;

    private JLabel totalKpi;
    private JLabel activeKpi;
    private JLabel avgLeadKpi;

    public SupplierPanel() {
        setLayout(new BorderLayout());
        setBackground(SwingUtils.BG);
        buildUI();
        load(null);
    }

    // ── Layout ───────────────────────────────────────────────

    private void buildUI() {
        JButton addBtn = SwingUtils.successButton("+ Add Supplier");
        addBtn.setPreferredSize(new Dimension(160, 40));
        addBtn.addActionListener(e -> showDialog(null));

        add(buildGradientHeader(addBtn), BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(SwingUtils.BG);
        content.setBorder(new EmptyBorder(16, PAD, PAD, PAD));

        content.add(buildKpiRow());
        content.add(Box.createVerticalStrut(14));
        content.add(buildFilterCard());
        content.add(Box.createVerticalStrut(12));
        content.add(buildTableCard());
        content.add(Box.createVerticalStrut(8));
        content.add(buildStatusBar());

        add(SwingUtils.cleanScroll(content), BorderLayout.CENTER);
    }

    private JComponent buildGradientHeader(JButton addBtn) {
        JPanel header = new JPanel(new BorderLayout(16, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(
                        0, 0, SwingUtils.PRIMARY,
                        getWidth(), getHeight(), SwingUtils.PRIMARY_2);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(12, PAD, 12, PAD));
        header.setPreferredSize(new Dimension(0, 72));
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));

        JLabel title = new JLabel("Supplier Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Manage vendors, contacts and delivery lead times");
        subtitle.setFont(SwingUtils.FONT_BODY);
        subtitle.setForeground(new Color(255, 255, 255, 210));

        JPanel textCol = new JPanel();
        textCol.setOpaque(false);
        textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS));
        textCol.add(title);
        textCol.add(Box.createVerticalStrut(2));
        textCol.add(subtitle);

        header.add(textCol, BorderLayout.WEST);
        header.add(addBtn, BorderLayout.EAST);
        return header;
    }

    private JPanel buildKpiRow() {
        totalKpi = kpiValue("0", SwingUtils.PRIMARY);
        activeKpi = kpiValue("0", SwingUtils.SUCCESS);
        avgLeadKpi = kpiValue("0", SwingUtils.WARNING);

        JPanel row = new JPanel(new GridLayout(1, 3, 12, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        row.add(kpiCard(totalKpi, "Total Suppliers", "Σ", SwingUtils.SOFT_BLUE));
        row.add(kpiCard(activeKpi, "Active", "✓", SwingUtils.SOFT_GREEN));
        row.add(kpiCard(avgLeadKpi, "Avg. Lead Days", "⏱", SwingUtils.SOFT_ORANGE));
        return row;
    }

    private JLabel kpiValue(String text, Color color) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(new Font("Segoe UI", Font.BOLD, 24));
        l.setForeground(color);
        return l;
    }

    private JPanel kpiCard(JLabel value, String label, String marker, Color tint) {
        JPanel card = SwingUtils.card();
        card.setBackground(tint);
        card.setLayout(new BorderLayout(8, 4));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SwingUtils.BORDER, 1, true),
                new EmptyBorder(12, 14, 12, 14)));

        JLabel markerLbl = new JLabel(marker, SwingConstants.CENTER);
        markerLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        markerLbl.setOpaque(true);
        markerLbl.setBackground(Color.WHITE);
        markerLbl.setForeground(SwingUtils.PRIMARY);
        markerLbl.setBorder(new EmptyBorder(5, 9, 5, 9));

        JLabel nameLbl = new JLabel(label, SwingConstants.CENTER);
        nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        nameLbl.setForeground(SwingUtils.TEXT_GRAY);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        top.setOpaque(false);
        top.add(markerLbl);

        card.add(top, BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);
        card.add(nameLbl, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildFilterCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(SwingUtils.SOFT_BLUE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(190, 220, 232), 1, true),
                new EmptyBorder(12, 16, 12, 16)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));

        searchField = SwingUtils.styledField("Search by name, contact, phone or email...");
        searchField.setPreferredSize(new Dimension(320, 38));

        JButton searchBtn = SwingUtils.primaryButton("Search");
        JButton refreshBtn = SwingUtils.outlineButton("Refresh");
        JButton editBtn = SwingUtils.outlineButton("Edit");
        JButton deleteBtn = SwingUtils.dangerButton("Deactivate");

        searchBtn.addActionListener(e -> load(searchField.getText()));
        searchField.addActionListener(e -> load(searchField.getText()));
        refreshBtn.addActionListener(e -> {
            searchField.setText("");
            load(null);
        });
        editBtn.addActionListener(e -> editSelected());
        deleteBtn.addActionListener(e -> deleteSelected());

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        left.add(wrapField("Search", searchField));
        left.add(searchBtn);
        left.add(refreshBtn);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(editBtn);
        right.add(deleteBtn);

        card.add(left, BorderLayout.WEST);
        card.add(right, BorderLayout.EAST);
        return card;
    }

    private JPanel buildTableCard() {
        JPanel card = SwingUtils.card();
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SwingUtils.BORDER, 1, true),
                new EmptyBorder(16, 18, 16, 18)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 480));

        card.add(SwingUtils.headingLabel("Supplier Directory"), BorderLayout.NORTH);

        String[] cols = {"ID", "Name", "Contact", "Phone", "Email", "Categories", "Lead Days", "Status"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        table = new JTable(model);
        SwingUtils.styleTable(table);
        table.setRowHeight(38);
        table.setAutoCreateRowSorter(true);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(7).setMaxWidth(90);
        table.getColumnModel().getColumn(7).setCellRenderer(new StatusBadgeRenderer());
        table.setDefaultRenderer(Object.class, new StripedRenderer());

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(SwingUtils.BORDER, 1, true));
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setPreferredSize(new Dimension(0, 360));
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    private JLabel buildStatusBar() {
        statusLabel = SwingUtils.bodyLabel("Ready");
        statusLabel.setForeground(SwingUtils.TEXT_GRAY);
        statusLabel.setBorder(new EmptyBorder(4, 4, 0, 4));
        statusLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        return statusLabel;
    }

    private JPanel wrapField(String label, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(SwingUtils.TEXT_GRAY);
        p.add(lbl, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    // ── Data ─────────────────────────────────────────────────

    private void load(String kw) {
        model.setRowCount(0);
        List<Supplier> list = (kw == null || kw.isBlank())
                ? service.getAllSuppliers()
                : service.searchSuppliers(kw);

        int leadSum = 0;
        for (Supplier s : list) {
            leadSum += s.getLeadDays();
            model.addRow(new Object[]{
                    s.getId(),
                    s.getName(),
                    s.getContactName(),
                    s.getPhone(),
                    s.getEmail(),
                    s.getSuppliedCategories().isEmpty()
                            ? "—"
                            : String.join(", ", s.getSuppliedCategories()),
                    s.getLeadDays() + " days",
                    s.isActive() ? "Active" : "Inactive"
            });
        }

        totalKpi.setText(String.valueOf(list.size()));
        activeKpi.setText(String.valueOf(list.stream().filter(Supplier::isActive).count()));
        avgLeadKpi.setText(list.isEmpty() ? "0" : String.valueOf(leadSum / list.size()));

        statusLabel.setText(list.size() + " supplier(s) loaded.");
    }

    private Supplier getSelected() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            SwingUtils.showError(this, "Select a supplier first.");
            return null;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        int id = (int) model.getValueAt(modelRow, 0);
        return service.getAllSuppliers().stream()
                .filter(s -> s.getId() == id)
                .findFirst()
                .orElse(null);
    }

    private void editSelected() {
        Supplier s = getSelected();
        if (s != null) showDialog(s);
    }

    private void deleteSelected() {
        Supplier s = getSelected();
        if (s == null) return;
        if (!SwingUtils.confirm(this, "Deactivate supplier '" + s.getName() + "'?")) return;
        Result<Void> r = service.deleteSupplier(s.getId());
        if (r.isFailure()) SwingUtils.showError(this, r.getErrorMessage());
        else load(null);
    }

    // ── Add / Edit dialog ────────────────────────────────────

    private void showDialog(Supplier existing) {
        boolean isNew = existing == null;
        JDialog dlg = styledDialog(isNew ? "Add Supplier" : "Edit Supplier", 520, 480);

        JTextField nameF = SwingUtils.styledField("Company name");
        JTextField contactF = SwingUtils.styledField("Contact person");
        JTextField phoneF = SwingUtils.styledField("Phone");
        JTextField emailF = SwingUtils.styledField("Email");
        JTextField addrF = SwingUtils.styledField("Address");
        JTextField categoriesF = SwingUtils.styledField("Comma-separated categories");
        JTextField leadF = SwingUtils.styledField("Lead days (e.g. 7)");

        if (!isNew) {
            nameF.setText(existing.getName());
            contactF.setText(existing.getContactName() != null ? existing.getContactName() : "");
            phoneF.setText(existing.getPhone() != null ? existing.getPhone() : "");
            emailF.setText(existing.getEmail() != null ? existing.getEmail() : "");
            addrF.setText(existing.getAddress() != null ? existing.getAddress() : "");
            categoriesF.setText(String.join(", ", existing.getSuppliedCategories()));
            leadF.setText(String.valueOf(existing.getLeadDays()));
        }

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(16, 22, 16, 22));
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(5, 8, 5, 8);

        Object[][] rows = {
                {"Company Name *", nameF},
                {"Contact Person", contactF},
                {"Phone", phoneF},
                {"Email", emailF},
                {"Address", addrF},
                {"Categories", categoriesF},
                {"Lead Days", leadF}
        };
        for (int i = 0; i < rows.length; i++) {
            gc.gridx = 0;
            gc.gridy = i;
            gc.weightx = 0.32;
            form.add(SwingUtils.fieldLabel((String) rows[i][0]), gc);
            gc.gridx = 1;
            gc.weightx = 0.68;
            form.add((Component) rows[i][1], gc);
        }

        JButton saveBtn = SwingUtils.successButton("Save Supplier");
        JButton cancelBtn = SwingUtils.outlineButton("Cancel");
        cancelBtn.addActionListener(e -> dlg.dispose());
        saveBtn.addActionListener(e -> {
            if (nameF.getText().trim().isEmpty()) {
                SwingUtils.showError(dlg, "Company name is required.");
                return;
            }
            Supplier s = isNew ? new Supplier() : existing;
            s.setName(nameF.getText().trim());
            s.setContactName(contactF.getText().trim());
            s.setPhone(phoneF.getText().trim());
            s.setEmail(emailF.getText().trim());
            s.setAddress(addrF.getText().trim());
            s.setSuppliedCategories(new java.util.ArrayList<>());
            java.util.Arrays.stream(categoriesF.getText().split(","))
                    .map(String::trim)
                    .filter(v -> !v.isEmpty())
                    .forEach(s::addCategory);
            try {
                s.setLeadDays(Integer.parseInt(leadF.getText().trim()));
            } catch (Exception ignored) {
                s.setLeadDays(7);
            }

            Result<Supplier> r = isNew ? service.addSupplier(s) : service.updateSupplier(s);
            if (r.isFailure()) SwingUtils.showError(dlg, r.getErrorMessage());
            else {
                dlg.dispose();
                load(null);
            }
        });

        dlg.add(new JScrollPane(form), BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);
        btnRow.setBorder(new EmptyBorder(10, 16, 16, 16));
        btnRow.add(cancelBtn);
        btnRow.add(saveBtn);
        dlg.add(btnRow, BorderLayout.SOUTH);

        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private JDialog styledDialog(String title, int width, int height) {
        JDialog dlg = new JDialog(
                SwingUtilities.getWindowAncestor(this),
                title,
                Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setLayout(new BorderLayout());
        dlg.getContentPane().setBackground(Color.WHITE);
        dlg.setSize(width, height);

        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setBackground(SwingUtils.PRIMARY);
        titleBar.setBorder(new EmptyBorder(12, 16, 12, 16));
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titleLbl.setForeground(Color.WHITE);
        titleBar.add(titleLbl, BorderLayout.WEST);
        dlg.add(titleBar, BorderLayout.NORTH);
        return dlg;
    }

    // ── Table renderers ──────────────────────────────────────

    private static class StripedRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean selected, boolean focused, int row, int col) {
            Component c = super.getTableCellRendererComponent(table, value, selected, focused, row, col);
            if (!selected) {
                c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(249, 251, 253));
            }
            setBorder(new EmptyBorder(0, 10, 0, 10));
            return c;
        }
    }

    private static class StatusBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean selected, boolean focused, int row, int col) {
            String status = value == null ? "" : value.toString();
            boolean active = "Active".equalsIgnoreCase(status);
            Color bg = active ? SwingUtils.SUCCESS : SwingUtils.TEXT_GRAY;
            JLabel badge = SwingUtils.badge(status.isEmpty() ? "—" : status, bg);
            badge.setFont(new Font("Segoe UI", Font.BOLD, 10));
            badge.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));

            JPanel wrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 6));
            wrap.setOpaque(true);
            wrap.setBackground(selected
                    ? table.getSelectionBackground()
                    : (row % 2 == 0 ? Color.WHITE : new Color(249, 251, 253)));
            wrap.add(badge);
            return wrap;
        }
    }
}
