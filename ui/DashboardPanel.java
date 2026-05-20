package com.pharmacy.ui;

import com.pharmacy.dao.MedicineDAO;
import com.pharmacy.dao.SaleDAO;
import com.pharmacy.model.Medicine;
import com.pharmacy.model.Sale;
import com.pharmacy.util.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Pharmacy dashboard with gradient header, KPI cards, 7-day revenue chart,
 * recent sales table, and color-coded low-stock alerts.
 */
public class DashboardPanel extends JPanel {

    private static final int PAD = 20;
    private static final DateTimeFormatter DAY_FMT = DateTimeFormatter.ofPattern("EEE dd");
    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
    private static final DateTimeFormatter TODAY_FMT = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy");

    private final SaleDAO saleDAO = SaleDAO.getInstance();
    private final MedicineDAO medDAO = MedicineDAO.getInstance();

    private JLabel revLabel;
    private JLabel txnLabel;
    private JLabel lowLabel;
    private JLabel medsLabel;
    private JLabel dateLabel;
    private WeekChartPanel weekChart;

    private JTable recentTable;
    private DefaultTableModel recentModel;
    private JTable alertTable;
    private DefaultTableModel alertModel;

    public DashboardPanel() {
        setLayout(new BorderLayout());
        setBackground(SwingUtils.BG);
        buildUI();
        refresh();
    }

    // ── Layout ───────────────────────────────────────────────

    private void buildUI() {
        JButton refreshBtn = SwingUtils.outlineButton("Refresh");
        refreshBtn.addActionListener(e -> refresh());

        add(buildGradientHeader(refreshBtn), BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(SwingUtils.BG);
        content.setBorder(new EmptyBorder(16, PAD, PAD, PAD));

        content.add(buildKpiRow());
        content.add(Box.createVerticalStrut(14));
        content.add(buildChartCard());
        content.add(Box.createVerticalStrut(14));
        content.add(buildTablesRow());

        JScrollPane scroll = SwingUtils.cleanScroll(content);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        add(scroll, BorderLayout.CENTER);
    }

    private JComponent buildGradientHeader(JButton refreshBtn) {
        String userName = SessionManager.getInstance().getCurrentUser().getFullName();

        JPanel header = new JPanel(new BorderLayout(16, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(
                        0, 0, SwingUtils.PRIMARY_2,
                        getWidth(), getHeight(), SwingUtils.PRIMARY);
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

        JLabel title = new JLabel("Dashboard");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Color.WHITE);

        JLabel welcome = new JLabel("Welcome back, " + userName);
        welcome.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        welcome.setForeground(new Color(255, 255, 255, 220));

        dateLabel = new JLabel(LocalDate.now().format(TODAY_FMT));
        dateLabel.setFont(SwingUtils.FONT_SMALL);
        dateLabel.setForeground(new Color(255, 255, 255, 190));

        JPanel textCol = new JPanel();
        textCol.setOpaque(false);
        textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS));
        textCol.add(title);
        textCol.add(Box.createVerticalStrut(2));
        textCol.add(welcome);
        textCol.add(Box.createVerticalStrut(1));
        textCol.add(dateLabel);

        refreshBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        refreshBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 90), 1, true),
                new EmptyBorder(5, 12, 5, 12)));

        header.add(textCol, BorderLayout.WEST);
        header.add(refreshBtn, BorderLayout.EAST);
        return header;
    }

    private JPanel buildKpiRow() {
        revLabel = kpiValue("Rs 0", SwingUtils.PRIMARY);
        txnLabel = kpiValue("0", SwingUtils.SUCCESS);
        lowLabel = kpiValue("0", SwingUtils.DANGER);
        medsLabel = kpiValue("0", SwingUtils.PRIMARY_2);

        JPanel row = new JPanel(new GridLayout(1, 4, 12, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 108));
        row.add(kpiCard(revLabel, "Today's Revenue", "Rs", SwingUtils.SOFT_GREEN));
        row.add(kpiCard(txnLabel, "Today's Sales", "#", SwingUtils.SOFT_BLUE));
        row.add(kpiCard(lowLabel, "Low Stock", "!", SwingUtils.SOFT_ORANGE));
        row.add(kpiCard(medsLabel, "Active Medicines", "+", new Color(237, 233, 254)));
        return row;
    }

    private JLabel kpiValue(String text, Color color) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 24));
        label.setForeground(color);
        return label;
    }

    private JPanel kpiCard(JLabel valueLabel, String labelText, String marker, Color tint) {
        JPanel card = SwingUtils.card();
        card.setBackground(tint);
        card.setLayout(new BorderLayout(8, 4));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SwingUtils.BORDER, 1, true),
                new EmptyBorder(12, 14, 12, 14)));

        JLabel markerLabel = new JLabel(marker, SwingConstants.CENTER);
        markerLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        markerLabel.setOpaque(true);
        markerLabel.setBackground(Color.WHITE);
        markerLabel.setForeground(SwingUtils.PRIMARY);
        markerLabel.setBorder(new EmptyBorder(5, 9, 5, 9));

        JLabel nameLabel = new JLabel(labelText, SwingConstants.CENTER);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        nameLabel.setForeground(SwingUtils.TEXT_GRAY);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        top.setOpaque(false);
        top.add(markerLabel);

        card.add(top, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(nameLabel, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildChartCard() {
        JPanel card = SwingUtils.card();
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SwingUtils.BORDER, 1, true),
                new EmptyBorder(14, 18, 14, 18)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 240));
        card.setPreferredSize(new Dimension(0, 240));

        JPanel north = new JPanel(new BorderLayout());
        north.setOpaque(false);
        north.add(SwingUtils.headingLabel("Revenue — Last 7 Days"), BorderLayout.WEST);
        JLabel sub = new JLabel("Daily completed sales");
        sub.setFont(SwingUtils.FONT_SMALL);
        sub.setForeground(SwingUtils.TEXT_GRAY);
        north.add(sub, BorderLayout.EAST);
        card.add(north, BorderLayout.NORTH);

        weekChart = new WeekChartPanel();
        weekChart.setPreferredSize(new Dimension(0, 170));
        card.add(weekChart, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildTablesRow() {
        JPanel row = new JPanel(new GridLayout(1, 2, 14, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 320));
        row.setPreferredSize(new Dimension(0, 320));
        row.add(buildRecentSalesCard());
        row.add(buildAlertsCard());
        return row;
    }

    private JPanel buildRecentSalesCard() {
        JPanel card = styledTableCard("Recent Sales", "Latest completed transactions");

        String[] cols = {"#", "Date", "Cashier", "Total", "Payment", "Status"};
        recentModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        recentTable = new JTable(recentModel);
        styleTable(recentTable);
        recentTable.getColumnModel().getColumn(0).setMaxWidth(44);

        card.add(wrapTable(recentTable), BorderLayout.CENTER);
        return card;
    }

    private JPanel buildAlertsCard() {
        JPanel card = styledTableCard("Low-Stock Alerts", "Items at or below reorder level");

        String[] cols = {"Medicine", "Stock", "Reorder"};
        alertModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        alertTable = new JTable(alertModel);
        styleTable(alertTable);
        alertTable.getColumnModel().getColumn(1).setCellRenderer(new StockAlertRenderer());
        alertTable.getColumnModel().getColumn(2).setCellRenderer(new StripedRenderer());

        card.add(wrapTable(alertTable), BorderLayout.CENTER);
        return card;
    }

    private JPanel styledTableCard(String title, String subtitle) {
        JPanel card = SwingUtils.card();
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SwingUtils.BORDER, 1, true),
                new EmptyBorder(14, 16, 14, 16)));

        JPanel north = new JPanel(new BorderLayout());
        north.setOpaque(false);
        north.add(SwingUtils.headingLabel(title), BorderLayout.WEST);
        JLabel sub = new JLabel(subtitle);
        sub.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        sub.setForeground(SwingUtils.TEXT_GRAY);
        north.add(sub, BorderLayout.SOUTH);
        card.add(north, BorderLayout.NORTH);
        return card;
    }

    private JScrollPane wrapTable(JTable table) {
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(SwingUtils.BORDER, 1, true));
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        sp.getViewport().setBackground(Color.WHITE);
        return sp;
    }

    private void styleTable(JTable table) {
        SwingUtils.styleTable(table);
        table.setRowHeight(34);
        table.setDefaultRenderer(Object.class, new StripedRenderer());
    }

    // ── Data refresh ─────────────────────────────────────────

    public void refresh() {
        LocalDate today = LocalDate.now();
        dateLabel.setText(today.format(TODAY_FMT));

        double revenue = saleDAO.totalRevenue(today);
        revLabel.setText(revenue >= 1000
                ? String.format("Rs %,.0fk", revenue / 1000)
                : String.format("Rs %,.0f", revenue));
        txnLabel.setText(String.valueOf(saleDAO.transactionCount(today)));
        lowLabel.setText(String.valueOf(medDAO.findLowStock().size()));
        medsLabel.setText(String.valueOf(medDAO.findActive().size()));

        weekChart.setData(buildWeekRevenue(today));

        recentModel.setRowCount(0);
        for (Sale sale : saleDAO.findRecent(15)) {
            recentModel.addRow(new Object[]{
                    sale.getId(),
                    sale.getSaleDate().format(DT_FMT),
                    sale.getCashierName(),
                    String.format("Rs %,.2f", sale.getGrandTotal()),
                    formatPayment(sale.getPaymentMethod()),
                    sale.getStatus()
            });
        }

        alertModel.setRowCount(0);
        for (Medicine med : medDAO.findLowStock()) {
            alertModel.addRow(new Object[]{
                    med.getName(),
                    med.getStockQty(),
                    med.getReorderLevel()
            });
        }
    }

    private Map<String, Double> buildWeekRevenue(LocalDate today) {
        Map<String, Double> data = new LinkedHashMap<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            data.put(d.format(DAY_FMT), saleDAO.totalRevenue(d));
        }
        return data;
    }

    private String formatPayment(Sale.PaymentMethod pm) {
        if (pm == null) return "Cash";
        return switch (pm) {
            case CARD -> "Card";
            case DIGITAL_WALLET -> "Wallet";
            default -> "Cash";
        };
    }

    // ── 7-day bar chart ──────────────────────────────────────

    private static class WeekChartPanel extends JPanel {
        private Map<String, Double> data = Map.of();

        WeekChartPanel() {
            setOpaque(false);
        }

        void setData(Map<String, Double> data) {
            this.data = data != null ? data : Map.of();
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int padL = 44;
            int padR = 12;
            int padT = 8;
            int padB = 28;
            int chartW = w - padL - padR;
            int chartH = h - padT - padB;

            if (data.isEmpty()) {
                drawEmpty(g2, w, h);
                g2.dispose();
                return;
            }

            double max = data.values().stream().mapToDouble(v -> v).max().orElse(1);
            if (max <= 0) max = 1;

            g2.setColor(new Color(235, 240, 245));
            g2.drawLine(padL, padT + chartH, padL + chartW, padT + chartH);

            int n = data.size();
            double slot = (double) chartW / n;
            double barW = slot * 0.55;
            int i = 0;

            for (Map.Entry<String, Double> e : data.entrySet()) {
                double val = e.getValue();
                int barH = (int) (val / max * chartH);
                int x = padL + (int) (i * slot + (slot - barW) / 2);
                int y = padT + chartH - barH;

                boolean isToday = i == n - 1;
                Color top = isToday ? SwingUtils.SUCCESS : SwingUtils.PRIMARY_2;
                Color bot = isToday ? new Color(5, 150, 105) : SwingUtils.PRIMARY;
                g2.setPaint(new GradientPaint(x, y, top, x, y + barH, bot));
                g2.fill(new Rectangle2D.Double(x, y, barW, Math.max(barH, 2)));

                g2.setColor(SwingUtils.TEXT_GRAY);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                String lbl = e.getKey();
                int lw = g2.getFontMetrics().stringWidth(lbl);
                g2.drawString(lbl, x + (int) (barW - lw) / 2, padT + chartH + 16);

                if (val > 0) {
                    String vs = val >= 1000 ? String.format("%.0fk", val / 1000) : String.format("%.0f", val);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 9));
                    g2.setColor(isToday ? SwingUtils.SUCCESS.darker() : SwingUtils.PRIMARY);
                    int vw = g2.getFontMetrics().stringWidth(vs);
                    g2.drawString(vs, x + (int) (barW - vw) / 2, Math.max(y - 4, padT + 10));
                }
                i++;
            }
            g2.dispose();
        }

        private void drawEmpty(Graphics2D g2, int w, int h) {
            g2.setColor(SwingUtils.TEXT_GRAY);
            g2.setFont(SwingUtils.FONT_BODY);
            String msg = "No sales data yet";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(msg, (w - fm.stringWidth(msg)) / 2, h / 2);
        }
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
            setBorder(new EmptyBorder(0, 8, 0, 8));
            return c;
        }
    }

    private static class StockAlertRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean selected, boolean focused, int row, int col) {
            Component c = super.getTableCellRendererComponent(table, value, selected, focused, row, col);
            setHorizontalAlignment(SwingConstants.CENTER);
            setBorder(new EmptyBorder(0, 8, 0, 8));

            if (!selected && value instanceof Number n) {
                int stock = n.intValue();
                int reorder = 0;
                Object reorderVal = table.getModel().getValueAt(row, 2);
                if (reorderVal instanceof Number rn) reorder = rn.intValue();

                if (stock <= 0) {
                    setForeground(SwingUtils.DANGER);
                    setFont(new Font("Segoe UI", Font.BOLD, 13));
                } else if (stock <= reorder) {
                    setForeground(SwingUtils.WARNING);
                    setFont(new Font("Segoe UI", Font.BOLD, 13));
                } else {
                    setForeground(SwingUtils.TEXT_DARK);
                    setFont(SwingUtils.FONT_BODY);
                }
                c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(249, 251, 253));
            }
            return c;
        }
    }
}
