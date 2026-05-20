package com.pharmacy.ui;

import com.pharmacy.dao.MedicineDAO;
import com.pharmacy.model.Medicine;
import com.pharmacy.model.Patient;
import com.pharmacy.model.Prescription;
import com.pharmacy.service.PatientService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class PrescriptionPanel extends JPanel {
    private final PatientService patientService = PatientService.getInstance();
    private final MedicineDAO medicineDAO = MedicineDAO.getInstance();

    private JTable table;
    private DefaultTableModel model;
    private JTextField searchField;

    public PrescriptionPanel() {
        setLayout(new BorderLayout());
        setBackground(SwingUtils.BG);
        buildUI();
        load(null);
    }

    private void buildUI() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, SwingUtils.BORDER),
            BorderFactory.createEmptyBorder(16, 24, 12, 24)));
        header.add(SwingUtils.titleLabel("Prescription Management"), BorderLayout.WEST);
        JButton addBtn = SwingUtils.successButton("+ New Prescription");
        addBtn.addActionListener(e -> showPrescriptionDialog());
        header.add(addBtn, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        searchField = SwingUtils.styledField("Search patient, doctor, medicine...");
        searchField.setPreferredSize(new Dimension(320, 34));
        JButton searchBtn = SwingUtils.primaryButton("Search");
        JButton refreshBtn = SwingUtils.outlineButton("Refresh");
        JButton fulfillBtn = SwingUtils.successButton("Fulfill Selected");
        JButton detailsBtn = SwingUtils.outlineButton("Details");

        searchBtn.addActionListener(e -> load(searchField.getText()));
        searchField.addActionListener(e -> load(searchField.getText()));
        refreshBtn.addActionListener(e -> { searchField.setText(""); load(null); });
        fulfillBtn.addActionListener(e -> fulfillSelected());
        detailsBtn.addActionListener(e -> showSelectedDetails());

        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);
        toolbar.add(SwingUtils.hBox(searchField, searchBtn, refreshBtn), BorderLayout.WEST);
        toolbar.add(SwingUtils.hBox(detailsBtn, fulfillBtn), BorderLayout.EAST);

        String[] cols = {"Patient ID", "Rx ID", "Patient", "Doctor", "Issue Date", "Medicines", "Status"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        SwingUtils.styleTable(table);
        table.getColumnModel().getColumn(0).setMaxWidth(80);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel center = SwingUtils.card();
        center.setLayout(new BorderLayout(0, 10));
        center.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));
        center.add(toolbar, BorderLayout.NORTH);
        center.add(new JScrollPane(table), BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);
    }

    private void load(String keyword) {
        model.setRowCount(0);
        String kw = keyword == null ? "" : keyword.trim().toLowerCase();
        for (Patient patient : patientService.getAllActive()) {
            for (Prescription rx : patient.getPrescriptionHistory()) {
                String medicines = medicineNames(rx);
                String haystack = (patient.getFullName() + " " + rx.getDoctorName() + " " + medicines).toLowerCase();
                if (!kw.isEmpty() && !haystack.contains(kw)) continue;
                model.addRow(new Object[]{
                    patient.getId(),
                    rx.getPrescriptionID(),
                    patient.getFullName(),
                    rx.getDoctorName(),
                    rx.getIssueDate(),
                    medicines,
                    rx.isFulfilled() ? "Fulfilled" : "Open"
                });
            }
        }
    }

    private void showPrescriptionDialog() {
        List<Patient> patients = patientService.getAllActive();
        if (patients.isEmpty()) {
            SwingUtils.showError(this, "Add a patient before creating prescriptions.");
            return;
        }

        JComboBox<Patient> patientBox = new JComboBox<>(patients.toArray(new Patient[0]));
        JTextField rxIdF = SwingUtils.styledField("Prescription ID");
        JTextField doctorF = SwingUtils.styledField("Doctor name");
        JTextField issueF = SwingUtils.styledField("YYYY-MM-DD");
        issueF.setText(LocalDate.now().toString());
        JTextField medSearchF = SwingUtils.styledField("Start typing medicine name...");
        JButton addMedBtn = SwingUtils.primaryButton("Add");
        DefaultListModel<Medicine> selectedMedsModel = new DefaultListModel<>();
        JList<Medicine> selectedMedsList = new JList<>(selectedMedsModel);
        selectedMedsList.setVisibleRowCount(5);

        Runnable addCurrentMed = () -> {
            String query = medSearchF.getText().trim();
            if(query.isEmpty()) return;
            List<Medicine> matches = medicineDAO.search(query);
            if(matches.isEmpty()){
                Medicine m = matches.get(0);
                if(!selectedMedsModel.contains(m)) selectedMedsModel.addElement(m);
                medSearchF.setText("");
                medSearchF.requestFocusInWindow();
            }
        };

        new MedicineSuggestionPopup(medSearchF, medicineDAO::search, medicine -> {
            if (!selectedMedsModel.contains(medicine)) selectedMedsModel.addElement(medicine);
            medSearchF.setText("");
            medSearchF.requestFocusInWindow();
        });

        addMedBtn.addActionListener(e -> addCurrentMed.run());
        medSearchF.addActionListener(e -> addCurrentMed.run());

        JButton removeMedBtn = SwingUtils.outlineButton("Remove Medicine");
        removeMedBtn.addActionListener(e -> {
            int idx = selectedMedsList.getSelectedIndex();
            if (idx >= 0) selectedMedsModel.remove(idx);
        });

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(5, 5, 5, 5);
        gc.fill = GridBagConstraints.HORIZONTAL;


        Component[][] rows = {
            {SwingUtils.fieldLabel("Patient"), patientBox},
            {SwingUtils.fieldLabel("Prescription ID"), rxIdF},
            {SwingUtils.fieldLabel("Doctor *"), doctorF},
            {SwingUtils.fieldLabel("Issue Date *"), issueF},
            {SwingUtils.fieldLabel("Medicine Search"), medSearchF}
        };

        for (int i = 0; i < rows.length; i++) {
            gc.gridx = 0; gc.gridy = i; gc.weightx = 0.25;
            form.add(rows[i][0], gc);
            gc.gridx = 1; gc.weightx = 0.75;
            form.add(rows[i][1], gc);
        }

        gc.gridx = 0; gc.gridy = rows.length; gc.weightx = 0.25;
        form.add(SwingUtils.fieldLabel("Selected Medicines"), gc);
        gc.gridx = 1; gc.weightx = 0.75;
        JScrollPane medScroll = new JScrollPane(selectedMedsList);
        medScroll.setPreferredSize(new Dimension(380, 120));
        form.add(medScroll, gc);
        gc.gridy++;
        form.add(removeMedBtn, gc);

        int res = JOptionPane.showConfirmDialog(this, form, "New Prescription",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;

        Patient patient = (Patient) patientBox.getSelectedItem();
        if (patient == null) return;
        if (doctorF.getText().trim().isEmpty()) {
            SwingUtils.showError(this, "Doctor name is required.");
            return;
        }
        if (selectedMedsModel.isEmpty()) {
            SwingUtils.showError(this, "Select at least one medicine.");
            return;
        }

        Prescription rx = new Prescription();
        rx.setPrescriptionID(rxIdF.getText().trim().isEmpty()
            ? "RX-" + System.currentTimeMillis()
            : rxIdF.getText().trim());
        rx.setPatientID(String.valueOf(patient.getId()));
        rx.setDoctorName(doctorF.getText().trim());
        try {
            rx.setIssueDate(LocalDate.parse(issueF.getText().trim()));
        } catch (DateTimeParseException ex) {
            SwingUtils.showError(this, "Issue date must use YYYY-MM-DD format.");
            return;
        }
        List<Medicine> meds = new ArrayList<>();
        for (int i = 0; i < selectedMedsModel.size(); i++) meds.add(selectedMedsModel.get(i));
        rx.setMedicines(meds);
        patient.addPrescription(rx);
        patientService.updatePatient(patient);
        load(null);
    }

    private void fulfillSelected() {
        PrescriptionSelection selection = getSelection();
        if (selection == null) return;
        selection.prescription.fulfill();
        patientService.updatePatient(selection.patient);
        load(searchField.getText());
    }

    private void showSelectedDetails() {
        PrescriptionSelection selection = getSelection();
        if (selection == null) return;
        Prescription rx = selection.prescription;
        String text = String.format(
            "Prescription: %s%nPatient: %s%nDoctor: %s%nIssue Date: %s%nStatus: %s%nMedicines:%n%s",
            rx.getPrescriptionID(),
            selection.patient.getFullName(),
            rx.getDoctorName(),
            rx.getIssueDate(),
            rx.isFulfilled() ? "Fulfilled" : "Open",
            medicineNames(rx).replace(", ", "\n"));
        JTextArea area = new JTextArea(text);
        area.setEditable(false);
        area.setFont(SwingUtils.FONT_MONO);
        JOptionPane.showMessageDialog(this, new JScrollPane(area), "Prescription Details", JOptionPane.INFORMATION_MESSAGE);
    }

    private PrescriptionSelection getSelection() {
        int row = table.getSelectedRow();
        if (row < 0) {
            SwingUtils.showError(this, "Select a prescription first.");
            return null;
        }
        int patientId = (int) model.getValueAt(row, 0);
        String rxId = String.valueOf(model.getValueAt(row, 1));
        Patient patient = patientService.findById(patientId).orElse(null);
        if (patient == null) return null;
        return patient.getPrescriptionHistory().stream()
            .filter(rx -> rxId.equals(rx.getPrescriptionID()))
            .findFirst()
            .map(rx -> new PrescriptionSelection(patient, rx))
            .orElse(null);
    }

    private String medicineNames(Prescription rx) {
        if (rx.getMedicines() == null || rx.getMedicines().isEmpty()) return "None";
        return rx.getMedicines().stream().map(Medicine::getName).reduce((a, b) -> a + ", " + b).orElse("None");
    }

    private record PrescriptionSelection(Patient patient, Prescription prescription) {}
}
