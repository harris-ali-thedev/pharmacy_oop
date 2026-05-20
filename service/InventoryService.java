package com.pharmacy.service;

import com.pharmacy.dao.*;
import com.pharmacy.generics.*;
import com.pharmacy.model.*;
import com.pharmacy.util.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Inventory management service.
 * Demonstrates: Service layer, Generics (Result<T>), OOP design
 */
public class InventoryService {

    private static InventoryService instance;
    private final MedicineDAO  medDAO      = MedicineDAO.getInstance();
    private final SupplierDAO  supplierDAO = SupplierDAO.getInstance();

    private InventoryService() {}

    public static synchronized InventoryService getInstance() {
        if (instance == null) instance = new InventoryService();
        return instance;
    }

    public Result<Medicine> addMedicine(Medicine med) {
        if (!med.isValid()) return Result.fail("Medicine data is invalid.");
        Medicine saved = medDAO.save(med);
        AuditLogger.log("ADD_MEDICINE", "Added: " + saved.getName() + " (ID=" + saved.getId() + ")");
        return Result.ok(saved);
    }

    public Result<Medicine> updateMedicine(Medicine med) {
        if (!medDAO.findById(med.getId()).isPresent())
            return Result.fail("Medicine ID " + med.getId() + " not found.");
        medDAO.update(med);
        AuditLogger.log("EDIT_MEDICINE", "Updated: " + med.getName());
        return Result.ok(med);
    }

    public Result<Void> deleteMedicine(int id) {
        Optional<Medicine> opt = medDAO.findById(id);
        if (opt.isEmpty()) return Result.fail("Medicine not found.");
        Medicine med = opt.get();
        med.deactivate();
        medDAO.update(med);
        AuditLogger.log("DELETE_MEDICINE", "Deactivated: " + med.getName());
        return Result.ok();
    }

    public Result<Void> addStock(int medId, int qty, String batchNote) {
        Optional<Medicine> opt = medDAO.findById(medId);
        if (opt.isEmpty()) return Result.fail("Medicine not found.");
        Medicine med = opt.get();
        if (qty <= 0) return Result.fail("Quantity must be positive.");
        med.addStock(qty);
        medDAO.update(med);
        AuditLogger.log("ADD_STOCK", "Added " + qty + " units to " + med.getName()
            + (batchNote != null ? " | " + batchNote : ""));
        return Result.ok();
    }

    public List<Medicine> getLowStockMedicines() { return medDAO.findLowStock(); }
    public List<Medicine> getOutOfStock()         { return medDAO.findOutOfStock(); }
    public List<Medicine> getAllActive()           { return medDAO.findActive(); }
    public List<Medicine> search(String kw)       { return medDAO.search(kw); }

    // Supplier operations
    public Result<Supplier> addSupplier(Supplier s) {
        if (!s.isValid()) return Result.fail("Supplier name is required.");
        Supplier saved = supplierDAO.save(s);
        AuditLogger.log("ADD_SUPPLIER", "Added: " + saved.getName());
        return Result.ok(saved);
    }

    public Result<Supplier> updateSupplier(Supplier s) {
        supplierDAO.update(s);
        AuditLogger.log("EDIT_SUPPLIER", "Updated: " + s.getName());
        return Result.ok(s);
    }

    public Result<Void> deleteSupplier(int id) {
        Optional<Supplier> opt = supplierDAO.findById(id);
        if (opt.isEmpty()) return Result.fail("Supplier not found.");
        opt.get().deactivate();
        supplierDAO.update(opt.get());
        AuditLogger.log("DELETE_SUPPLIER", "Deactivated supplier ID=" + id);
        return Result.ok();
    }

    public List<Supplier> getAllSuppliers()      { return supplierDAO.findActive(); }
    public List<Supplier> searchSuppliers(String kw) { return supplierDAO.search(kw); }
}
