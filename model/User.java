package com.pharmacy.model;

/**
 * User entity.
 * Demonstrates: Inheritance (extends Entity), Encapsulation, Interface implementation
 */
public class User extends Entity implements Deactivatable, Searchable {

    private static final long serialVersionUID = 1L;

    private String username;
    private String passwordHash;   // BCrypt-style simple hash for the demo
    private String fullName;
    private Role   role;
    private String email;
    private String phone;
    private boolean active;
    private int failedLoginCount;
    private boolean mustChangePassword;

    public User() {
        super();
        this.active = true;
        this.role   = Role.CASHIER;
    }

    public User(int id, String username, String fullName, Role role) {
        super(id);
        this.username = username;
        this.fullName = fullName;
        this.role     = role;
        this.active   = true;
    }

    // ── Abstract implementations ────────────────────────────
    @Override
    public String getDisplayName() {
        return fullName + " (" + role.getLabel() + ")";
    }

    public String getDetails() {
        return String.format("%s | %s | %s", fullName, role != null ? role.getLabel() : "No role", email);
    }

    public String getRoleName() {
        return role != null ? role.getLabel() : "";
    }

    public String performOperation() {
        return getRoleName() + " operation executed";
    }

    @Override
    public boolean isValid() {
        return username != null && !username.isBlank()
            && fullName  != null && !fullName.isBlank()
            && role      != null;
    }

    // ── Searchable ──────────────────────────────────────────
    @Override
    public boolean matches(String keyword) {
        if (keyword == null || keyword.isBlank()) return true;
        String kw = keyword.toLowerCase();
        return (username != null && username.toLowerCase().contains(kw))
            || (fullName  != null && fullName.toLowerCase().contains(kw))
            || (email     != null && email.toLowerCase().contains(kw))
            || role.getLabel().toLowerCase().contains(kw);
    }

    // ── Deactivatable ───────────────────────────────────────
    @Override public boolean isActive()          { return active; }
    @Override public void setActive(boolean v)   { this.active = v; }

    // ── Business logic ──────────────────────────────────────
    public boolean isAdmin()      { return role == Role.ADMIN; }
    public boolean isPharmacist() { return role == Role.PHARMACIST; }
    public boolean isCashier()    { return role == Role.CASHIER; }

    public void recordFailedLogin() {
        failedLoginCount++;
    }

    public boolean isLocked() {
        return failedLoginCount >= 5;
    }

    public void resetFailedLogins() {
        failedLoginCount = 0;
    }

    // ── Getters / Setters ───────────────────────────────────
    public String  getUsername()                    { return username; }
    public void    setUsername(String v)            { this.username = v; }
    public String  getPasswordHash()                { return passwordHash; }
    public void    setPasswordHash(String v)        { this.passwordHash = v; }
    public String  getFullName()                    { return fullName; }
    public void    setFullName(String v)            { this.fullName = v; }
    public Role    getRole()                        { return role; }
    public void    setRole(Role v)                  { this.role = v; }
    public String  getEmail()                       { return email; }
    public void    setEmail(String v)               { this.email = v; }
    public String  getPhone()                       { return phone; }
    public void    setPhone(String v)               { this.phone = v; }
    public int     getFailedLoginCount()            { return failedLoginCount; }
    public void    setFailedLoginCount(int v)       { this.failedLoginCount = v; }
    public boolean isMustChangePassword()           { return mustChangePassword; }
    public void    setMustChangePassword(boolean v) { this.mustChangePassword = v; }

    @Override
    public String toString() { return fullName + " [" + role.getLabel() + "]"; }
}
