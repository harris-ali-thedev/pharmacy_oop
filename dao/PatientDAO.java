package com.pharmacy.dao;

import com.pharmacy.model.Patient;
import java.util.List;

/**
 * Patient DAO.
 */
public class PatientDAO extends FileRepository<Patient> implements SearchableRepository<Patient> {

    private static PatientDAO instance;

    private PatientDAO() { super("patients"); }

    public static synchronized PatientDAO getInstance() {
        if (instance == null) instance = new PatientDAO();
        return instance;
    }

    @Override protected Class<Patient[]> getArrayClass() { return Patient[].class; }

    @Override
    public List<Patient> search(String keyword) {
        return filter(p -> p.isActive() && p.matches(keyword));
    }

    public List<Patient> findActive() { return filter(Patient::isActive); }
}
