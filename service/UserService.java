package com.pharmacy.service;

import com.pharmacy.dao.UserDAO;
import com.pharmacy.generics.Result;
import com.pharmacy.model.User;
import com.pharmacy.util.AuditLogger;
import com.pharmacy.util.SessionManager;

import java.util.List;
import java.util.Optional;

/**
 * User management service.
 * Demonstrates: Service layer, Generics (Result<T>), Singleton
 */
public class UserService {

    private static UserService instance;
    private final UserDAO dao = UserDAO.getInstance();

    private UserService() {}

    public static synchronized UserService getInstance() {
        if (instance == null) instance = new UserService();
        return instance;
    }

    public Result<User> login(String username, String password) {
        Optional<User> opt = dao.authenticate(username, password);
        if (opt.isEmpty()) return Result.fail("Invalid username or password.");
        User user = opt.get();
        if (user.isLocked()) return Result.fail("Account locked. Contact administrator.");
        user.resetFailedLogins();
        dao.update(user);
        SessionManager.getInstance().login(user);
        return Result.ok(user);
    }

    public void logout() {
        SessionManager.getInstance().logout();
    }

    public Result<User> createUser(User user, String password) {
        SessionManager.getInstance().requireAdmin();
        if (password == null || password.length() < 6)
            return Result.fail("Password must be at least 6 characters.");
        if (dao.findByUsername(user.getUsername()).isPresent())
            return Result.fail("Username '" + user.getUsername() + "' already exists.");
        User saved = dao.createUser(user, password);
        AuditLogger.log("ADD_USER", "Created user: " + saved.getUsername() + " [" + saved.getRole() + "]");
        return Result.ok(saved);
    }

    public Result<User> updateUser(User user) {
        SessionManager.getInstance().requireAdmin();
        dao.update(user);
        AuditLogger.log("EDIT_USER", "Updated user: " + user.getUsername());
        return Result.ok(user);
    }

    public Result<Void> changePassword(int userId, String newPassword) {
        if (newPassword == null || newPassword.length() < 6)
            return Result.fail("Password must be at least 6 characters.");
        dao.changePassword(userId, newPassword);
        AuditLogger.log("CHANGE_PASSWORD", "Password changed for user ID=" + userId);
        return Result.ok();
    }

    public Result<Void> deactivateUser(int userId) {
        SessionManager.getInstance().requireAdmin();
        User current = SessionManager.getInstance().getCurrentUser();
        if (current != null && current.getId() == userId)
            return Result.fail("You cannot deactivate your own account.");
        dao.findById(userId).ifPresent(u -> { u.deactivate(); dao.update(u); });
        AuditLogger.log("DEACTIVATE_USER", "Deactivated user ID=" + userId);
        return Result.ok();
    }

    public List<User>      getAllUsers()          { return dao.findAll(); }
    public List<User>      getActiveUsers()       { return dao.findActive(); }
    public List<User>      searchUsers(String kw) { return dao.search(kw); }
    public Optional<User>  findById(int id)       { return dao.findById(id); }
}
