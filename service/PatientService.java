package com.pharmacy.service;

import com.pharmacy.dao.PatientDAO;
import com.pharmacy.generics.Result;
import com.pharmacy.model.Patient;
import com.pharmacy.util.AuditLogger;

import java.util.List;
import java.util.Optional;

/**
 * Patient management service.
 */
public class PatientService {

    private static PatientService instance;
    private final PatientDAO dao = PatientDAO.getInstance();

    private PatientService() {}

    public static synchronized PatientService getInstance() {
        if (instance == null) instance = new PatientService();
        return instance;
    }

    public Result<Patient> addPatient(Patient p) {
        if (!p.isValid()) return Result.fail("First name and last name are required.");
        Patient saved = dao.save(p);
        AuditLogger.log("ADD_PATIENT", "Registered: " + saved.getFullName());
        return Result.ok(saved);
    }

    public Result<Patient> updatePatient(Patient p) {
        dao.update(p);
        AuditLogger.log("EDIT_PATIENT", "Updated: " + p.getFullName());
        return Result.ok(p);
    }

    public Result<Void> deletePatient(int id) {
        dao.findById(id).ifPresent(p -> { p.deactivate(); dao.update(p); });
        AuditLogger.log("DELETE_PATIENT", "Deactivated patient ID=" + id);
        return Result.ok();
    }

    public List<Patient> getAllActive()     { return dao.findActive(); }
    public List<Patient> search(String kw)  { return dao.search(kw); }
    public Optional<Patient> findById(int id) { return dao.findById(id); }
}
