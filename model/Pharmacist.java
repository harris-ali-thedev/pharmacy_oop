package com.pharmacy.model;

import java.util.ArrayList;
import java.util.List;

public class Pharmacist extends User {
    private static final long serialVersionUID = 1L;

    private String licenseNumber;

    public Pharmacist() {
        super();
        setRole(Role.PHARMACIST);
    }

    public Sale processSale(SaleItem item) {
        Sale sale = new Sale();
        sale.addItem(item);
        return sale;
    }

    public void managePrescription(Prescription prescription) {
        if (prescription != null && prescription.validate()) {
            prescription.fulfill();
        }
    }

    public void updateInventory(Medicine medicine) {
        if (medicine != null && medicine.isLowStock()) {
            medicine.addStock(medicine.getReorderLevel());
        }
    }

    @Override
    public String performOperation() {
        return "Pharmacist processes sales, prescriptions, and inventory";
    }

    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String v) { this.licenseNumber = v; }
}
