package com.pharmacy.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Prescription extends Entity {
    private static final long serialVersionUID = 1L;

    private String prescriptionID;
    private String patientID;
    private String doctorName;
    private List<Medicine> medicines;
    private boolean isFulfilled;
    private LocalDate issueDate;

    public Prescription() {
        super();
        this.medicines = new ArrayList<>();
        this.issueDate = LocalDate.now();
    }

    @Override
    public String getDisplayName() {
        return "Prescription " + (prescriptionID != null ? prescriptionID : getId());
    }

    @Override
    public boolean isValid() {
        return doctorName != null && !doctorName.isBlank();
    }

    public void fulfill() {
        isFulfilled = true;
    }

    public boolean validate() {
        return isValid() && medicines != null && !medicines.isEmpty();
    }

    public boolean isFulfilled() {
        return isFulfilled;
    }

    public String getPrescriptionID() { return prescriptionID; }
    public void setPrescriptionID(String v) { this.prescriptionID = v; }
    public String getPatientID() { return patientID; }
    public void setPatientID(String v) { this.patientID = v; }
    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String v) { this.doctorName = v; }
    public List<Medicine> getMedicines() { return medicines; }
    public void setMedicines(List<Medicine> v) { this.medicines = v != null ? v : new ArrayList<>(); }
    public void setFulfilled(boolean v) { this.isFulfilled = v; }
    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate v) { this.issueDate = v; }
}
