package com.pharmacy.ui;

import com.pharmacy.model.Medicine;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

class MedicineSuggestionPopup {
    private final JTextField field;
    private final Function<String, List<Medicine>> searcher;
    private final Consumer<Medicine> onSelected;
    private final JPopupMenu popup = new JPopupMenu();
    private final DefaultListModel<Medicine> model = new DefaultListModel<>();
    private final JList<Medicine> list = new JList<>(model);

    MedicineSuggestionPopup(JTextField field,
                            Function<String, List<Medicine>> searcher,
                            Consumer<Medicine> onSelected) {
        this.field = field;
        this.searcher = searcher;
        this.onSelected = onSelected;
        buildPopup();
        attach();
    }

    private void buildPopup() {
        list.setFont(SwingUtils.FONT_BODY);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setVisibleRowCount(6);
        list.setCellRenderer((jList, medicine, index, selected, focused) -> {
            JLabel label = new JLabel(format(medicine));
            label.setOpaque(true);
            label.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
            label.setFont(SwingUtils.FONT_BODY);
            label.setBackground(selected ? new Color(219, 234, 254) : Color.WHITE);
            label.setForeground(SwingUtils.TEXT_DARK);
            return label;
        });
        list.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                // Support both single and double click
                chooseSelected();
            }
        });

        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(BorderFactory.createLineBorder(SwingUtils.BORDER));
        scroll.setPreferredSize(new Dimension(Math.max(420, field.getPreferredSize().width), 180));
        popup.setBorder(BorderFactory.createLineBorder(SwingUtils.BORDER));
        popup.add(scroll);

        // Crucial: allow popup to be shown even if field loses focus to the popup itself
        popup.setFocusable(false);
    }

    private void attach() {
        field.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { refresh(); }
            public void removeUpdate(DocumentEvent e) { refresh(); }
            public void changedUpdate(DocumentEvent e) { refresh(); }
        });
        field.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (!popup.isVisible()) return;
                if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    int next = Math.min(list.getSelectedIndex() + 1, model.size() - 1);
                    list.setSelectedIndex(next);
                    list.ensureIndexIsVisible(next);
                    e.consume();
                } else if (e.getKeyCode() == KeyEvent.VK_UP) {
                    int prev = Math.max(list.getSelectedIndex() - 1, 0);
                    list.setSelectedIndex(prev);
                    list.ensureIndexIsVisible(prev);
                    e.consume();
                } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    chooseSelected();
                    e.consume();
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    popup.setVisible(false);
                }
            }
        });
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                // Delay hiding to allow mouse clicks on the popup to process
                Timer timer = new Timer(200, evt -> {
                    if (!field.hasFocus()) popup.setVisible(false);
                });
                timer.setRepeats(false);
                timer.start();
            }
        });
    }

    private void refresh() {
        String text = field.getText().trim();
        if (text.length() < 1 || !field.hasFocus()) {
            popup.setVisible(false);
            return;
        }
        List<Medicine> matches = searcher.apply(text).stream().limit(8).toList();
        model.clear();
        matches.forEach(model::addElement);
        if (model.isEmpty()) {
            popup.setVisible(false);
            return;
        }
        list.setSelectedIndex(0);
        if (!popup.isVisible()) popup.show(field, 0, field.getHeight() + 2);
    }

    private void chooseSelected() {
        Medicine medicine = list.getSelectedValue();
        if (medicine == null) return;
        popup.setVisible(false);
        field.setText(medicine.getName());
        onSelected.accept(medicine);
    }

    private String format(Medicine m) {
        String type = m.isNarcotic() ? "Narcotic" : m.isScheduled() ? "Prescription" : "OTC/General";
        return String.format("%s | %s | Stock %d | Rs %.2f", m.getName(), type, m.getStockQty(), m.getUnitPrice());
    }
}
