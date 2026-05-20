package com.pharmacy.model;

/**
 * Enum for user roles.
 * Demonstrates: Enum, Encapsulation
 */
public enum Role {
    ADMIN("Administrator", true, true, true),
    PHARMACIST("Pharmacist", true, true, false),
    CASHIER("Cashier", false, false, false);

    private final String label;
    private final boolean canManageInventory;
    private final boolean canVerifyPrescriptions;
    private final boolean canManageUsers;

    Role(String label, boolean canManageInventory, boolean canVerifyPrescriptions, boolean canManageUsers) {
        this.label = label;
        this.canManageInventory = canManageInventory;
        this.canVerifyPrescriptions = canVerifyPrescriptions;
        this.canManageUsers = canManageUsers;
    }

    public String getLabel()                  { return label; }
    public boolean canManageInventory()       { return canManageInventory; }
    public boolean canVerifyPrescriptions()   { return canVerifyPrescriptions; }
    public boolean canManageUsers()           { return canManageUsers; }

    @Override
    public String toString() { return label; }
}
