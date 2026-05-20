package com.pharmacy.dao;

import com.pharmacy.model.Supplier;
import java.util.List;

/**
 * Supplier DAO.
 * Demonstrates: Generics, Singleton, File-based persistence
 */
public class SupplierDAO extends FileRepository<Supplier> implements SearchableRepository<Supplier> {

    private static SupplierDAO instance;

    private SupplierDAO() {
        super("suppliers");
        seedIfEmpty();
    }

    public static synchronized SupplierDAO getInstance() {
        if (instance == null) instance = new SupplierDAO();
        return instance;
    }

    @Override protected Class<Supplier[]> getArrayClass() { return Supplier[].class; }

    @Override
    public List<Supplier> search(String keyword) {
        return filter(s -> s.isActive() && s.matches(keyword));
    }

    public List<Supplier> findActive() { return filter(Supplier::isActive); }

    private void seedIfEmpty() {
        if (!store.isEmpty()) return;
        String[][] data = {
            {"MediCorp", "Ali Hassan", "0300-1234567", "ali@medicorp.pk",  "Karachi",   "7"},
            {"PharmEx",  "Sara Khan",  "0321-9876543", "sara@pharmex.pk",  "Lahore",    "5"},
            {"Healix",   "Umar Riaz",  "0333-5551234", "umar@healix.pk",   "Islamabad", "10"},
        };
        for (String[] row : data) {
            Supplier s = new Supplier();
            s.setName(row[0]);
            s.setContactName(row[1]);
            s.setPhone(row[2]);
            s.setEmail(row[3]);
            s.setAddress(row[4]);
            s.setLeadDays(Integer.parseInt(row[5]));
            save(s);
        }
    }
}
