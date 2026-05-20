package com.pharmacy.model;

public class PrescriptionMedicine extends Medicine {
    private static final long serialVersionUID = 1L;

    private String doctorApprovalCode;
    private boolean requiresAgeVerification;

    public PrescriptionMedicine() {
        super();
        setScheduled(true);
    }

    @Override
    public String getDetails() {
        return super.getDetails() + " | Prescription required";
    }

    public boolean validateApprovalCode(String code) {
        return doctorApprovalCode != null && doctorApprovalCode.equals(code);
    }

    public String getDoctorApprovalCode() { return doctorApprovalCode; }
    public void setDoctorApprovalCode(String v) { this.doctorApprovalCode = v; }
    public boolean isRequiresAgeVerification() { return requiresAgeVerification; }
    public void setRequiresAgeVerification(boolean v) { this.requiresAgeVerification = v; }
}
