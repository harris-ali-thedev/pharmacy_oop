package com.pharmacy.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Supplier entity.
 * Demonstrates: Inheritance, Encapsulation, Interface implementation
 */
public class Supplier extends Entity implements Deactivatable, Searchable {

    private static final long serialVersionUID = 1L;

    private String  name;
    private String  contactName;
    private String  phone;
    private String  contactNumber;
    private String  email;
    private String  address;
    private List<String> suppliedCategories;
    private int     leadDays;
    private boolean active;

    public Supplier() {
        super();
        this.active   = true;
        this.leadDays = 7;
        this.suppliedCategories = new ArrayList<>();
    }

    public Supplier(int id, String name, String contactName, String phone) {
        super(id);
        this.name        = name;
        this.contactName = contactName;
        this.phone       = phone;
        this.active      = true;
        this.leadDays    = 7;
        this.suppliedCategories = new ArrayList<>();
    }

    @Override
    public String getDisplayName() { return name + " (" + contactName + ")"; }

    @Override
    public boolean isValid() {
        return name != null && !name.isBlank();
    }

    @Override
    public boolean matches(String keyword) {
        if (keyword == null || keyword.isBlank()) return true;
        String kw = keyword.toLowerCase();
        return (name        != null && name.toLowerCase().contains(kw))
            || (contactName != null && contactName.toLowerCase().contains(kw))
            || (phone       != null && phone.toLowerCase().contains(kw))
            || (email       != null && email.toLowerCase().contains(kw));
    }

    @Override public boolean isActive()        { return active; }
    @Override public void setActive(boolean v) { this.active = v; }

    public String getSupplierID() { return String.valueOf(getId()); }

    public void addCategory(String category) {
        if (category != null && !category.isBlank() && !getSuppliedCategories().contains(category.trim())) {
            getSuppliedCategories().add(category.trim());
        }
    }

    public String  getName()                 { return name; }
    public void    setName(String v)         { this.name = v; }
    public String  getContactName()          { return contactName; }
    public void    setContactName(String v)  { this.contactName = v; }
    public String  getPhone()                { return phone; }
    public void    setPhone(String v)        { this.phone = v; this.contactNumber = v; }
    public String  getContactNumber()        { return contactNumber != null ? contactNumber : phone; }
    public void    setContactNumber(String v){ this.contactNumber = v; this.phone = v; }
    public String  getEmail()                { return email; }
    public void    setEmail(String v)        { this.email = v; }
    public String  getAddress()              { return address; }
    public void    setAddress(String v)      { this.address = v; }
    public int     getLeadDays()             { return leadDays; }
    public void    setLeadDays(int v)        { this.leadDays = v; }
    public List<String> getSuppliedCategories() {
        if (suppliedCategories == null) suppliedCategories = new ArrayList<>();
        return suppliedCategories;
    }
    public void setSuppliedCategories(List<String> v) { this.suppliedCategories = v != null ? v : new ArrayList<>(); }

    @Override
    public String toString() { return name; }
}
