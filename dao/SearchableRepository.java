package com.pharmacy.dao;

import com.pharmacy.model.Entity;
import com.pharmacy.model.Searchable;
import java.util.List;

/**
 * Extended repository interface adding keyword search.
 * Demonstrates: Interface Inheritance, Generics with multiple bounds
 *
 * @param <T> Entity type that is both an Entity and Searchable
 */
public interface SearchableRepository<T extends Entity & Searchable>
        extends Repository<T, Integer> {

    /**
     * Search entities by keyword.
     * @param keyword search term (matched against entity's matches() method)
     * @return list of matching entities
     */
    List<T> search(String keyword);
}
