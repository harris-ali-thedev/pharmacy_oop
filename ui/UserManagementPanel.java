package com.pharmacy.ui;

import com.pharmacy.generics.Result;
import com.pharmacy.model.*;
import com.pharmacy.service.UserService;
import com.pharmacy.util.AuditLogger;
import com.pharmacy.util.SessionManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class UserManagementPanel extends JPanel {

    private final UserService service = UserService.getInstance();
    private JTable            table;
    private DefaultTableModel model;
    private JTextField        searchField;

    public UserManagementPanel() {
        setLayout(new BorderLayout());
        setBackground(SwingUtils.BG);

        if (!SessionManager.getInstance().isAdmin()) {
            add(accessDenied(), BorderLayout.CENTER);
            return;
        }
        buildUI();
        load(null);
    }

    private JPanel accessDenied() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(SwingUtils.BG);
        JLabel l = new JLabel(" Access Denied — Administrators only. ");
        l.setFont(new Font("Segoe UI", Font.BOLD, 18));
        l.setForeground(SwingUtils.DANGER);
        p.add(l);
        return p;
    }

    private void buildUI() {
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, SwingUtils.BORDER),
            BorderFactory.createEmptyBorder(16, 24, 12, 24)));
        header.add(SwingUtils.titleLabel("User Management"), BorderLayout.WEST);
        JButton addBtn = SwingUtils.successButton("+ Add User");
        addBtn.addActionListener(e -> showDialog(null));
        header.add(addBtn, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Toolbar
        searchField = SwingUtils.styledField("Search users...");
        searchField.setPreferredSize(new Dimension(280, 34));
        JButton searchBtn  = SwingUtils.primaryButton("Search");
        JButton refreshBtn = SwingUtils.outlineButton("Refresh");
        JButton editBtn    = SwingUtils.outlineButton("Edit");
        JButton detailsBtn = SwingUtils.outlineButton("Role Details");
        JButton deactBtn   = SwingUtils.dangerButton("Deactivate");
        JButton resetPwdBtn = SwingUtils.warningButton("Reset Pwd");

        searchBtn.addActionListener(e -> load(searchField.getText()));
        searchField.addActionListener(e -> load(searchField.getText()));
        refreshBtn.addActionListener(e -> { searchField.setText(""); load(null); });
        editBtn.addActionListener(e -> editSelected());
        detailsBtn.addActionListener(e -> showRoleDetails());
        deactBtn.addActionListener(e -> deactivateSelected());
        resetPwdBtn.addActionListener(e -> resetPasswordSelected());

        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);
        toolbar.add(SwingUtils.hBox(searchField, searchBtn, refreshBtn), BorderLayout.WEST);
        toolbar.add(SwingUtils.hBox(detailsBtn, resetPwdBtn, editBtn, deactBtn), BorderLayout.EAST);

        // Table
        String[] cols = {"ID", "Username", "Full Name", "Role", "Email", "Phone", "Active", "Locked"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        SwingUtils.styleTable(table);
        table.getColumnModel().getColumn(0).setMaxWidth(50);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(SwingUtils.BORDER));

        JPanel center = SwingUtils.card();
        center.setLayout(new BorderLayout(0, 10));
        center.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));
        center.add(toolbar, BorderLayout.NORTH);
        center.add(scroll, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);
    }

    private void load(String kw) {
        model.setRowCount(0);
        List<User> list = (kw == null || kw.isBlank())
            ? service.getAllUsers() : service.searchUsers(kw);
        for (User u : list) {
            model.addRow(new Object[]{
                u.getId(), u.getUsername(), u.getFullName(),
                u.getRole().getLabel(), u.getEmail(), u.getPhone(),
                u.isActive() ? "Yes" : "No",
                u.isLocked() ? "LOCKED" : "-"
            });
        }
    }

    private User getSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { SwingUtils.showError(this, "Select a user first."); return null; }
        int id = (int) model.getValueAt(row, 0);
        return service.findById(id).orElse(null);
    }

    private void editSelected()   { User u = getSelected(); if (u != null) showDialog(u); }

    private void showRoleDetails() {
        User u = getSelected();
        if (u == null) return;
        StringBuilder sb = new StringBuilder();
        sb.append(u.getDetails()).append("\n\n");
        sb.append("Operation: ").append(u.performOperation()).append("\n");
        if (u instanceof Admin admin) {
            sb.append("System log entries: ").append(admin.viewSystemLogs().size()).append("\n");
        } else if (u instanceof Pharmacist pharmacist) {
            sb.append("License: ").append(pharmacist.getLicenseNumber() != null ? pharmacist.getLicenseNumber() : "Not recorded").append("\n");
        }
        JTextArea area = new JTextArea(sb.toString());
        area.setEditable(false);
        area.setFont(SwingUtils.FONT_MONO);
        JOptionPane.showMessageDialog(this, new JScrollPane(area), "User Role Details", JOptionPane.INFORMATION_MESSAGE);
    }

    private void deactivateSelected() {
        User u = getSelected(); if (u == null) return;
        if (!SwingUtils.confirm(this, "Deactivate user '" + u.getUsername() + "'?")) return;
        Result<Void> r = service.deactivateUser(u.getId());
        if (r.isFailure()) SwingUtils.showError(this, r.getErrorMessage());
        else load(null);
    }

    private void resetPasswordSelected() {
        User u = getSelected(); if (u == null) return;
        String pwd = SwingUtils.inputDialog(this,
            "New password for " + u.getUsername() + " (min 6 chars):", "");
        if (pwd == null || pwd.isBlank()) return;
        Result<Void> r = service.changePassword(u.getId(), pwd);
        if (r.isFailure()) SwingUtils.showError(this, r.getErrorMessage());
        else SwingUtils.showInfo(this, "Password reset successfully.");
    }

    private void showDialog(User existing) {
        boolean isNew = (existing == null);
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this),
            isNew ? "Add User" : "Edit User", Dialog.ModalityType.APPLICATION_MODAL);

        JTextField usernameF = SwingUtils.styledField("Username");
        JTextField fullNameF = SwingUtils.styledField("Full name");
        JTextField emailF    = SwingUtils.styledField("Email");
        JTextField phoneF    = SwingUtils.styledField("Phone");
        JTextField licenseF  = SwingUtils.styledField("Pharmacist license number");
        JPasswordField passF = SwingUtils.styledPasswordField();
        JComboBox<Role> roleBox = new JComboBox<>(Role.values());
        JCheckBox activeBox = new JCheckBox("Active", true);

        if (!isNew) {
            usernameF.setText(existing.getUsername());
            usernameF.setEnabled(false); // username not changeable in edit
            fullNameF.setText(existing.getFullName());
            emailF.setText(existing.getEmail() != null ? existing.getEmail() : "");
            phoneF.setText(existing.getPhone() != null ? existing.getPhone() : "");
            if (existing instanceof Pharmacist pharmacist) {
                licenseF.setText(pharmacist.getLicenseNumber() != null ? pharmacist.getLicenseNumber() : "");
            }
            roleBox.setSelectedItem(existing.getRole());
            activeBox.setSelected(existing.isActive());
        }

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(5, 5, 5, 5);

        Object[][] rows = {
            {"Username *",  usernameF},
            {"Full Name *", fullNameF},
            {"Email",       emailF},
            {"Phone",       phoneF},
            {"Role",        roleBox},
            {"License No.",  licenseF},
        };
        if (isNew) {
            Object[][] allRows = new Object[rows.length + 1][];
            System.arraycopy(rows, 0, allRows, 0, rows.length);
            allRows[rows.length] = new Object[]{"Password *", passF};
            rows = allRows;
        }

        for (int i = 0; i < rows.length; i++) {
            gc.gridx = 0; gc.gridy = i; gc.weightx = 0.3;
            form.add(SwingUtils.fieldLabel((String) rows[i][0]), gc);
            gc.gridx = 1; gc.weightx = 0.7;
            form.add((Component) rows[i][1], gc);
        }
        gc.gridx = 1; gc.gridy = rows.length;
        form.add(activeBox, gc);

        JButton saveBtn   = SwingUtils.successButton("Save");
        JButton cancelBtn = SwingUtils.outlineButton("Cancel");
        cancelBtn.addActionListener(e -> dlg.dispose());
        saveBtn.addActionListener(e -> {
            if (usernameF.getText().trim().isEmpty() || fullNameF.getText().trim().isEmpty()) {
                SwingUtils.showError(dlg, "Username and full name are required.");
                return;
            }
            User u = isNew ? createUserByRole((Role) roleBox.getSelectedItem()) : existing;
            u.setUsername(usernameF.getText().trim().toLowerCase());
            u.setFullName(fullNameF.getText().trim());
            u.setEmail(emailF.getText().trim());
            u.setPhone(phoneF.getText().trim());
            u.setRole((Role) roleBox.getSelectedItem());
            u.setActive(activeBox.isSelected());
            if (u instanceof Pharmacist pharmacist) {
                pharmacist.setLicenseNumber(licenseF.getText().trim());
            }

            Result<?> r;
            if (isNew) {
                String pwd = new String(passF.getPassword());
                r = service.createUser(u, pwd);
            } else {
                r = service.updateUser(u);
            }
            if (r.isFailure()) SwingUtils.showError(dlg, r.getErrorMessage());
            else { dlg.dispose(); load(null); }
        });

        dlg.setLayout(new BorderLayout());
        dlg.add(new JScrollPane(form), BorderLayout.CENTER);
        dlg.add(SwingUtils.hBox(saveBtn, cancelBtn), BorderLayout.SOUTH);
        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private User createUserByRole(Role role) {
        if (role == Role.ADMIN) return new Admin();
        if (role == Role.PHARMACIST) return new Pharmacist();
        return new User();
    }
}
