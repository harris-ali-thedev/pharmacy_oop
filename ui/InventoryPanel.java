package com.pharmacy.ui;

import com.pharmacy.dao.SupplierDAO;
import com.pharmacy.generics.Result;
import com.pharmacy.model.*;
import com.pharmacy.service.InventoryService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Inventory management panel with KPI summary cards, gradient header,
 * grouped toolbar, badge renderers, striped table, and styled dialogs.
 */
public class InventoryPanel extends JPanel {

    private static final int PAD = 20;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private final InventoryService service = InventoryService.getInstance();
    private final SupplierDAO supDAO = SupplierDAO.getInstance();

    private JTable table;
    private DefaultTableModel model;
    private JTextField searchField;
    private JComboBox<String> typeFilterBox;
    private JLabel statusLabel;

    private JLabel statTotalVal;
    private JLabel statLowVal;
    private JLabel statExpiredVal;
    private JLabel statValueVal;

    public InventoryPanel() {
        setLayout(new BorderLayout());
        setBackground(SwingUtils.BG);
        buildUI();
        loadAll();
    }

    // ── Layout ───────────────────────────────────────────────

    private void buildUI() {
        JButton addBtn = SwingUtils.successButton("+ Add Medicine");
        addBtn.setPreferredSize(new Dimension(168, 40));
        addBtn.addActionListener(e -> showDialog(null));

        add(buildGradientHeader(addBtn), BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(SwingUtils.BG);
        content.setBorder(new EmptyBorder(18, PAD, PAD, PAD));
        content.add(buildKpiRow());
        content.add(Box.createVerticalStrut(16));
        content.add(buildFilterCard());
        content.add(Box.createVerticalStrut(12));
        content.add(buildTableSection());
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
        header.setBorder(new EmptyBorder(22, PAD, 20, PAD));
        header.setPreferredSize(new Dimension(0, 72));
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));

        JLabel title = new JLabel("Inventory Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Track medicines, stock levels, and expiry alerts");
        subtitle.setFont(SwingUtils.FONT_BODY);
        subtitle.setForeground(new Color(255, 255, 255, 210));

        JPanel textCol = new JPanel();
        textCol.setOpaque(false);
        textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS));
        textCol.add(title);
        textCol.add(Box.createVerticalStrut(4));
        textCol.add(subtitle);

        addBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 80), 1, true),
                BorderFactory.createEmptyBorder(10, 18, 10, 18)));

        header.add(textCol, BorderLayout.WEST);
        header.add(addBtn, BorderLayout.EAST);
        return header;
    }

    private JPanel buildKpiRow() {
        statTotalVal = kpiNumber("0", SwingUtils.PRIMARY);
        statLowVal = kpiNumber("0", SwingUtils.WARNING);
        statExpiredVal = kpiNumber("0", SwingUtils.DANGER);
        statValueVal = kpiNumber("Rs 0", SwingUtils.PRIMARY_2);

        JPanel row = new JPanel(new GridLayout(1, 4, 12, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 108));
        row.add(kpiCard(statTotalVal, "Total Medicines", SwingUtils.SOFT_BLUE, "Rx"));
        row.add(kpiCard(statLowVal, "Low Stock", SwingUtils.SOFT_ORANGE, "!"));
        row.add(kpiCard(statExpiredVal, "Expired", new Color(254, 235, 235), "X"));
        row.add(kpiCard(statValueVal, "Inventory Value", SwingUtils.SOFT_GREEN, "Rs"));
        return row;
    }

    private JLabel kpiNumber(String text, Color color) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(new Font("Segoe UI", Font.BOLD, 26));
        l.setForeground(color);
        return l;
    }

    private JPanel kpiCard(JLabel value, String label, Color tint, String icon) {
        JPanel card = SwingUtils.card();
        card.setBackground(tint);
        card.setLayout(new BorderLayout(10, 4));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SwingUtils.BORDER, 1, true),
                new EmptyBorder(14, 16, 14, 16)));

        JLabel iconLbl = new JLabel(icon, SwingConstants.CENTER);
        iconLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        iconLbl.setOpaque(true);
        iconLbl.setBackground(Color.WHITE);
        iconLbl.setForeground(SwingUtils.PRIMARY);
        iconLbl.setBorder(new EmptyBorder(6, 10, 6, 10));

        JLabel nameLbl = new JLabel(label, SwingConstants.CENTER);
        nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        nameLbl.setForeground(SwingUtils.TEXT_GRAY);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        top.setOpaque(false);
        top.add(iconLbl);

        card.add(top, BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);
        card.add(nameLbl, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildFilterCard() {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(SwingUtils.SOFT_BLUE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(190, 220, 232), 1, true),
                new EmptyBorder(14, 16, 14, 16)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));

        card.add(sectionLabel("Search & Filters"), BorderLayout.NORTH);

        searchField = SwingUtils.styledField("Search by name, category or barcode");
        searchField.setPreferredSize(new Dimension(320, 38));

        typeFilterBox = SwingUtils.styledCombo("All Types", "OTC", "Prescription", "Narcotic", "General");
        typeFilterBox.setPreferredSize(new Dimension(150, 38));

        JButton searchBtn = SwingUtils.primaryButton("Search");
        JButton refreshBtn = SwingUtils.outlineButton("Refresh");
        JButton lowStockBtn = SwingUtils.warningButton("Low Stock");
        JButton expiryBtn = SwingUtils.warningButton("Expiry Alerts");

        searchBtn.addActionListener(e -> doSearch());
        searchField.addActionListener(e -> doSearch());
        typeFilterBox.addActionListener(e -> doSearch());
        refreshBtn.addActionListener(e -> loadAll());
        lowStockBtn.addActionListener(e -> loadLowStock());
        expiryBtn.addActionListener(e -> loadExpiryAlerts());

        new MedicineSuggestionPopup(searchField, service::search, medicine -> populate(List.of(medicine)));

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filters.setOpaque(false);
        filters.add(wrapField("Search", searchField));
        filters.add(wrapField("Type", typeFilterBox));
        filters.add(searchBtn);
        filters.add(refreshBtn);
        filters.add(verticalDivider());
        filters.add(lowStockBtn);
        filters.add(expiryBtn);

        card.add(filters, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildActionBar() {
        JButton addStockBtn = SwingUtils.successButton("+ Add Stock");
        JButton detailsBtn = SwingUtils.outlineButton("Details");
        JButton editBtn = SwingUtils.outlineButton("Edit");
        JButton deleteBtn = SwingUtils.dangerButton("Delete");

        addStockBtn.addActionListener(e -> addStockDialog());
        detailsBtn.addActionListener(e -> showSelectedDetails());
        editBtn.addActionListener(e -> editSelected());
        deleteBtn.addActionListener(e -> deleteSelected());

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(0, 0, 8, 0));
        bar.add(SwingUtils.bodyLabel("Row actions:"));
        bar.add(addStockBtn);
        bar.add(detailsBtn);
        bar.add(editBtn);
        bar.add(deleteBtn);
        return bar;
    }

    private JPanel buildTableSection() {
        JPanel section = SwingUtils.card();
        section.setLayout(new BorderLayout(0, 0));
        section.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SwingUtils.BORDER, 1, true),
                new EmptyBorder(16, 18, 16, 18)));

        JPanel north = new JPanel(new BorderLayout());
        north.setOpaque(false);
        north.add(sectionLabel("Medicine Catalog"), BorderLayout.WEST);
        north.add(buildActionBar(), BorderLayout.EAST);
        section.add(north, BorderLayout.NORTH);

        String[] cols = {"ID", "Type", "Name", "Generic", "Category", "Price (Rs)",
                "Stock", "Reorder", "Expiry", "Narcotic", "Supplier"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int c) {
                return c == 9 ? Boolean.class : String.class;
            }
        };

        table = new JTable(model);
        SwingUtils.styleTable(table);
        table.setRowHeight(38);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);

        table.getColumnModel().getColumn(0).setMaxWidth(55);
        table.getColumnModel().getColumn(1).setPreferredWidth(110);
        table.getColumnModel().getColumn(1).setCellRenderer(new TypeBadgeRenderer());
        table.getColumnModel().getColumn(6).setCellRenderer(new StockRenderer());
        table.getColumnModel().getColumn(8).setCellRenderer(new ExpiryRenderer());
        table.setDefaultRenderer(Object.class, new StripedRenderer());

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) {
                    showSelectedDetails();
                }
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(SwingUtils.BORDER, 1, true));
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setPreferredSize(new Dimension(0, 420));
        section.add(scroll, BorderLayout.CENTER);
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, 520));
        return section;
    }

    private JComponent buildStatusBar() {
        statusLabel = SwingUtils.bodyLabel("Ready");
        statusLabel.setForeground(SwingUtils.TEXT_GRAY);
        statusLabel.setBorder(new EmptyBorder(4, 4, 0, 4));
        statusLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        return statusLabel;
    }

    private JLabel sectionLabel(String text) {
        JLabel l = SwingUtils.headingLabel(text);
        l.setBorder(new EmptyBorder(0, 0, 8, 0));
        return l;
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

    private Component verticalDivider() {
        JSeparator sep = new JSeparator(SwingConstants.VERTICAL);
        sep.setPreferredSize(new Dimension(2, 32));
        sep.setForeground(SwingUtils.BORDER);
        return sep;
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

    private static class TypeBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean selected, boolean focused, int row, int col) {
            String type = value == null ? "" : value.toString();
            Color bg = switch (type) {
                case "OTC" -> SwingUtils.SUCCESS;
                case "Prescription" -> SwingUtils.PRIMARY;
                case "Narcotic" -> SwingUtils.DANGER;
                default -> SwingUtils.TEXT_GRAY;
            };
            JLabel badge = SwingUtils.badge(type.isEmpty() ? "—" : type, bg);
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

    private class StockRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean selected, boolean focused, int row, int col) {
            Component c = super.getTableCellRendererComponent(table, value, selected, focused, row, col);
            setHorizontalAlignment(SwingConstants.CENTER);
            setBorder(new EmptyBorder(0, 8, 0, 8));

            int modelRow = table.convertRowIndexToModel(row);
            int stock = parseIntSafe(value);
            int reorder = parseIntSafe(model.getValueAt(modelRow, 7));

            if (!selected) {
                if (stock <= 0) {
                    setForeground(SwingUtils.DANGER);
                    setFont(new Font("Segoe UI", Font.BOLD, 13));
                } else if (stock <= reorder) {
                    setForeground(SwingUtils.WARNING);
                    setFont(new Font("Segoe UI", Font.BOLD, 13));
                } else {
                    setForeground(SwingUtils.SUCCESS);
                    setFont(SwingUtils.FONT_BODY);
                }
            }
            return c;
        }
    }

    private static class ExpiryRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean selected, boolean focused, int row, int col) {
            Component c = super.getTableCellRendererComponent(table, value, selected, focused, row, col);
            setBorder(new EmptyBorder(0, 10, 0, 10));
            String text = value == null ? "" : value.toString();
            if (!text.isBlank() && !selected) {
                try {
                    LocalDate exp = LocalDate.parse(text);
                    if (exp.isBefore(LocalDate.now())) {
                        setForeground(SwingUtils.DANGER);
                        setFont(new Font("Segoe UI", Font.BOLD, 12));
                    } else if (!exp.isAfter(LocalDate.now().plusDays(30))) {
                        setForeground(SwingUtils.WARNING);
                    }
                } catch (Exception ignored) { /* keep default */ }
            }
            return c;
        }
    }

    private static int parseIntSafe(Object v) {
        if (v instanceof Number n) return n.intValue();
        try {
            return Integer.parseInt(String.valueOf(v));
        } catch (Exception e) {
            return 0;
        }
    }

    // ── Data operations ──────────────────────────────────────

    private void loadAll() {
        if (searchField != null) searchField.setText("");
        if (typeFilterBox != null) typeFilterBox.setSelectedItem("All Types");
        populate(service.getAllActive());
        refreshGlobalStats();
        statusLabel.setText("Showing all active medicines.");
    }

    private void doSearch() {
        String kw = searchField.getText().trim();
        List<Medicine> list = kw.isEmpty() ? service.getAllActive() : service.search(kw);
        populate(filterByType(list));
    }

    private List<Medicine> filterByType(List<Medicine> list) {
        if (typeFilterBox == null) return list;
        String type = (String) typeFilterBox.getSelectedItem();
        if (type == null || "All Types".equals(type)) return list;
        return list.stream().filter(m -> medicineType(m).equals(type)).toList();
    }

    private void loadLowStock() {
        populate(service.getLowStockMedicines());
        statusLabel.setText("Showing low-stock medicines.");
    }

    private void loadExpiryAlerts() {
        populate(service.getAllActive().stream().filter(Medicine::isExpired).toList());
        statusLabel.setText("Showing expired medicines.");
    }

    private void populate(List<Medicine> list) {
        model.setRowCount(0);
        for (Medicine m : list) {
            model.addRow(new Object[]{
                    m.getId(),
                    medicineType(m),
                    m.getName(),
                    m.getGenericName(),
                    m.getCategory(),
                    String.format("%,.2f", m.getUnitPrice()),
                    m.getStockQty(),
                    m.getReorderLevel(),
                    m.getExpiryDate() != null ? m.getExpiryDate().toString() : "",
                    m.isNarcotic(),
                    m.getSupplierName()
            });
        }
        statusLabel.setText(list.size() + " medicine(s) in view.");
    }

    private void refreshGlobalStats() {
        List<Medicine> all = service.getAllActive();
        statTotalVal.setText(String.valueOf(all.size()));
        statLowVal.setText(String.valueOf(service.getLowStockMedicines().size()));
        statExpiredVal.setText(String.valueOf(
                all.stream().filter(Medicine::isExpired).count()));
        double value = all.stream().mapToDouble(m -> m.getUnitPrice() * m.getStockQty()).sum();
        statValueVal.setText(value >= 1_000_000
                ? String.format("Rs %.1fM", value / 1_000_000)
                : String.format("Rs %,.0f", value));
    }

    private Medicine getSelected() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            SwingUtils.showError(this, "Please select a medicine first.");
            return null;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        int id = (int) model.getValueAt(modelRow, 0);
        return service.getAllActive().stream()
                .filter(m -> m.getId() == id)
                .findFirst()
                .orElse(null);
    }

    private String medicineType(Medicine m) {
        if (m instanceof NarcoticMedicine) return "Narcotic";
        if (m instanceof PrescriptionMedicine) return "Prescription";
        if (m instanceof OTCMedicine) return "OTC";
        if (m.isNarcotic()) return "Narcotic";
        if (m.isScheduled()) return "Prescription";
        return "General";
    }

    private void showSelectedDetails() {
        Medicine m = getSelected();
        if (m == null) return;

        JDialog dlg = styledDialog("Medicine Details", 480, 420);
        JTextArea area = new JTextArea(buildDetailText(m));
        area.setEditable(false);
        area.setFont(SwingUtils.FONT_MONO);
        area.setBackground(new Color(248, 250, 252));
        area.setBorder(new EmptyBorder(12, 14, 12, 14));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);

        dlg.add(new JScrollPane(area), BorderLayout.CENTER);
        JButton closeBtn = SwingUtils.primaryButton("Close");
        closeBtn.addActionListener(e -> dlg.dispose());
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.setOpaque(false);
        south.setBorder(new EmptyBorder(8, 12, 12, 12));
        south.add(closeBtn);
        dlg.add(south, BorderLayout.SOUTH);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private String buildDetailText(Medicine m) {
        StringBuilder sb = new StringBuilder();
        sb.append(m.getDetails()).append("\n\n");
        sb.append("Medicine ID: ").append(m.getMedicineID()).append("\n");
        sb.append("Supplier ID: ").append(m.getSupplierID()).append("\n");
        sb.append("Expired: ").append(m.isExpired() ? "Yes" : "No").append("\n");
        if (m instanceof OTCMedicine otc) {
            sb.append("Discount eligible: ").append(otc.isDiscountEligible() ? "Yes" : "No").append("\n");
            sb.append(String.format("Discounted price: Rs %.2f%n", otc.getDiscountedPrice()));
        } else if (m instanceof PrescriptionMedicine rx) {
            sb.append("Age verification: ")
                    .append(rx.isRequiresAgeVerification() ? "Required" : "No").append("\n");
        } else if (m instanceof NarcoticMedicine narcotic) {
            sb.append("DEA schedule: ").append(narcotic.getDeaScheduleLevel()).append("\n");
            sb.append("Dispense limit: ").append(narcotic.getDispenseLimit()).append("\n");
        }
        return sb.toString();
    }

    private void editSelected() {
        Medicine m = getSelected();
        if (m != null) showDialog(m);
    }

    private void deleteSelected() {
        Medicine m = getSelected();
        if (m == null) return;
        if (!SwingUtils.confirm(this, "Delete '" + m.getName() + "'? This cannot be undone.")) return;
        Result<Void> r = service.deleteMedicine(m.getId());
        if (r.isFailure()) SwingUtils.showError(this, r.getErrorMessage());
        else loadAll();
    }

    private void addStockDialog() {
        Medicine m = getSelected();
        if (m == null) return;

        JDialog dlg = styledDialog("Add Stock", 420, 280);
        JTextField qtyField = SwingUtils.styledField("Quantity to add");
        JTextField batchField = SwingUtils.styledField("Batch / lot number (optional)");

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(16, 20, 8, 20));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;

        addFormRow(form, gc, 0, "Medicine", new JLabel(m.getName()));
        addFormRow(form, gc, 1, "Current Stock", new JLabel(String.valueOf(m.getStockQty())));
        addFormRow(form, gc, 2, "Add Quantity *", qtyField);
        addFormRow(form, gc, 3, "Batch Note", batchField);

        dlg.add(form, BorderLayout.CENTER);

        JButton saveBtn = SwingUtils.successButton("Update Stock");
        JButton cancelBtn = SwingUtils.outlineButton("Cancel");
        cancelBtn.addActionListener(e -> dlg.dispose());
        saveBtn.addActionListener(e -> {
            try {
                int qty = Integer.parseInt(qtyField.getText().trim());
                Result<Void> r = service.addStock(m.getId(), qty, batchField.getText().trim());
                if (r.isFailure()) SwingUtils.showError(dlg, r.getErrorMessage());
                else {
                    SwingUtils.showInfo(dlg, "Stock updated successfully.");
                    dlg.dispose();
                    loadAll();
                }
            } catch (NumberFormatException ex) {
                SwingUtils.showError(dlg, "Please enter a valid quantity.");
            }
        });

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setOpaque(false);
        btnRow.setBorder(new EmptyBorder(8, 12, 14, 12));
        btnRow.add(cancelBtn);
        btnRow.add(saveBtn);
        dlg.add(btnRow, BorderLayout.SOUTH);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    // ── Add / Edit dialog ────────────────────────────────────

    private void showDialog(Medicine existing) {
        boolean isNew = existing == null;
        JDialog dlg = styledDialog(isNew ? "Add Medicine" : "Edit Medicine", 860, 640);

        JTextField nameF = SwingUtils.styledField("Medicine name");
        JTextField genericF = SwingUtils.styledField("Generic / active ingredient");
        JTextField categoryF = SwingUtils.styledField("e.g. Antibiotic");
        JTextField barcodeF = SwingUtils.styledField("Barcode");
        JTextField priceF = SwingUtils.styledField("Unit price");
        JTextField costF = SwingUtils.styledField("Unit cost");
        JTextField stockF = SwingUtils.styledField("Stock quantity");
        JTextField reorderF = SwingUtils.styledField("Reorder level");
        JTextField mfgF = SwingUtils.styledField("Manufacturer");
        JTextField strengthF = SwingUtils.styledField("e.g. 500mg");
        JTextField expiryF = SwingUtils.styledField("YYYY-MM-DD");
        JComboBox<String> typeBox = SwingUtils.styledCombo("General", "OTC", "Prescription", "Narcotic");
        JTextField discountRateF = SwingUtils.styledField("OTC discount %");
        JTextField approvalCodeF = SwingUtils.styledField("Doctor approval code");
        JCheckBox ageVerifyCb = styledCheckBox("Requires age verification");
        JTextField scheduleF = SwingUtils.styledField("DEA schedule level");
        JTextField dispenseLimitF = SwingUtils.styledField("Dispense limit");
        JCheckBox narcoticCb = styledCheckBox("Narcotic / Controlled");
        JCheckBox scheduledCb = styledCheckBox("Scheduled (Rx required)");

        Runnable applyTypeFlags = () -> {
            String type = (String) typeBox.getSelectedItem();
            narcoticCb.setSelected("Narcotic".equals(type));
            scheduledCb.setSelected("Narcotic".equals(type) || "Prescription".equals(type));
        };
        typeBox.addActionListener(e -> applyTypeFlags.run());

        List<Supplier> suppliers = supDAO.findActive();
        String[] supNames = suppliers.stream().map(Supplier::getName).toArray(String[]::new);
        JComboBox<String> supBox = SwingUtils.styledCombo(supNames);

        if (!isNew) {
            nameF.setText(existing.getName());
            genericF.setText(existing.getGenericName() != null ? existing.getGenericName() : "");
            categoryF.setText(existing.getCategory() != null ? existing.getCategory() : "");
            barcodeF.setText(existing.getBarcode() != null ? existing.getBarcode() : "");
            priceF.setText(String.valueOf(existing.getUnitPrice()));
            costF.setText(String.valueOf(existing.getUnitCost()));
            stockF.setText(String.valueOf(existing.getStockQty()));
            reorderF.setText(String.valueOf(existing.getReorderLevel()));
            mfgF.setText(existing.getManufacturer() != null ? existing.getManufacturer() : "");
            strengthF.setText(existing.getStrength() != null ? existing.getStrength() : "");
            expiryF.setText(existing.getExpiryDate() != null ? existing.getExpiryDate().toString() : "");
            typeBox.setSelectedItem(medicineType(existing));
            typeBox.setEnabled(false);
            narcoticCb.setSelected(existing.isNarcotic());
            scheduledCb.setSelected(existing.isScheduled());
            if (existing instanceof OTCMedicine otc) {
                discountRateF.setText(String.valueOf(otc.getDiscountRate()));
            }
            if (existing instanceof PrescriptionMedicine rx) {
                approvalCodeF.setText(rx.getDoctorApprovalCode() != null ? rx.getDoctorApprovalCode() : "");
                ageVerifyCb.setSelected(rx.isRequiresAgeVerification());
            }
            if (existing instanceof NarcoticMedicine narcotic) {
                scheduleF.setText(String.valueOf(narcotic.getDeaScheduleLevel()));
                dispenseLimitF.setText(String.valueOf(narcotic.getDispenseLimit()));
            }
            String supName = existing.getSupplierName();
            if (supName != null) supBox.setSelectedItem(supName);
        } else {
            typeBox.setSelectedItem("OTC");
            applyTypeFlags.run();
        }

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(16, 22, 16, 22));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(5, 8, 5, 8);
        gc.fill = GridBagConstraints.HORIZONTAL;

        Object[][] rows = {
                {"Medicine Type", typeBox},
                {"Name *", nameF},
                {"Generic Name", genericF},
                {"Category", categoryF},
                {"Barcode", barcodeF},
                {"Unit Price *", priceF},
                {"Unit Cost", costF},
                {"Stock Qty", stockF},
                {"Reorder Level", reorderF},
                {"Manufacturer", mfgF},
                {"Strength", strengthF},
                {"Expiry Date", expiryF},
                {"Supplier", supBox},
                {"OTC Discount %", discountRateF},
                {"Approval Code", approvalCodeF},
                {"DEA Schedule", scheduleF},
                {"Dispense Limit", dispenseLimitF},
        };

        for (int i = 0; i < rows.length; i++) {
            gc.gridx = 0;
            gc.gridy = i;
            gc.weightx = 0.28;
            form.add(SwingUtils.fieldLabel((String) rows[i][0]), gc);
            gc.gridx = 1;
            gc.weightx = 0.72;
            form.add((Component) rows[i][1], gc);
        }

        gc.gridx = 1;
        gc.gridy = rows.length;
        form.add(narcoticCb, gc);
        gc.gridy++;
        form.add(scheduledCb, gc);
        gc.gridy++;
        form.add(ageVerifyCb, gc);

        JButton saveBtn = SwingUtils.successButton("Save Medicine");
        JButton cancelBtn = SwingUtils.outlineButton("Cancel");
        cancelBtn.addActionListener(e -> dlg.dispose());

        saveBtn.addActionListener(e -> {
            if (nameF.getText().trim().isEmpty()) {
                SwingUtils.showError(dlg, "Medicine name is required.");
                return;
            }
            Medicine m = isNew ? createMedicineByType((String) typeBox.getSelectedItem()) : existing;
            m.setName(nameF.getText().trim());
            m.setGenericName(genericF.getText().trim());
            m.setCategory(categoryF.getText().trim());
            m.setBarcode(barcodeF.getText().trim());
            m.setManufacturer(mfgF.getText().trim());
            m.setStrength(strengthF.getText().trim());
            m.setNarcotic(narcoticCb.isSelected());
            m.setScheduled(scheduledCb.isSelected());
            if (!expiryF.getText().trim().isEmpty()) {
                try {
                    m.setExpiryDate(LocalDate.parse(expiryF.getText().trim()));
                } catch (DateTimeParseException ex) {
                    SwingUtils.showError(dlg, "Expiry date must use YYYY-MM-DD format.");
                    return;
                }
            } else {
                m.setExpiryDate(null);
            }
            try {
                m.setUnitPrice(Double.parseDouble(priceF.getText()));
            } catch (Exception ignored) { /* keep existing */ }
            try {
                m.setUnitCost(Double.parseDouble(costF.getText()));
            } catch (Exception ignored) { /* keep existing */ }
            try {
                m.setStockQty(Integer.parseInt(stockF.getText()));
            } catch (Exception ignored) { /* keep existing */ }
            try {
                m.setReorderLevel(Integer.parseInt(reorderF.getText()));
            } catch (Exception ignored) { /* keep existing */ }

            int si = supBox.getSelectedIndex();
            if (si >= 0 && si < suppliers.size()) {
                m.setSupplierId(suppliers.get(si).getId());
                m.setSupplierName(suppliers.get(si).getName());
            }

            if (m instanceof OTCMedicine otc) {
                try {
                    otc.setDiscountRate(Double.parseDouble(discountRateF.getText().trim()));
                } catch (Exception ignored) { /* keep existing */ }
            }
            if (m instanceof PrescriptionMedicine rx) {
                rx.setDoctorApprovalCode(approvalCodeF.getText().trim());
                rx.setRequiresAgeVerification(ageVerifyCb.isSelected());
            }
            if (m instanceof NarcoticMedicine narcotic) {
                try {
                    narcotic.setDeaScheduleLevel(Integer.parseInt(scheduleF.getText().trim()));
                } catch (Exception ignored) { /* keep existing */ }
                try {
                    narcotic.setDispenseLimit(Integer.parseInt(dispenseLimitF.getText().trim()));
                } catch (Exception ignored) { /* keep existing */ }
            }

            Result<?> r = isNew ? service.addMedicine(m) : service.updateMedicine(m);
            if (r.isFailure()) SwingUtils.showError(dlg, r.getErrorMessage());
            else {
                dlg.dispose();
                loadAll();
            }
        });

        dlg.add(new JScrollPane(form), BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);
        btnRow.setBorder(new EmptyBorder(10, 16, 16, 16));
        btnRow.setBackground(Color.WHITE);
        btnRow.add(cancelBtn);
        btnRow.add(saveBtn);
        dlg.add(btnRow, BorderLayout.SOUTH);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private Medicine createMedicineByType(String type) {
        if ("OTC".equals(type)) return new OTCMedicine();
        if ("Prescription".equals(type)) return new PrescriptionMedicine();
        if ("Narcotic".equals(type)) return new NarcoticMedicine();
        return new Medicine();
    }

    // ── Dialog helpers ───────────────────────────────────────

    private JDialog styledDialog(String title, int width, int height) {
        JDialog dlg = new JDialog(
                SwingUtilities.getWindowAncestor(this),
                title,
                Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setLayout(new BorderLayout());
        dlg.getContentPane().setBackground(Color.WHITE);

        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setBackground(SwingUtils.PRIMARY);
        titleBar.setBorder(new EmptyBorder(14, 18, 14, 18));
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLbl.setForeground(Color.WHITE);
        titleBar.add(titleLbl, BorderLayout.WEST);
        dlg.add(titleBar, BorderLayout.NORTH);

        dlg.setSize(width, height);
        return dlg;
    }

    private JCheckBox styledCheckBox(String text) {
        JCheckBox cb = new JCheckBox(text);
        cb.setFont(SwingUtils.FONT_BODY);
        cb.setForeground(SwingUtils.TEXT_DARK);
        cb.setOpaque(false);
        cb.setFocusPainted(false);
        return cb;
    }

    private void addFormRow(JPanel form, GridBagConstraints gc, int row, String label, Component field) {
        gc.gridx = 0;
        gc.gridy = row;
        gc.weightx = 0.35;
        form.add(SwingUtils.fieldLabel(label), gc);
        gc.gridx = 1;
        gc.weightx = 0.65;
        form.add(field, gc);
    }
}
