package com.pharmacy.dao;

import com.pharmacy.model.*;
import com.pharmacy.util.PasswordUtil;
import java.util.*;

/**
 * User DAO — handles authentication and user management.
 */
public class UserDAO extends FileRepository<User> implements SearchableRepository<User> {

    private static UserDAO instance;

    private UserDAO() {
        super("users");
        seedAdminIfEmpty();
    }

    public static synchronized UserDAO getInstance() {
        if (instance == null) instance = new UserDAO();
        return instance;
    }

    @Override protected Class<User[]> getArrayClass() { return User[].class; }

    public Optional<User> authenticate(String username, String password) {
        return store.values().stream()
            .filter(u -> u.isActive()
                      && u.getUsername().equalsIgnoreCase(username.trim())
                      && PasswordUtil.verify(password, u.getPasswordHash()))
            .findFirst();
    }

    public Optional<User> findByUsername(String username) {
        return store.values().stream()
            .filter(u -> u.getUsername().equalsIgnoreCase(username))
            .findFirst();
    }

    public User createUser(User user, String plainPassword) {
        user.setPasswordHash(PasswordUtil.hash(plainPassword));
        return save(user);
    }

    public void changePassword(int userId, String newPlain) {
        findById(userId).ifPresent(u -> {
            u.setPasswordHash(PasswordUtil.hash(newPlain));
            update(u);
        });
    }

    @Override
    public List<User> search(String keyword) { return filter(u -> u.matches(keyword)); }

    public List<User> findActive() { return filter(User::isActive); }

    private void seedAdminIfEmpty() {
        if (!store.isEmpty()) return;
        User admin = new User();
        admin.setUsername("admin");
        admin.setFullName("Administrator");
        admin.setRole(Role.ADMIN);
        admin.setEmail("admin@pharmacy.com");
        createUser(admin, "admin123");

        User pharma = new User();
        pharma.setUsername("pharmacist");
        pharma.setFullName("Dr. Sara Khan");
        pharma.setRole(Role.PHARMACIST);
        pharma.setEmail("sara@pharmacy.com");
        createUser(pharma, "pharma123");

        User cashier = new User();
        cashier.setUsername("cashier");
        cashier.setFullName("Ahmed Ali");
        cashier.setRole(Role.CASHIER);
        cashier.setEmail("ahmed@pharmacy.com");
        createUser(cashier, "cashier123");
    }
}
