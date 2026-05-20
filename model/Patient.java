package com.pharmacy.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Patient entity.
 * Demonstrates: Inheritance, Encapsulation, Collections
 */
public class Patient extends Entity implements Deactivatable, Searchable {

    private static final long serialVersionUID = 1L;

    private String       firstName;
    private String       lastName;
    private String       phone;
    private String       email;
    private String       nicNumber;
    private String       address;
    private List<String> allergies;
    private List<Prescription> prescriptionHistory;
    private int          loyaltyPoints;
    private boolean      active;

    public Patient() {
        super();
        this.active    = true;
        this.allergies = new ArrayList<>();
        this.prescriptionHistory = new ArrayList<>();
    }

    public Patient(int id, String firstName, String lastName, String phone) {
        super(id);
        this.firstName = firstName;
        this.lastName  = lastName;
        this.phone     = phone;
        this.active    = true;
        this.allergies = new ArrayList<>();
        this.prescriptionHistory = new ArrayList<>();
    }

    @Override
    public String getDisplayName() { return firstName + " " + lastName; }

    @Override
    public boolean isValid() {
        return firstName != null && !firstName.isBlank()
            && lastName  != null && !lastName.isBlank();
    }

    @Override
    public boolean matches(String keyword) {
        if (keyword == null || keyword.isBlank()) return true;
        String kw = keyword.toLowerCase();
        return (firstName  != null && firstName.toLowerCase().contains(kw))
            || (lastName   != null && lastName.toLowerCase().contains(kw))
            || (phone      != null && phone.toLowerCase().contains(kw))
            || (nicNumber  != null && nicNumber.toLowerCase().contains(kw));
    }

    @Override public boolean isActive()        { return active; }
    @Override public void setActive(boolean v) { this.active = v; }

    public boolean hasAllergy(String substance) {
        if (substance == null) return false;
        return allergies.stream().anyMatch(a -> a.equalsIgnoreCase(substance.trim()));
    }

    public void addAllergy(String allergen) {
        if (allergen != null && !allergen.isBlank() && !hasAllergy(allergen)) {
            allergies.add(allergen.trim());
        }
    }

    public void addPrescription(Prescription prescription) {
        if (prescription != null) getPrescriptionHistory().add(prescription);
    }

    public void addLoyaltyPoints(int pts) {
        if (pts > 0) loyaltyPoints += pts;
    }

    public String getFullName() { return firstName + " " + lastName; }

    public String  getFirstName()                { return firstName; }
    public void    setFirstName(String v)        { this.firstName = v; }
    public String  getLastName()                 { return lastName; }
    public void    setLastName(String v)         { this.lastName = v; }
    public String  getPhone()                    { return phone; }
    public void    setPhone(String v)            { this.phone = v; }
    public String  getEmail()                    { return email; }
    public void    setEmail(String v)            { this.email = v; }
    public String  getNicNumber()                { return nicNumber; }
    public void    setNicNumber(String v)        { this.nicNumber = v; }
    public String  getAddress()                  { return address; }
    public void    setAddress(String v)          { this.address = v; }
    public List<String> getAllergies()            { return allergies; }
    public void    setAllergies(List<String> v)  { this.allergies = v != null ? v : new ArrayList<>(); }
    public List<Prescription> getPrescriptionHistory() {
        if (prescriptionHistory == null) prescriptionHistory = new ArrayList<>();
        return prescriptionHistory;
    }
    public void setPrescriptionHistory(List<Prescription> v) { this.prescriptionHistory = v != null ? v : new ArrayList<>(); }
    public int     getLoyaltyPoints()            { return loyaltyPoints; }
    public void    setLoyaltyPoints(int v)       { this.loyaltyPoints = v; }

    @Override
    public String toString() { return getFullName() + " (" + phone + ")"; }
}
