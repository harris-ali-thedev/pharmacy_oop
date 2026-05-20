package com.pharmacy.dao;

import com.pharmacy.model.Entity;
import com.pharmacy.util.FileStore;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Abstract generic file-backed repository.
 * Demonstrates: Generics, Abstraction, Template Method Pattern, File Handling
 *
 * All CRUD is done in-memory; the FileStore handles serialization to disk.
 *
 * @param <T> Entity type stored in this repository
 */
public abstract class FileRepository<T extends Entity> implements Repository<T, Integer> {

    /** In-memory store: id → entity */
    protected final Map<Integer, T> store = new LinkedHashMap<>();

    /** Auto-incrementing ID counter */
    private final AtomicInteger idSeq = new AtomicInteger(1);

    /** Logical file name (no path) used by FileStore */
    private final String fileName;

    protected FileRepository(String fileName) {
        this.fileName = fileName;
        load();
    }

    /** Subclasses must provide the array class token (used for typing) */
    protected abstract Class<T[]> getArrayClass();

    // ── Repository interface ──────────────────────────────────

    @Override
    public T save(T entity) {
        if (entity == null) throw new IllegalArgumentException("Entity cannot be null");
        if (!entity.isValid()) throw new IllegalArgumentException("Entity is not valid: " + entity.getDisplayName());
        int nextId = idSeq.getAndIncrement();
        entity.setId(nextId);
        store.put(nextId, entity);
        persist();
        return entity;
    }

    @Override
    public void update(T entity) {
        if (entity == null || entity.getId() <= 0)
            throw new IllegalArgumentException("Entity has no ID — save it first");
        if (!store.containsKey(entity.getId()))
            throw new NoSuchElementException("Entity not found: id=" + entity.getId());
        store.put(entity.getId(), entity);
        persist();
    }

    @Override
    public Optional<T> findById(Integer id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<T> findAll() {
        return Collections.unmodifiableList(new ArrayList<>(store.values()));
    }

    @Override
    public boolean deleteById(Integer id) {
        boolean removed = store.remove(id) != null;
        if (removed) persist();
        return removed;
    }

    @Override
    public int count() {
        return store.size();
    }

    // ── Helpers ───────────────────────────────────────────────

    /** Persist the in-memory store to disk */
    protected void persist() {
        FileStore.save(fileName, store.values());
    }

    /** Load all entries from disk into the in-memory store */
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void load() {
        List rawList = FileStore.load(fileName);
        if (rawList != null) {
            for (Object obj : rawList) {
                T entity = (T) obj;
                store.put(entity.getId(), entity);
                if (entity.getId() >= idSeq.get()) {
                    idSeq.set(entity.getId() + 1);
                }
            }
        }
    }

    /** Convenience: filter the store by a predicate */
    protected List<T> filter(Predicate<T> predicate) {
        return store.values().stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }
}
