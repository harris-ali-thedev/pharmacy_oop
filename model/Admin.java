package com.pharmacy.model;

import java.util.ArrayList;
import java.util.List;

public class Admin extends User {
    private static final long serialVersionUID = 1L;

    private List<String> systemLogs = new ArrayList<>();

    public Admin() {
        super();
        setRole(Role.ADMIN);
    }

    public void addUser(User user) {
        systemLogs.add("Added user: " + (user != null ? user.getUsername() : "null"));
    }

    public void removeUser(String id) {
        systemLogs.add("Removed user ID: " + id);
    }

    public void generateReport() {
        systemLogs.add("Generated admin report");
    }

    public List<String> viewSystemLogs() {
        return new ArrayList<>(systemLogs);
    }

    @Override
    public String performOperation() {
        return "Admin manages users and system reports";
    }

    public List<String> getSystemLogs() { return systemLogs; }
    public void setSystemLogs(List<String> v) { this.systemLogs = v != null ? v : new ArrayList<>(); }
}
