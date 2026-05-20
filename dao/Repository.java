package com.pharmacy.dao;

import com.pharmacy.model.Entity;
import java.util.List;
import java.util.Optional;

/**
 * Generic Repository interface.
 * Demonstrates: Generics, Interface, Bounded Type Parameters
 *
 * @param <T>  Entity type (must extend Entity)
 * @param <ID> Primary key type
 */
public interface Repository<T extends Entity, ID> {

    /**
     * Save a new entity (assign an ID and persist).
     * @param entity the entity to save
     * @return the saved entity with its assigned ID
     */
    T save(T entity);

    /**
     * Update an existing entity.
     * @param entity the entity with updated fields
     */
    void update(T entity);

    /**
     * Find entity by primary key.
     * @param id the primary key
     * @return Optional containing the entity, or empty if not found
     */
    Optional<T> findById(ID id);

    /**
     * Return all entities.
     * @return unmodifiable list of all entities
     */
    List<T> findAll();

    /**
     * Delete (hard) an entity by ID.
     * @param id the primary key
     * @return true if something was deleted
     */
    boolean deleteById(ID id);

    /**
     * Total count of persisted entities.
     */
    int count();
}
