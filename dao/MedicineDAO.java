package com.pharmacy.dao;

import com.pharmacy.model.Medicine;
import java.util.*;

public class MedicineDAO extends FileRepository<Medicine> implements SearchableRepository<Medicine> {

    private static MedicineDAO instance;

    private MedicineDAO() {
        super("medicines");
        seedSampleIfEmpty();
    }

    public static synchronized MedicineDAO getInstance() {
        if (instance == null) instance = new MedicineDAO();
        return instance;
    }

    @Override protected Class<Medicine[]> getArrayClass() { return Medicine[].class; }

    @Override
    public List<Medicine> search(String keyword) {
        return filter(m -> m.isActive() && m.matches(keyword));
    }

    public List<Medicine> findActive()      { return filter(Medicine::isActive); }
    public List<Medicine> findLowStock()    { return filter(m -> m.isActive() && m.isLowStock()); }
    public List<Medicine> findOutOfStock()  { return filter(m -> m.isActive() && m.isOutOfStock()); }

    public Optional<Medicine> findByBarcode(String barcode) {
        if (barcode == null) return Optional.empty();
        return store.values().stream()
            .filter(m -> barcode.equals(m.getBarcode()))
            .findFirst();
    }

    private void seedSampleIfEmpty() {
        if (!store.isEmpty()) return;
        Object[][] data = {
            {"Paracetamol",  "Analgesic",     "Acetaminophen", 100.0, 150, 50,  false, false},
            {"Amoxicillin",  "Antibiotic",    "Amoxicillin",   200.0, 120, 30,  false, true},
            {"Ibuprofen",    "NSAID",         "Ibuprofen",      80.0, 200, 25,  false, false},
            {"Omeprazole",   "Antacid",       "Omeprazole",    150.0,  80, 20,  false, false},
            {"Metformin",    "Antidiabetic",  "Metformin",      60.0,  90, 15,  false, false},
            {"Atorvastatin", "Statin",        "Atorvastatin",  300.0,  60, 20,  false, false},
            {"Cetirizine",   "Antihistamine", "Cetirizine",     90.0, 180, 30,  false, false},
            {"Aspirin",      "Analgesic",     "Aspirin",        50.0, 300, 40,  false, false},
            {"Diazepam",     "Sedative",      "Diazepam",      500.0,  40, 10,  true,  true},
            {"Pantoprazole", "Antacid",       "Pantoprazole",  180.0,  70, 20,  false, false},
        };
        for (Object[] row : data) {
            Medicine m = new Medicine();
            m.setName((String) row[0]);
            m.setCategory((String) row[1]);
            m.setGenericName((String) row[2]);
            m.setUnitPrice((double) row[3]);
            m.setUnitCost((double) row[3] * 0.6);
            m.setStockQty((int) row[4]);
            m.setReorderLevel((int) row[5]);
            m.setNarcotic((boolean) row[6]);
            m.setScheduled((boolean) row[7]);
            save(m);
        }
    }
}
