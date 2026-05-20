package com.pharmacy.model;

/**
 * Interface for entities that can be searched by keyword.
 * Demonstrates: Interface, Polymorphism
 */
public interface Searchable {
    /**
     * Returns true if this entity matches the given search keyword.
     * Case-insensitive matching expected.
     */
    boolean matches(String keyword);
}
