package com.pharmacy.model;

/**
 * Marker interface for entities that can be soft-deleted.
 * Demonstrates: Interface Segregation, Polymorphism
 */
public interface Deactivatable {
    boolean isActive();
    void setActive(boolean active);
    default void deactivate() { setActive(false); }
    default void activate()   { setActive(true);  }
}
