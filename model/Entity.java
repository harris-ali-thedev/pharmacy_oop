package com.pharmacy.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/*
 Abstract base class for all domain entities.
 Demonstrates: Abstraction, Encapsulation, Inheritance
 */
public abstract class Entity implements Serializable, Comparable<Entity> {
    private static final long serialVersionUID = 1L;

    private int id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Entity() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Entity(int id) {
        this();
        this.id = id;
    }

    // Abstract methods — every entity must implement these
    public abstract String getDisplayName();
    public abstract boolean isValid();

    // Template method pattern — defines algorithm skeleton
    public final String getSummary() {
        return "[" + getClass().getSimpleName() + " #" + id + "] " + getDisplayName();
    }

    // Default compareTo by ID
    @Override
    public int compareTo(Entity other) {
        return Integer.compare(this.id, other.id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Entity)) return false;
        Entity other = (Entity) obj;
        return this.id == other.id && this.getClass() == other.getClass();
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; this.updatedAt = LocalDateTime.now(); }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
