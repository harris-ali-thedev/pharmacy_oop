package com.pharmacy.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Simple password hashing utility using SHA-256.
 * (Production systems should use BCrypt — SHA-256 is used here
 *  to avoid external dependencies in a coursework project.)
 *
 * Demonstrates: Encapsulation, Static utility methods
 */
public final class PasswordUtil {

    private static final String SALT = "PharmacyPMS_OOP_2024";

    private PasswordUtil() {}

    /**
     * Hash a plain-text password.
     * @param plain the raw password
     * @return Base64-encoded SHA-256 hash
     */
    public static String hash(String plain) {
        if (plain == null) throw new IllegalArgumentException("Password cannot be null");
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update((SALT + plain).getBytes());
            return Base64.getEncoder().encodeToString(md.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    /**
     * Verify a plain-text password against a stored hash.
     * @param plain      the raw password to check
     * @param storedHash the hash from the data store
     * @return true if they match
     */
    public static boolean verify(String plain, String storedHash) {
        if (plain == null || storedHash == null) return false;
        return hash(plain).equals(storedHash);
    }
}
