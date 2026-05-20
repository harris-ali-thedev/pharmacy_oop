package com.pharmacy.ui;

import com.pharmacy.dao.SaleDAO;
import com.pharmacy.model.Sale;
import com.pharmacy.model.SaleItem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Arc2D;
import java.awt.geom.Rectangle2D;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;

/**
 * Sales analytics panel with KPI cards, daily revenue bar chart,
 * payment-method donut chart, and data tables.
 */
public class SalesReportPanel extends JPanel {

    private static final int PAD = 20;
    private static final DateTimeFormatter DAY_FMT = DateTimeFormatter.ofPattern("dd MMM");
    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    private final SaleDAO saleDAO = SaleDAO.getInstance();

    private JLabel revLabel;
    private JLabel txnLabel;
    private JLabel avgLabel;
    private JLabel rangeLabel;

    private JSpinner fromSpinner;
    private JSpinner toSpinner;

    private BarChartPanel revenueChart;
    private DonutChartPanel paymentChart;
    private HorizontalBarChartPanel topMedChart;

    private JTable recentTable;
    private DefaultTableModel recentModel;

    public SalesReportPanel() {
        setLayout(new BorderLayout());
        setBackground(SwingUtils.BG);
        buildUI();
        refresh();
    }

    // ── Layout ───────────────────────────────────────────────

    private void buildUI() {
        JButton refreshBtn = SwingUtils.outlineButton("Refresh");
        refreshBtn.addActionListener(e -> refresh());
        refreshBtn.setBackground(Color.BLUE);

        add(buildGradientHeader(refreshBtn), BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(SwingUtils.BG);
        content.setBorder(new EmptyBorder(16, PAD, PAD, PAD));

        content.add(buildFilterCard());
        content.add(Box.createVerticalStrut(14));
        content.add(buildKpiRow());
        content.add(Box.createVerticalStrut(14));
        content.add(buildChartsRow());
        content.add(Box.createVerticalStrut(14));
        content.add(buildTablesRow());

        JScrollPane scroll = SwingUtils.cleanScroll(content);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        add(scroll, BorderLayout.CENTER);
    }

    private JComponent buildGradientHeader(JButton refreshBtn) {
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
        header.setBorder(new EmptyBorder(20, PAD, 18, PAD));
        header.setPreferredSize(new Dimension(0, 72));
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));

        JLabel title = new JLabel("Sales Reports & Analytics");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Color.WHITE);

        rangeLabel = new JLabel("Showing data for selected period");
        rangeLabel.setFont(SwingUtils.FONT_BODY);
        rangeLabel.setForeground(new Color(255, 255, 255, 210));

        JPanel textCol = new JPanel();
        textCol.setOpaque(false);
        textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS));
        textCol.add(title);
        textCol.add(Box.createVerticalStrut(4));
        textCol.add(rangeLabel);

        refreshBtn.setBackground(new Color(255, 255, 255, 30));
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 90), 1, true),
                new EmptyBorder(8, 16, 8, 16)));

        header.add(textCol, BorderLayout.WEST);
        header.add(refreshBtn, BorderLayout.EAST);
        return header;
    }

    private JPanel buildFilterCard() {
        JPanel card = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        card.setBackground(SwingUtils.SOFT_BLUE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(190, 220, 232), 1, true),
                new EmptyBorder(12, 16, 12, 16)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));

        SpinnerDateModel fromModel = new SpinnerDateModel();
        SpinnerDateModel toModel = new SpinnerDateModel();
        fromSpinner = new JSpinner(fromModel);
        toSpinner = new JSpinner(toModel);
        fromSpinner.setEditor(new JSpinner.DateEditor(fromSpinner, "dd-MM-yyyy"));
        toSpinner.setEditor(new JSpinner.DateEditor(toSpinner, "dd-MM-yyyy"));

        Calendar cal = Calendar.getInstance();
        toModel.setValue(cal.getTime());
        cal.add(Calendar.DAY_OF_MONTH, -30);
        fromModel.setValue(cal.getTime());

        JButton runBtn = SwingUtils.primaryButton("Run Report");
        runBtn.addActionListener(e -> refresh());

        card.add(fieldLabel("From"));
        card.add(fromSpinner);
        card.add(fieldLabel("To"));
        card.add(toSpinner);
        card.add(runBtn);
        return card;
    }

    private JPanel buildKpiRow() {
        revLabel = kpiValue("Rs 0", SwingUtils.PRIMARY);
        txnLabel = kpiValue("0", SwingUtils.SUCCESS);
        avgLabel = kpiValue("Rs 0", SwingUtils.WARNING);

        JPanel row = new JPanel(new GridLayout(1, 3, 12, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        row.add(kpiCard(revLabel, "Total Revenue", "Rs", SwingUtils.SOFT_GREEN));
        row.add(kpiCard(txnLabel, "Transactions", "#", SwingUtils.SOFT_BLUE));
        row.add(kpiCard(avgLabel, "Avg. Sale Value", "~", SwingUtils.SOFT_ORANGE));
        return row;
    }

    private JLabel kpiValue(String text, Color color) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(new Font("Segoe UI", Font.BOLD, 26));
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
        markerLbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        markerLbl.setOpaque(true);
        markerLbl.setBackground(Color.WHITE);
        markerLbl.setForeground(SwingUtils.PRIMARY);
        markerLbl.setBorder(new EmptyBorder(6, 10, 6, 10));

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

    private JPanel buildChartsRow() {
        JPanel row = new JPanel(new GridLayout(1, 2, 14, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
        row.setPreferredSize(new Dimension(0, 300));

        revenueChart = new BarChartPanel("Daily Revenue (Rs)");
        paymentChart = new DonutChartPanel("Payment Methods");

        row.add(chartCard("Revenue Trend", revenueChart));
        row.add(chartCard("Payment Split", paymentChart));
        return row;
    }

    private JPanel buildTablesRow() {
        JPanel row = new JPanel(new GridLayout(1, 2, 14, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 340));
        row.setPreferredSize(new Dimension(0, 340));

        topMedChart = new HorizontalBarChartPanel("Top Medicines by Units");
        row.add(buildRecentSalesCard());
        row.add(chartCard("Top Sellers", topMedChart));
        return row;
    }

    private JPanel chartCard(String title, JComponent chart) {
        JPanel card = SwingUtils.card();
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SwingUtils.BORDER, 1, true),
                new EmptyBorder(14, 16, 14, 16)));
        card.add(sectionLabel(title), BorderLayout.NORTH);
        chart.setPreferredSize(new Dimension(0, 220));
        card.add(chart, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildRecentSalesCard() {
        JPanel card = SwingUtils.card();
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SwingUtils.BORDER, 1, true),
                new EmptyBorder(14, 16, 14, 16)));
        card.add(sectionLabel("Recent Sales"), BorderLayout.NORTH);

        String[] cols = {"#", "Date", "Cashier", "Patient", "Total", "Payment", "Status"};
        recentModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        recentTable = new JTable(recentModel);
        SwingUtils.styleTable(recentTable);
        recentTable.setRowHeight(34);
        recentTable.getColumnModel().getColumn(0).setMaxWidth(48);
        recentTable.setDefaultRenderer(Object.class, new StripedRenderer());
        recentTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) viewSaleDetails();
            }
        });

        JScrollPane sp = new JScrollPane(recentTable);
        sp.setBorder(BorderFactory.createLineBorder(SwingUtils.BORDER, 1, true));
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        card.add(sp, BorderLayout.CENTER);

        JLabel hint = new JLabel("Double-click a row for line-item details");
        hint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        hint.setForeground(SwingUtils.TEXT_GRAY);
        card.add(hint, BorderLayout.SOUTH);
        return card;
    }

    // ── Data refresh ─────────────────────────────────────────

    private void refresh() {
        LocalDate from = spinnerDate(fromSpinner);
        LocalDate to = spinnerDate(toSpinner);
        if (from.isAfter(to)) {
            LocalDate tmp = from;
            from = to;
            to = tmp;
        }

        rangeLabel.setText(String.format("Period: %s  →  %s",
                from.format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                to.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))));

        List<Sale> sales = saleDAO.findByDateRange(from, to).stream()
                .filter(s -> s.getStatus() == Sale.Status.COMPLETED)
                .toList();

        double revenue = sales.stream().mapToDouble(Sale::getGrandTotal).sum();
        int count = sales.size();
        double avg = count > 0 ? revenue / count : 0;

        revLabel.setText(revenue >= 1_000_000
                ? String.format("Rs %.1fM", revenue / 1_000_000)
                : String.format("Rs %,.0f", revenue));
        txnLabel.setText(String.valueOf(count));
        avgLabel.setText(String.format("Rs %,.0f", avg));

        revenueChart.setData(buildDailyRevenue(sales, from, to));
        paymentChart.setData(buildPaymentSplit(sales));

        Map<String, Integer> top = saleDAO.topMedicines(from, to, 8);
        topMedChart.setData(toDoubleMap(top));

        recentModel.setRowCount(0);
        LocalDate finalFrom = from;
        LocalDate finalTo = to;
        saleDAO.findRecent(50).stream()
                .filter(s -> {
                    LocalDate d = s.getSaleDate().toLocalDate();
                    return !d.isBefore(finalFrom) && !d.isAfter(finalTo);
                })
                .forEach(s -> recentModel.addRow(new Object[]{
                        s.getId(),
                        s.getSaleDate().format(DT_FMT),
                        s.getCashierName(),
                        s.getPatientName() != null ? s.getPatientName() : "Walk-in",
                        String.format("Rs %,.2f", s.getGrandTotal()),
                        formatPayment(s.getPaymentMethod()),
                        s.getStatus()
                }));
    }

    private LocalDate spinnerDate(JSpinner spinner) {
        Date d = (Date) spinner.getValue();
        return d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private Map<String, Double> buildDailyRevenue(List<Sale> sales, LocalDate from, LocalDate to) {
        long days = ChronoUnit.DAYS.between(from, to) + 1;
        int maxBars = (int) Math.min(days, 14);
        LocalDate start = days > maxBars ? to.minusDays(maxBars - 1) : from;

        Map<String, Double> daily = new LinkedHashMap<>();
        for (LocalDate d = start; !d.isAfter(to); d = d.plusDays(1)) {
            daily.put(d.format(DAY_FMT), 0.0);
        }
        for (Sale s : sales) {
            String key = s.getSaleDate().toLocalDate().format(DAY_FMT);
            if (daily.containsKey(key)) {
                daily.merge(key, s.getGrandTotal(), Double::sum);
            }
        }
        return daily;
    }

    private Map<String, Double> buildPaymentSplit(List<Sale> sales) {
        Map<String, Double> split = new LinkedHashMap<>();
        split.put("Cash", 0.0);
        split.put("Card", 0.0);
        split.put("Wallet", 0.0);
        for (Sale s : sales) {
            String key = switch (s.getPaymentMethod()) {
                case CARD -> "Card";
                case DIGITAL_WALLET -> "Wallet";
                default -> "Cash";
            };
            split.merge(key, s.getGrandTotal(), Double::sum);
        }
        split.entrySet().removeIf(e -> e.getValue() <= 0);
        return split;
    }

    private Map<String, Double> toDoubleMap(Map<String, Integer> intMap) {
        Map<String, Double> out = new LinkedHashMap<>();
        intMap.forEach((k, v) -> out.put(truncate(k, 22), v.doubleValue()));
        return out;
    }

    private String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }

    private String formatPayment(Sale.PaymentMethod pm) {
        if (pm == null) return "Cash";
        return switch (pm) {
            case CARD -> "Card";
            case DIGITAL_WALLET -> "Wallet";
            default -> "Cash";
        };
    }

    // ── Sale details dialog ────────────────────────────────────

    private void viewSaleDetails() {
        int row = recentTable.getSelectedRow();
        if (row < 0) return;
        int saleId = (int) recentModel.getValueAt(row, 0);

        saleDAO.findById(saleId).ifPresent(sale -> {
            JDialog dlg = new JDialog(
                    SwingUtilities.getWindowAncestor(this),
                    "Sale #" + sale.getId(),
                    Dialog.ModalityType.APPLICATION_MODAL);
            dlg.setLayout(new BorderLayout());
            dlg.getContentPane().setBackground(Color.WHITE);

            JPanel titleBar = new JPanel(new BorderLayout());
            titleBar.setBackground(SwingUtils.PRIMARY);
            titleBar.setBorder(new EmptyBorder(12, 16, 12, 16));
            JLabel titleLbl = new JLabel("Sale #" + sale.getId() + " — Details");
            titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
            titleLbl.setForeground(Color.WHITE);
            titleBar.add(titleLbl, BorderLayout.WEST);
            dlg.add(titleBar, BorderLayout.NORTH);

            StringBuilder sb = new StringBuilder();
            sb.append("Date: ").append(sale.getSaleDate().toLocalDate()).append("\n");
            sb.append("Cashier: ").append(sale.getCashierName()).append("\n");
            sb.append("Payment: ").append(formatPayment(sale.getPaymentMethod())).append("\n");
            sb.append("─────────────────────────\n");
            for (SaleItem item : sale.getItems()) {
                sb.append(String.format("%-22s x%d = Rs %.2f%n",
                        item.getMedicineName(), item.getQuantity(), item.getSubtotal()));
            }
            sb.append("─────────────────────────\n");
            sb.append(String.format("Subtotal:    Rs %.2f%n", sale.getSubtotal()));
            sb.append(String.format("Discount:  - Rs %.2f%n", sale.getDiscountAmount()));
            sb.append(String.format("Tax:         Rs %.2f%n", sale.getTaxAmount()));
            sb.append(String.format("GRAND TOTAL: Rs %.2f%n", sale.getGrandTotal()));

            JTextArea ta = new JTextArea(sb.toString());
            ta.setFont(SwingUtils.FONT_MONO);
            ta.setEditable(false);
            ta.setBackground(new Color(248, 250, 252));
            ta.setBorder(new EmptyBorder(12, 14, 12, 14));
            dlg.add(new JScrollPane(ta), BorderLayout.CENTER);

            JButton close = SwingUtils.primaryButton("Close");
            close.addActionListener(e -> dlg.dispose());
            JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            south.setOpaque(false);
            south.setBorder(new EmptyBorder(8, 12, 12, 12));
            south.add(close);
            dlg.add(south, BorderLayout.SOUTH);

            dlg.setSize(420, 400);
            dlg.setLocationRelativeTo(this);
            dlg.setVisible(true);
        });
    }

    // ── Custom charts (pure Swing Graphics2D) ─────────────────

    /** Vertical bar chart for daily revenue. */
    private static class BarChartPanel extends JPanel {
        private final String title;
        private Map<String, Double> data = Map.of();

        BarChartPanel(String title) {
            this.title = title;
            setOpaque(false);
            setPreferredSize(new Dimension(400, 220));
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
            int padL = 48, padR = 12, padT = 28, padB = 36;
            int chartW = w - padL - padR;
            int chartH = h - padT - padB;

            g2.setColor(SwingUtils.TEXT_GRAY);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
            g2.drawString(title, padL, 18);

            if (data.isEmpty()) {
                drawEmpty(g2, w, h);
                g2.dispose();
                return;
            }

            double max = data.values().stream().mapToDouble(v -> v).max().orElse(1);
            if (max <= 0) max = 1;

            int n = data.size();
            double barW = (double) chartW / n * 0.65;
            double gap = (double) chartW / n * 0.35;
            int i = 0;

            // grid line
            g2.setColor(new Color(230, 236, 242));
            g2.drawLine(padL, padT + chartH, padL + chartW, padT + chartH);

            for (Map.Entry<String, Double> e : data.entrySet()) {
                double val = e.getValue();
                int barH = (int) (val / max * chartH);
                int x = padL + (int) (i * (barW + gap) + gap / 2);
                int y = padT + chartH - barH;

                GradientPaint gp = new GradientPaint(
                        x, y, SwingUtils.PRIMARY_2,
                        x, y + barH, SwingUtils.PRIMARY);
                g2.setPaint(gp);
                g2.fill(new Rectangle2D.Double(x, y, barW, barH));

                g2.setColor(SwingUtils.TEXT_DARK);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
                FontMetrics fm = g2.getFontMetrics();
                String lbl = e.getKey();
                int lw = fm.stringWidth(lbl);
                g2.drawString(lbl, x + (int) (barW - lw) / 2, padT + chartH + 14);

                if (val > 0) {
                    String valStr = val >= 1000 ? String.format("%.0fk", val / 1000) : String.format("%.0f", val);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 9));
                    g2.setColor(SwingUtils.PRIMARY);
                    int vw = g2.getFontMetrics().stringWidth(valStr);
                    g2.drawString(valStr, x + (int) (barW - vw) / 2, y - 4);
                }
                i++;
            }
            g2.dispose();
        }
    }

    /** Donut chart for payment method distribution. */
    private static class DonutChartPanel extends JPanel {
        private static final Color[] COLORS = {
                SwingUtils.PRIMARY, SwingUtils.SUCCESS, SwingUtils.ACCENT, SwingUtils.WARNING
        };

        private final String title;
        private Map<String, Double> data = Map.of();

        DonutChartPanel(String title) {
            this.title = title;
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

            g2.setColor(SwingUtils.TEXT_GRAY);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
            g2.drawString(title, 12, 18);

            if (data.isEmpty()) {
                drawEmpty(g2, w, h);
                g2.dispose();
                return;
            }

            double total = data.values().stream().mapToDouble(v -> v).sum();
            int size = Math.min(w / 2, h - 50);
            int cx = w / 2 - 40;
            int cy = h / 2 + 8;
            int x = cx - size / 2;
            int y = cy - size / 2;

            double start = 90;
            int ci = 0;
            for (Map.Entry<String, Double> e : data.entrySet()) {
                double angle = (e.getValue() / total) * 360;
                g2.setColor(COLORS[ci % COLORS.length]);
                g2.fill(new Arc2D.Double(x, y, size, size, start, -angle, Arc2D.PIE));
                start -= angle;
                ci++;
            }

            // donut hole
            int hole = (int) (size * 0.55);
            g2.setColor(Color.WHITE);
            g2.fillOval(cx - hole / 2, cy - hole / 2, hole, hole);

            // center total
            g2.setColor(SwingUtils.TEXT_DARK);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
            String center = total >= 1000 ? String.format("Rs %.0fk", total / 1000) : String.format("Rs %.0f", total);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(center, cx - fm.stringWidth(center) / 2, cy + 4);

            // legend
            int ly = 36;
            int lx = w - 110;
            ci = 0;
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            for (Map.Entry<String, Double> e : data.entrySet()) {
                g2.setColor(COLORS[ci % COLORS.length]);
                g2.fillRect(lx, ly, 12, 12);
                g2.setColor(SwingUtils.TEXT_DARK);
                double pct = total > 0 ? e.getValue() / total * 100 : 0;
                g2.drawString(String.format("%s %.0f%%", e.getKey(), pct), lx + 18, ly + 11);
                ly += 20;
                ci++;
            }
            g2.dispose();
        }
    }

    /** Horizontal bar chart for top medicines. */
    private static class HorizontalBarChartPanel extends JPanel {
        private final String title;
        private Map<String, Double> data = Map.of();

        HorizontalBarChartPanel(String title) {
            this.title = title;
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
            int pad = 14;

            g2.setColor(SwingUtils.TEXT_GRAY);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
            g2.drawString(title, pad, 18);

            if (data.isEmpty()) {
                drawEmpty(g2, w, h);
                g2.dispose();
                return;
            }

            double max = data.values().stream().mapToDouble(v -> v).max().orElse(1);
            int barH = 22;
            int gap = 8;
            int y = 32;
            int labelW = 120;
            int barMaxW = w - labelW - pad * 3;

            Color[] colors = {
                    SwingUtils.PRIMARY, SwingUtils.PRIMARY_2, SwingUtils.SUCCESS,
                    new Color(56, 178, 172), SwingUtils.ACCENT, SwingUtils.WARNING,
                    new Color(99, 102, 241), new Color(168, 85, 247)
            };
            int ci = 0;
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            for (Map.Entry<String, Double> e : data.entrySet()) {
                if (y + barH > h - pad) break;

                g2.setColor(SwingUtils.TEXT_DARK);
                g2.drawString(e.getKey(), pad, y + 15);

                int bw = (int) (e.getValue() / max * barMaxW);
                g2.setColor(colors[ci % colors.length]);
                g2.fillRoundRect(pad + labelW, y, Math.max(bw, 4), barH, 8, 8);

                g2.setColor(SwingUtils.TEXT_GRAY);
                g2.drawString(String.valueOf(e.getValue().intValue()),
                        pad + labelW + bw + 6, y + 15);

                y += barH + gap;
                ci++;
            }
            g2.dispose();
        }
    }

    private static void drawEmpty(Graphics2D g2, int w, int h) {
        g2.setColor(SwingUtils.TEXT_GRAY);
        g2.setFont(SwingUtils.FONT_BODY);
        String msg = "No data for this period";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(msg, (w - fm.stringWidth(msg)) / 2, h / 2);
    }

    // ── Helpers ──────────────────────────────────────────────

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

    private JLabel sectionLabel(String text) {
        return SwingUtils.headingLabel(text);
    }

    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(SwingUtils.TEXT_GRAY);
        return l;
    }
}
