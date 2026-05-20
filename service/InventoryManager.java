package com.pharmacy.service;

import com.pharmacy.dao.MedicineDAO;
import com.pharmacy.model.Medicine;

import java.util.List;
import java.util.Optional;

public class InventoryManager {
    private final MedicineDAO medicineDAO = MedicineDAO.getInstance();
    private final int lowStockThreshold;

    public InventoryManager() {
        this(20);
    }

    public InventoryManager(int lowStockThreshold) {
        this.lowStockThreshold = lowStockThreshold;
    }

    public void addMedicine(Medicine medicine) {
        medicineDAO.save(medicine);
    }

    public void removeMedicine(String id) {
        parseId(id).ifPresent(medicineDAO::deleteById);
    }

    public List<Medicine> getLowStockAlerts() {
        return medicineDAO.findAll().stream()
            .filter(m -> m.isActive() && m.isLowStock(lowStockThreshold))
            .toList();
    }

    public List<Medicine> getExpiryAlerts() {
        return medicineDAO.findAll().stream()
            .filter(m -> m.isActive() && m.isExpired())
            .toList();
    }

    public Medicine findByID(String id) {
        return parseId(id).flatMap(medicineDAO::findById).orElse(null);
    }

    public List<Medicine> searchByName(String query) {
        return medicineDAO.search(query);
    }

    public int getLowStockThreshold() {
        return lowStockThreshold;
    }

    private Optional<Integer> parseId(String id) {
        try {
            return Optional.of(Integer.parseInt(id));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
