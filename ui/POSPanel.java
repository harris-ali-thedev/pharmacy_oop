package com.pharmacy.ui;

import com.pharmacy.dao.MedicineDAO;
import com.pharmacy.dao.PatientDAO;
import com.pharmacy.generics.Result;
import com.pharmacy.model.*;
import com.pharmacy.service.POSService;
import com.pharmacy.service.POSService.SaleSummary;
import com.pharmacy.util.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Point-of-Sale panel with gradient header, search suggestions dropdown,
 * styled cart table, and accent payment summary.
 */
public class POSPanel extends JPanel {

    private static final int PAD = 18;

    private final POSService posService = POSService.getInstance();
    private final MedicineDAO medDAO = MedicineDAO.getInstance();
    private final PatientDAO patDAO = PatientDAO.getInstance();

    private final List<CartItem> cart = new ArrayList<>();
    private Patient selectedPatient;

    private JTable cartTable;
    private DefaultTableModel cartModel;

    private JTextField searchField;
    private JLabel patientChip;
    private JLabel cartCountBadge;
    private JLabel errorLabel;

    private JTextField discountField;
    private JTextField taxField;
    private JTextField amountPaidField;
    private JComboBox<String> paymentBox;

    private JLabel subtotalLbl;
    private JLabel discountLbl;
    private JLabel taxLbl;
    private JLabel grandTotalLbl;
    private JLabel changeLbl;
    private JPanel grandTotalPanel;

    public POSPanel() {
        setLayout(new BorderLayout());
        setBackground(SwingUtils.BG);
        buildUI();
    }

    // ── UI construction ──────────────────────────────────────

    private void buildUI() {
        add(buildGradientHeader(), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                buildLeftPanel(),
                buildRightPanel());
        split.setDividerLocation(720);
        split.setResizeWeight(0.62);
        split.setDividerSize(6);
        split.setBorder(null);
        split.setBackground(SwingUtils.BG);
        add(split, BorderLayout.CENTER);
    }

    private JComponent buildGradientHeader() {
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
        header.setBorder(new EmptyBorder(20, PAD, 18, PAD));
        header.setPreferredSize(new Dimension(0, 72));
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));

        JLabel title = new JLabel("Point of Sale");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Color.WHITE);

        String cashier = SessionManager.getInstance().getCurrentUser().getFullName();
        JLabel subtitle = new JLabel("Cashier: " + cashier + "  •  Scan or search medicines to sell");
        subtitle.setFont(SwingUtils.FONT_BODY);
        subtitle.setForeground(new Color(255, 255, 255, 210));

        JPanel textCol = new JPanel();
        textCol.setOpaque(false);
        textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS));
        textCol.add(title);
        textCol.add(Box.createVerticalStrut(4));
        textCol.add(subtitle);

        cartCountBadge = SwingUtils.badge("0 items", SwingUtils.ACCENT);
        cartCountBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        cartCountBadge.setBorder(new EmptyBorder(8, 14, 8, 14));

        header.add(textCol, BorderLayout.WEST);
        header.add(cartCountBadge, BorderLayout.EAST);
        return header;
    }

    // ── Left: search + cart ──────────────────────────────────

    private JPanel buildLeftPanel() {
        JPanel left = new JPanel(new BorderLayout(0, 14));
        left.setBackground(SwingUtils.BG);
        left.setBorder(new EmptyBorder(16, PAD, 16, 8));

        left.add(buildSearchCard(), BorderLayout.NORTH);
        left.add(buildCartCard(), BorderLayout.CENTER);
        left.add(buildErrorBar(), BorderLayout.SOUTH);
        return left;
    }

    private JPanel buildSearchCard() {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(SwingUtils.SOFT_BLUE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(190, 220, 232), 1, true),
                new EmptyBorder(14, 16, 14, 16)));

        card.add(sectionLabel("Add Medicine"), BorderLayout.NORTH);

        searchField = SwingUtils.styledField("Type name, category, or barcode...");
        searchField.setPreferredSize(new Dimension(0, 40));

        // Live suggestion dropdown while typing
        new MedicineSuggestionPopup(searchField, medDAO::search, med -> addMedicineToCart(med, 1));

        JButton addBtn = SwingUtils.primaryButton("Add to Cart");
        JButton patientBtn = SwingUtils.outlineButton("Select Patient");
        addBtn.addActionListener(e -> addToCart());
        searchField.addActionListener(e -> addToCart());
        patientBtn.addActionListener(e -> selectPatient());

        JLabel hint = new JLabel("Suggestions appear as you type  •  ↑↓ navigate  •  Enter to add");
        hint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        hint.setForeground(SwingUtils.TEXT_GRAY);

        JPanel searchRow = new JPanel(new BorderLayout(10, 0));
        searchRow.setOpaque(false);
        searchRow.add(wrapLabeled("Search medicine", searchField), BorderLayout.CENTER);
        JPanel btnCol = new JPanel();
        btnCol.setOpaque(false);
        btnCol.setLayout(new BoxLayout(btnCol, BoxLayout.Y_AXIS));
        btnCol.add(addBtn);
        btnCol.add(Box.createVerticalStrut(6));
        btnCol.add(patientBtn);
        searchRow.add(btnCol, BorderLayout.EAST);

        patientChip = new JLabel("  Walk-in customer  ");
        patientChip.setFont(new Font("Segoe UI", Font.BOLD, 12));
        patientChip.setOpaque(true);
        patientChip.setBackground(Color.WHITE);
        patientChip.setForeground(SwingUtils.PRIMARY);
        patientChip.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SwingUtils.BORDER, 1, true),
                new EmptyBorder(6, 12, 6, 12)));

        JPanel south = new JPanel(new BorderLayout(0, 6));
        south.setOpaque(false);
        south.add(hint, BorderLayout.NORTH);
        south.add(patientChip, BorderLayout.SOUTH);

        card.add(searchRow, BorderLayout.CENTER);
        card.add(south, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildCartCard() {
        JPanel card = SwingUtils.card();
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SwingUtils.BORDER, 1, true),
                new EmptyBorder(16, 18, 16, 18)));

        JPanel north = new JPanel(new BorderLayout());
        north.setOpaque(false);
        north.add(sectionLabel("Shopping Cart"), BorderLayout.WEST);

        JButton removeBtn = SwingUtils.dangerButton("Remove");
        JButton clearBtn = SwingUtils.outlineButton("Clear All");
        removeBtn.addActionListener(e -> removeSelected());
        clearBtn.addActionListener(e -> clearCart());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.add(removeBtn);
        actions.add(clearBtn);
        north.add(actions, BorderLayout.EAST);
        card.add(north, BorderLayout.NORTH);

        String[] cols = {"ID", "Medicine", "Unit Price", "Qty", "Subtotal"};
        cartModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return c == 3;
            }
        };
        cartTable = new JTable(cartModel);
        SwingUtils.styleTable(cartTable);
        cartTable.setRowHeight(40);
        cartTable.getColumnModel().getColumn(0).setMaxWidth(50);
        cartTable.getColumnModel().getColumn(2).setMaxWidth(110);
        cartTable.getColumnModel().getColumn(3).setMaxWidth(70);
        cartTable.setDefaultRenderer(Object.class, new StripedRenderer());
        cartTable.getColumnModel().getColumn(4).setCellRenderer(new MoneyRenderer());

        cartModel.addTableModelListener(e -> {
            if (e.getColumn() == 3) {
                int row = e.getFirstRow();
                try {
                    int newQty = Integer.parseInt(cartModel.getValueAt(row, 3).toString());
                    int medId = (int) cartModel.getValueAt(row, 0);
                    cart.stream()
                            .filter(ci -> ci.getMedId() == medId)
                            .findFirst()
                            .ifPresent(ci -> medDAO.findById(medId).ifPresent(med -> {
                                if (newQty > 0 && newQty <= med.getStockQty()) {
                                    ci.setQuantity(newQty);
                                    cartModel.setValueAt(
                                            String.format("Rs %,.2f", ci.getSubtotal()), row, 4);
                                    recalculate();
                                }
                            }));
                } catch (NumberFormatException ignored) { /* invalid qty */ }
            }
        });

        JScrollPane scroll = verticalScroll(cartTable);
        scroll.setBorder(BorderFactory.createLineBorder(SwingUtils.BORDER, 1, true));
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setPreferredSize(new Dimension(0, 340));
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildErrorBar() {
        errorLabel = new JLabel(" ");
        errorLabel.setFont(SwingUtils.FONT_SMALL);
        errorLabel.setForeground(SwingUtils.DANGER);
        errorLabel.setBorder(new EmptyBorder(4, 4, 0, 4));

        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.add(errorLabel, BorderLayout.CENTER);
        return bar;
    }

    // ── Right: totals + payment ────────────────────────────────

    private JComponent buildRightPanel() {
        JPanel checkout = new JPanel();
        checkout.setLayout(new BoxLayout(checkout, BoxLayout.Y_AXIS));
        checkout.setBackground(SwingUtils.BG);
        checkout.setBorder(new EmptyBorder(16, 10, 16, PAD));

        checkout.add(buildTotalsCard());
        checkout.add(Box.createVerticalStrut(12));
        checkout.add(buildPaymentCard());
        checkout.add(Box.createVerticalStrut(14));
        checkout.add(buildCheckoutButton());

        JScrollPane scroll = verticalScroll(checkout);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(SwingUtils.BG);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        return scroll;
    }

    private JPanel buildTotalsCard() {
        JPanel card = SwingUtils.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SwingUtils.BORDER, 1, true),
                new EmptyBorder(16, 18, 16, 18)));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 320));

        card.add(sectionLabel("Order Summary"));
        card.add(Box.createVerticalStrut(10));

        subtotalLbl = amountLabel("Rs 0.00");
        discountLbl = amountLabel("Rs 0.00");
        taxLbl = amountLabel("Rs 0.00");

        card.add(summaryRow("Subtotal", subtotalLbl));
        card.add(Box.createVerticalStrut(8));

        discountField = SwingUtils.styledField("0");
        discountField.setPreferredSize(new Dimension(70, 34));
        discountField.getDocument().addDocumentListener(docListener(this::recalculate));
        card.add(inputSummaryRow("Discount (%)", discountField, discountLbl));

        taxField = SwingUtils.styledField("0");
        taxField.setPreferredSize(new Dimension(70, 34));
        taxField.getDocument().addDocumentListener(docListener(this::recalculate));
        card.add(Box.createVerticalStrut(8));
        card.add(inputSummaryRow("Tax (%)", taxField, taxLbl));

        card.add(Box.createVerticalStrut(12));
        card.add(new JSeparator());
        card.add(Box.createVerticalStrut(12));

        grandTotalLbl = new JLabel("Rs 0.00", SwingConstants.CENTER);
        grandTotalLbl.setFont(new Font("Segoe UI", Font.BOLD, 28));
        grandTotalLbl.setForeground(Color.WHITE);

        grandTotalPanel = new JPanel(new BorderLayout());
        grandTotalPanel.setBackground(SwingUtils.PRIMARY);
        grandTotalPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SwingUtils.PRIMARY.darker(), 1, true),
                new EmptyBorder(14, 16, 14, 16)));
        JLabel gtCaption = new JLabel("GRAND TOTAL", SwingConstants.CENTER);
        gtCaption.setFont(new Font("Segoe UI", Font.BOLD, 11));
        gtCaption.setForeground(new Color(255, 255, 255, 200));
        grandTotalPanel.add(gtCaption, BorderLayout.NORTH);
        grandTotalPanel.add(grandTotalLbl, BorderLayout.CENTER);
        card.add(grandTotalPanel);

        card.add(Box.createVerticalStrut(10));
        changeLbl = new JLabel("Change: Rs 0.00", SwingConstants.CENTER);
        changeLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        changeLbl.setForeground(SwingUtils.SUCCESS);
        card.add(changeLbl);

        return card;
    }

    private JPanel buildPaymentCard() {
        JPanel card = SwingUtils.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SwingUtils.BORDER, 1, true),
                new EmptyBorder(16, 18, 16, 18)));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));

        card.add(sectionLabel("Payment"));
        card.add(Box.createVerticalStrut(10));

        paymentBox = SwingUtils.styledCombo("CASH", "CARD", "DIGITAL_WALLET");
        paymentBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        card.add(wrapLabeled("Payment method", paymentBox));
        card.add(Box.createVerticalStrut(10));

        amountPaidField = SwingUtils.styledField("0.00");
        amountPaidField.getDocument().addDocumentListener(docListener(this::updateChange));
        card.add(wrapLabeled("Amount paid (Rs)", amountPaidField));
        card.add(Box.createVerticalStrut(8));

        JPanel quickPay = new JPanel(new GridLayout(1, 3, 8, 0));
        quickPay.setOpaque(false);
        quickPay.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        for (int amount : new int[]{500, 1000, 2000}) {
            JButton qb = SwingUtils.outlineButton("Rs " + amount);
            qb.addActionListener(e -> {
                amountPaidField.setText(String.valueOf(amount));
                updateChange();
            });
            quickPay.add(qb);
        }
        JButton exactBtn = SwingUtils.outlineButton("Exact");
        exactBtn.addActionListener(e -> {
            double grand = parseAmount(grandTotalLbl.getText());
            amountPaidField.setText(String.format("%.2f", grand));
            updateChange();
        });
        JPanel quickRow2 = new JPanel(new GridLayout(1, 1, 0, 0));
        quickRow2.setOpaque(false);
        quickRow2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        quickRow2.add(exactBtn);

        card.add(wrapLabeled("Quick amounts", quickPay));
        card.add(Box.createVerticalStrut(6));
        card.add(quickRow2);

        return card;
    }

    private JButton buildCheckoutButton() {
        JButton checkoutBtn = SwingUtils.successButton("Complete Sale");
        checkoutBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        checkoutBtn.setPreferredSize(new Dimension(0, 52));
        checkoutBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        checkoutBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        checkoutBtn.setBackground(new Color(5, 150, 105));
        checkoutBtn.addActionListener(e -> checkout());
        return checkoutBtn;
    }

    // ── Cart operations ──────────────────────────────────────

    private void addToCart() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) return;

        List<Medicine> results = medDAO.search(query);
        if (results.isEmpty()) {
            showError("No medicine found for: " + query);
            return;
        }

        Medicine med;
        if (results.size() == 1) {
            med = results.get(0);
        } else {
            String[] names = results.stream().map(Medicine::getDisplayName).toArray(String[]::new);
            String chosen = (String) JOptionPane.showInputDialog(
                    this, "Multiple matches — select one:", "Select Medicine",
                    JOptionPane.PLAIN_MESSAGE, null, names, names[0]);
            if (chosen == null) return;
            med = results.stream()
                    .filter(m -> m.getDisplayName().equals(chosen))
                    .findFirst()
                    .orElse(results.get(0));
        }
        addMedicineToCart(med, 1);
    }

    private void addMedicineToCart(Medicine med, int qty) {
        Result<List<CartItem>> r = posService.addToCart(cart, med.getId(), qty);
        if (r.isFailure()) {
            showError(r.getErrorMessage());
            return;
        }
        refreshCartTable();
        recalculate();
        searchField.setText("");
        searchField.requestFocusInWindow();
        hideError();
    }

    private void removeSelected() {
        int row = cartTable.getSelectedRow();
        if (row < 0) {
            showError("Select an item to remove.");
            return;
        }
        int medId = (int) cartModel.getValueAt(row, 0);
        posService.removeFromCart(cart, medId);
        refreshCartTable();
        recalculate();
        hideError();
    }

    private void clearCart() {
        if (!cart.isEmpty() && !SwingUtils.confirm(this, "Clear the entire cart?")) return;
        cart.clear();
        selectedPatient = null;
        updatePatientChip();
        refreshCartTable();
        recalculate();
        amountPaidField.setText("0.00");
        hideError();
    }

    private void refreshCartTable() {
        cartModel.setRowCount(0);
        for (CartItem ci : cart) {
            cartModel.addRow(new Object[]{
                    ci.getMedId(),
                    ci.getMedicineName(),
                    String.format("Rs %,.2f", ci.getUnitPrice()),
                    ci.getQuantity(),
                    String.format("Rs %,.2f", ci.getSubtotal())
            });
        }
        cartCountBadge.setText(cart.size() + (cart.size() == 1 ? " item" : " items"));
    }

    // ── Totals ───────────────────────────────────────────────

    private void recalculate() {
        double discPct = safeDouble(discountField.getText());
        double taxPct = safeDouble(taxField.getText());

        Result<Double> dr = posService.validateDiscount(discPct);
        if (dr.isFailure()) {
            showError(dr.getErrorMessage());
            discPct = 0;
        }

        SaleSummary s = posService.computeSummary(cart, discPct, taxPct);
        subtotalLbl.setText(String.format("Rs %,.2f", s.subtotal()));
        discountLbl.setText(String.format("- Rs %,.2f", s.discountAmount()));
        taxLbl.setText(String.format("+ Rs %,.2f", s.taxAmount()));
        grandTotalLbl.setText(String.format("Rs %,.2f", s.grandTotal()));
        updateChange();
    }

    private void updateChange() {
        double paid = safeDouble(amountPaidField.getText());
        double grand = parseAmount(grandTotalLbl.getText());
        double change = paid - grand;
        if (change >= 0) {
            changeLbl.setText(String.format("Change: Rs %,.2f", change));
            changeLbl.setForeground(SwingUtils.SUCCESS);
        } else {
            changeLbl.setText(String.format("Short: Rs %,.2f", Math.abs(change)));
            changeLbl.setForeground(SwingUtils.DANGER);
        }
    }

    // ── Patient selection ────────────────────────────────────

    private void selectPatient() {
        JDialog dlg = new JDialog(
                SwingUtilities.getWindowAncestor(this),
                "Select Patient",
                Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setLayout(new BorderLayout());
        dlg.getContentPane().setBackground(Color.WHITE);

        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setBackground(SwingUtils.PRIMARY);
        titleBar.setBorder(new EmptyBorder(12, 16, 12, 16));
        JLabel titleLbl = new JLabel("Find Patient");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titleLbl.setForeground(Color.WHITE);
        titleBar.add(titleLbl, BorderLayout.WEST);
        dlg.add(titleBar, BorderLayout.NORTH);

        JTextField searchPt = SwingUtils.styledField("Search by name or phone...");
        JList<String> resultList = new JList<>();
        resultList.setFont(SwingUtils.FONT_BODY);
        resultList.setSelectionBackground(new Color(210, 242, 238));
        DefaultListModel<String> listModel = new DefaultListModel<>();
        resultList.setModel(listModel);

        List<Patient>[] pHolder = new List[]{patDAO.findActive()};

        Runnable doSearch = () -> {
            String kw = searchPt.getText().trim();
            pHolder[0] = kw.isEmpty() ? patDAO.findActive() : patDAO.search(kw);
            listModel.clear();
            pHolder[0].forEach(p ->
                    listModel.addElement(p.getFullName() + "  |  " + p.getPhone()
                            + "  |  " + p.getLoyaltyPoints() + " pts"));
        };
        doSearch.run();
        searchPt.getDocument().addDocumentListener(docListener(doSearch));

        JPanel body = new JPanel(new BorderLayout(8, 8));
        body.setBorder(new EmptyBorder(14, 16, 14, 16));
        body.setOpaque(false);
        body.add(searchPt, BorderLayout.NORTH);
        JScrollPane sp = new JScrollPane(resultList);
        sp.setBorder(BorderFactory.createLineBorder(SwingUtils.BORDER, 1, true));
        sp.setPreferredSize(new Dimension(400, 260));
        body.add(sp, BorderLayout.CENTER);
        dlg.add(body, BorderLayout.CENTER);

        JButton walkInBtn = SwingUtils.outlineButton("Walk-in (no patient)");
        JButton okBtn = SwingUtils.primaryButton("Select");
        JButton cancelBtn = SwingUtils.outlineButton("Cancel");

        walkInBtn.addActionListener(e -> {
            selectedPatient = null;
            updatePatientChip();
            dlg.dispose();
        });
        cancelBtn.addActionListener(e -> dlg.dispose());
        okBtn.addActionListener(e -> {
            int idx = resultList.getSelectedIndex();
            if (idx >= 0 && idx < pHolder[0].size()) {
                selectedPatient = pHolder[0].get(idx);
                updatePatientChip();
                dlg.dispose();
            } else {
                SwingUtils.showError(dlg, "Please select a patient from the list.");
            }
        });

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setOpaque(false);
        btnRow.setBorder(new EmptyBorder(8, 12, 14, 12));
        btnRow.add(walkInBtn);
        btnRow.add(cancelBtn);
        btnRow.add(okBtn);
        dlg.add(btnRow, BorderLayout.SOUTH);

        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private void updatePatientChip() {
        if (selectedPatient == null) {
            patientChip.setText("  Walk-in customer  ");
            patientChip.setForeground(SwingUtils.PRIMARY);
        } else {
            patientChip.setText("  " + selectedPatient.getFullName()
                    + "  •  " + selectedPatient.getPhone()
                    + "  •  " + selectedPatient.getLoyaltyPoints() + " pts  ");
            patientChip.setForeground(SwingUtils.SUCCESS.darker());
        }
    }

    // ── Checkout ─────────────────────────────────────────────

    private void checkout() {
        if (cart.isEmpty()) {
            showError("Cart is empty — add medicines before checkout.");
            return;
        }

        double discPct = safeDouble(discountField.getText());
        double taxPct = safeDouble(taxField.getText());
        SaleSummary summary = posService.computeSummary(cart, discPct, taxPct);
        double amountPaid = safeDouble(amountPaidField.getText());

        String pmStr = (String) paymentBox.getSelectedItem();
        Sale.PaymentMethod pm = Sale.PaymentMethod.valueOf(pmStr);
        Payable payment = buildPayment(pm, summary.grandTotal(), amountPaid);
        if (payment == null) return;
        if (pm != Sale.PaymentMethod.CASH) amountPaid = summary.grandTotal();

        String msg = String.format(
                "Process sale of Rs %,.2f for %s?%nPayment: %s | Paid: Rs %,.2f | Change: Rs %,.2f",
                summary.grandTotal(),
                selectedPatient != null ? selectedPatient.getFullName() : "Walk-in",
                pm, amountPaid, summary.changeFor(amountPaid));

        if (!SwingUtils.confirm(this, msg)) return;

        Result<Sale> r = posService.checkout(cart, summary, pm, amountPaid, selectedPatient);
        if (r.isFailure()) {
            showError(r.getErrorMessage());
            return;
        }

        Sale sale = r.getData();
        sale.setPayment(payment);
        try {
            sale.processPayment();
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
            return;
        }
        showReceipt(sale);
        clearCart();
    }

    private Payable buildPayment(Sale.PaymentMethod method, double total, double amountPaid) {
        if (method == Sale.PaymentMethod.CASH) {
            return new CashPayment(amountPaid);
        }
        if (method == Sale.PaymentMethod.CARD) {
            JTextField cardNoF = SwingUtils.styledField("Card number");
            JTextField holderF = SwingUtils.styledField("Card holder name");
            JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
            form.setBorder(new EmptyBorder(10, 10, 10, 10));
            form.add(SwingUtils.fieldLabel("Card Number"));
            form.add(cardNoF);
            form.add(SwingUtils.fieldLabel("Card Holder"));
            form.add(holderF);
            int res = JOptionPane.showConfirmDialog(
                    this, form, "Card Payment",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (res != JOptionPane.OK_OPTION) return null;
            CardPayment payment = new CardPayment(cardNoF.getText().trim(), holderF.getText().trim());
            try {
                payment.processPayment(total);
            } catch (IllegalArgumentException ex) {
                SwingUtils.showError(this, ex.getMessage());
                return null;
            }
            return payment;
        }
        return new Payable() {
            private static final long serialVersionUID = 1L;

            @Override
            public void processPayment(double amount) { /* wallet */ }

            @Override
            public String generateReceipt() {
                return String.format("Digital wallet payment | Rs %.2f", total);
            }

            @Override
            public String getPaymentType() {
                return "Digital Wallet";
            }
        };
    }

    // ── Receipt ────────────────────────────────────────────

    private void showReceipt(Sale sale) {
        StringBuilder sb = new StringBuilder();
        sb.append("==============================\n");
        sb.append("        MEDICARE PHARMACY\n");
        sb.append("    Blue Area, Islamabad\n");
        sb.append("==============================\n");
        sb.append(String.format("Receipt #: %d%n", sale.getId()));
        sb.append(String.format("Date: %s%n", sale.getSaleDate().toLocalDate()));
        sb.append(String.format("Cashier: %s%n", sale.getCashierName()));
        if (!sale.isWalkIn()) {
            sb.append(String.format("Patient: %s%n", sale.getPatientName()));
        }
        sb.append("------------------------------\n");
        for (SaleItem item : sale.getItems()) {
            sb.append(String.format("%-20s x%d%n", item.getMedicineName(), item.getQuantity()));
            sb.append(String.format("  Rs %.2f x %d = Rs %.2f%n",
                    item.getUnitPrice(), item.getQuantity(), item.getSubtotal()));
        }
        sb.append("------------------------------\n");
        sb.append(String.format("Subtotal:     Rs %.2f%n", sale.getSubtotal()));
        if (sale.getDiscountAmount() > 0) {
            sb.append(String.format("Discount:   - Rs %.2f%n", sale.getDiscountAmount()));
        }
        if (sale.getTaxAmount() > 0) {
            sb.append(String.format("Tax:          Rs %.2f%n", sale.getTaxAmount()));
        }
        sb.append(String.format("GRAND TOTAL:  Rs %.2f%n", sale.getGrandTotal()));
        sb.append(String.format("Paid:         Rs %.2f%n", sale.getAmountPaid()));
        sb.append(String.format("Change:       Rs %.2f%n", sale.getChangeReturned()));
        if (sale.getPayment() != null) {
            sb.append(String.format("Payment Type: %s%n", sale.getPayment().getPaymentType()));
            sb.append(sale.getPayment().generateReceipt()).append("\n");
        }
        sb.append("------------------------------\n");
        sb.append(sale.generateInvoice().format()).append("\n");
        sb.append("==============================\n");
        sb.append("  Thank you for your visit!\n");
        sb.append("==============================\n");

        JDialog dlg = new JDialog(
                SwingUtilities.getWindowAncestor(this),
                "Sale Complete — Receipt #" + sale.getId(),
                Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setLayout(new BorderLayout());
        dlg.getContentPane().setBackground(Color.WHITE);

        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setBackground(SwingUtils.SUCCESS);
        titleBar.setBorder(new EmptyBorder(14, 18, 14, 18));
        JLabel titleLbl = new JLabel("Sale Complete — Receipt #" + sale.getId());
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLbl.setForeground(Color.WHITE);
        titleBar.add(titleLbl, BorderLayout.WEST);
        dlg.add(titleBar, BorderLayout.NORTH);

        JTextArea ta = new JTextArea(sb.toString());
        ta.setFont(SwingUtils.FONT_MONO);
        ta.setEditable(false);
        ta.setBackground(new Color(248, 250, 252));
        ta.setBorder(new EmptyBorder(12, 14, 12, 14));

        dlg.add(new JScrollPane(ta), BorderLayout.CENTER);

        JButton closeBtn = SwingUtils.primaryButton("Close");
        closeBtn.addActionListener(e -> dlg.dispose());
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.setOpaque(false);
        south.setBorder(new EmptyBorder(8, 12, 14, 12));
        south.add(closeBtn);
        dlg.add(south, BorderLayout.SOUTH);

        dlg.setSize(420, 520);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    // ── Table renderers ────────────────────────────────────

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

    private static class MoneyRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean selected, boolean focused, int row, int col) {
            Component c = super.getTableCellRendererComponent(table, value, selected, focused, row, col);
            setHorizontalAlignment(SwingConstants.RIGHT);
            if (!selected) {
                setForeground(SwingUtils.PRIMARY);
                setFont(new Font("Segoe UI", Font.BOLD, 13));
                c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(249, 251, 253));
            }
            setBorder(new EmptyBorder(0, 10, 0, 10));
            return c;
        }
    }

    // ── UI helpers ─────────────────────────────────────────

    /** Scroll pane with a always-visible vertical scrollbar. */
    private static JScrollPane verticalScroll(Component view) {
        JScrollPane scroll = new JScrollPane(view);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getHorizontalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    private JLabel sectionLabel(String text) {
        JLabel l = SwingUtils.headingLabel(text);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JPanel wrapLabeled(String label, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 68));
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(SwingUtils.TEXT_GRAY);
        p.add(lbl, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private JPanel summaryRow(String label, JLabel value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        row.add(SwingUtils.fieldLabel(label), BorderLayout.WEST);
        value.setHorizontalAlignment(SwingConstants.RIGHT);
        row.add(value, BorderLayout.EAST);
        return row;
    }

    private JPanel inputSummaryRow(String label, JTextField input, JLabel computed) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        row.add(SwingUtils.fieldLabel(label), BorderLayout.WEST);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(input);
        computed.setHorizontalAlignment(SwingConstants.RIGHT);
        right.add(computed);
        row.add(right, BorderLayout.EAST);
        return row;
    }

    private JLabel amountLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(SwingUtils.FONT_BODY);
        l.setForeground(SwingUtils.TEXT_DARK);
        l.setHorizontalAlignment(SwingConstants.RIGHT);
        return l;
    }

    private double safeDouble(String s) {
        try {
            return Double.parseDouble(s.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private double parseAmount(String text) {
        return safeDouble(text.replace("Rs", "").replace(",", "").trim());
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setForeground(SwingUtils.DANGER);
    }

    private void hideError() {
        errorLabel.setText(" ");
    }

    private DocumentListener docListener(Runnable action) {
        return new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                action.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                action.run();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                action.run();
            }
        };
    }
}
