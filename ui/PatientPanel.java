package com.pharmacy.ui;

import com.pharmacy.generics.Result;
import com.pharmacy.model.*;
import com.pharmacy.service.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class PatientPanel extends JPanel {

    private final PatientService service = PatientService.getInstance();

    private JTable            table;
    private DefaultTableModel model;
    private JTextField        searchField;

    public PatientPanel() {
        setLayout(new BorderLayout());
        setBackground(SwingUtils.BG);
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
        header.add(SwingUtils.titleLabel("Patient Management"), BorderLayout.WEST);
        JButton addBtn = SwingUtils.successButton("+ New Patient");
        addBtn.addActionListener(e -> showDialog(null));
        header.add(addBtn, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Toolbar
        searchField = SwingUtils.styledField("Search by name, phone, or NIC...");
        searchField.setPreferredSize(new Dimension(300, 34));
        JButton searchBtn  = SwingUtils.primaryButton("Search");
        JButton refreshBtn = SwingUtils.outlineButton("Refresh");
        JButton editBtn    = SwingUtils.outlineButton("Edit");
        JButton rxBtn      = SwingUtils.outlineButton("Prescriptions");
        JButton deleteBtn  = SwingUtils.dangerButton("Delete");

        searchBtn.addActionListener(e -> load(searchField.getText()));
        searchField.addActionListener(e -> load(searchField.getText()));
        refreshBtn.addActionListener(e -> { searchField.setText(""); load(null); });
        editBtn.addActionListener(e -> editSelected());
        rxBtn.addActionListener(e -> managePrescriptions());
        deleteBtn.addActionListener(e -> deleteSelected());

        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);
        toolbar.add(SwingUtils.hBox(searchField, searchBtn, refreshBtn), BorderLayout.WEST);
        toolbar.add(SwingUtils.hBox(rxBtn, editBtn, deleteBtn), BorderLayout.EAST);

        // Table
        String[] cols = {"ID", "Name", "Phone", "Email", "NIC", "Allergies", "Prescriptions", "Points", "Active"};
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
        center.add(scroll,  BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);
    }

    private void load(String keyword) {
        model.setRowCount(0);
        List<Patient> list = (keyword == null || keyword.isBlank())
            ? service.getAllActive() : service.search(keyword);
        for (Patient p : list) {
            model.addRow(new Object[]{
                p.getId(), p.getFullName(), p.getPhone(), p.getEmail(),
                p.getNicNumber(),
                p.getAllergies().isEmpty() ? "None" : String.join(", ", p.getAllergies()),
                p.getPrescriptionHistory().size(),
                p.getLoyaltyPoints(), p.isActive() ? "Yes" : "No"
            });
        }
    }

    private Patient getSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { SwingUtils.showError(this, "Select a patient first."); return null; }
        int id = (int) model.getValueAt(row, 0);
        return service.findById(id).orElse(null);
    }

    private void editSelected()   { Patient p = getSelected(); if (p != null) showDialog(p); }

    private void managePrescriptions() {
        Patient p = getSelected();
        if (p == null) return;

        DefaultListModel<String> rxModel = new DefaultListModel<>();
        JList<String> rxList = new JList<>(rxModel);
        Runnable refresh = () -> {
            rxModel.clear();
            for (Prescription rx : p.getPrescriptionHistory()) {
                rxModel.addElement(String.format("%s | Dr. %s | %s | %s",
                    rx.getPrescriptionID() != null ? rx.getPrescriptionID() : "RX-" + rx.getId(),
                    rx.getDoctorName(),
                    rx.getIssueDate(),
                    rx.isFulfilled() ? "Fulfilled" : "Open"));
            }
        };
        refresh.run();

        JTextField idF = SwingUtils.styledField("Prescription ID");
        JTextField doctorF = SwingUtils.styledField("Doctor name");
        JTextField issueF = SwingUtils.styledField("YYYY-MM-DD");
        JCheckBox fulfilledCb = new JCheckBox("Fulfilled");
        issueF.setText(LocalDate.now().toString());

        JButton addRxBtn = SwingUtils.successButton("+ Add Prescription");
        JButton fulfillBtn = SwingUtils.outlineButton("Fulfill Selected");

        addRxBtn.addActionListener(e -> {
            if (doctorF.getText().trim().isEmpty()) {
                SwingUtils.showError(this, "Doctor name is required.");
                return;
            }
            Prescription rx = new Prescription();
            rx.setPrescriptionID(idF.getText().trim().isEmpty()
                ? "RX-" + System.currentTimeMillis()
                : idF.getText().trim());
            rx.setPatientID(String.valueOf(p.getId()));
            rx.setDoctorName(doctorF.getText().trim());
            try { rx.setIssueDate(LocalDate.parse(issueF.getText().trim())); }
            catch (DateTimeParseException ex) {
                SwingUtils.showError(this, "Issue date must use YYYY-MM-DD format.");
                return;
            }
            rx.setFulfilled(fulfilledCb.isSelected());
            p.addPrescription(rx);
            service.updatePatient(p);
            refresh.run();
            load(null);
        });

        fulfillBtn.addActionListener(e -> {
            int idx = rxList.getSelectedIndex();
            if (idx < 0 || idx >= p.getPrescriptionHistory().size()) return;
            p.getPrescriptionHistory().get(idx).fulfill();
            service.updatePatient(p);
            refresh.run();
            load(null);
        });

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.add(SwingUtils.fieldLabel("Prescription ID"));
        form.add(idF);
        form.add(SwingUtils.fieldLabel("Doctor"));
        form.add(doctorF);
        form.add(SwingUtils.fieldLabel("Issue Date"));
        form.add(issueF);
        form.add(new JLabel());
        form.add(fulfilledCb);

        JPanel actions = SwingUtils.hBox(addRxBtn, fulfillBtn);
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JScrollPane listScroll = new JScrollPane(rxList);
        listScroll.setPreferredSize(new Dimension(520, 180));
        panel.add(listScroll, BorderLayout.NORTH);
        panel.add(form, BorderLayout.CENTER);
        panel.add(actions, BorderLayout.SOUTH);

        JOptionPane.showMessageDialog(this, panel, "Prescription History - " + p.getFullName(), JOptionPane.PLAIN_MESSAGE);
    }
    private void deleteSelected() {
        Patient p = getSelected();
        if (p == null) return;
        if (!SwingUtils.confirm(this, "Deactivate patient '" + p.getFullName() + "'?")) return;
        service.deletePatient(p.getId());
        load(null);
    }

    private void showDialog(Patient existing) {
        boolean isNew = (existing == null);
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this),
            isNew ? "New Patient" : "Edit Patient", Dialog.ModalityType.APPLICATION_MODAL);

        JTextField firstF    = SwingUtils.styledField("First name");
        JTextField lastF     = SwingUtils.styledField("Last name");
        JTextField phoneF    = SwingUtils.styledField("Phone number");
        JTextField emailF    = SwingUtils.styledField("Email address");
        JTextField nicF      = SwingUtils.styledField("NIC / CNIC number");
        JTextField addrF     = SwingUtils.styledField("Address");
        JTextField allergyF  = SwingUtils.styledField("Comma-separated allergies");

        if (!isNew) {
            firstF.setText(existing.getFirstName());
            lastF.setText(existing.getLastName());
            phoneF.setText(existing.getPhone() != null ? existing.getPhone() : "");
            emailF.setText(existing.getEmail() != null ? existing.getEmail() : "");
            nicF.setText(existing.getNicNumber() != null ? existing.getNicNumber() : "");
            addrF.setText(existing.getAddress() != null ? existing.getAddress() : "");
            allergyF.setText(String.join(", ", existing.getAllergies()));
        }

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(5, 5, 5, 5);

        Object[][] rows = {
            {"First Name *", firstF}, {"Last Name *", lastF},
            {"Phone", phoneF},        {"Email", emailF},
            {"NIC Number", nicF},     {"Address", addrF},
            {"Allergies", allergyF}
        };
        for (int i = 0; i < rows.length; i++) {
            gc.gridx = 0; gc.gridy = i; gc.weightx = 0.3;
            form.add(SwingUtils.fieldLabel((String) rows[i][0]), gc);
            gc.gridx = 1; gc.weightx = 0.7;
            form.add((Component) rows[i][1], gc);
        }

        JButton saveBtn   = SwingUtils.successButton("Save");
        JButton cancelBtn = SwingUtils.outlineButton("Cancel");
        cancelBtn.addActionListener(e -> dlg.dispose());
        saveBtn.addActionListener(e -> {
            if (firstF.getText().trim().isEmpty() || lastF.getText().trim().isEmpty()) {
                SwingUtils.showError(dlg, "First and last name are required.");
                return;
            }
            Patient p = isNew ? new Patient() : existing;
            p.setFirstName(firstF.getText().trim());
            p.setLastName(lastF.getText().trim());
            p.setPhone(phoneF.getText().trim());
            p.setEmail(emailF.getText().trim());
            p.setNicNumber(nicF.getText().trim());
            p.setAddress(addrF.getText().trim());
            if (!allergyF.getText().isBlank()) {
                java.util.Arrays.stream(allergyF.getText().split(","))
                    .map(String::trim).filter(s -> !s.isEmpty())
                    .forEach(p::addAllergy);
            }
            Result<?> r = isNew ? service.addPatient(p) : service.updatePatient(p);
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
}
