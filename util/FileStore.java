package com.pharmacy.util;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * File-handling utility that serializes / deserializes collections to disk.
 * Uses Java Object Serialization (binary .dat files).
 *
 * Demonstrates: File I/O, Exception Handling, Generics
 */
public final class FileStore {

    private static final Logger LOG = Logger.getLogger(FileStore.class.getName());
    private static final String DATA_DIR = "data";

    // Prevent instantiation — utility class
    private FileStore() {}

    static {
        try {
            Files.createDirectories(Paths.get(DATA_DIR));
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Cannot create data directory", e);
        }
    }

    /**
     * Persist a collection to a binary file.
     *
     * @param fileName logical name (e.g. "users")
     * @param data     the collection to persist
     */
    public static <T extends Serializable> void save(String fileName, Collection<T> data) {
        Path path = Paths.get(DATA_DIR, fileName + ".dat");
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new BufferedOutputStream(new FileOutputStream(path.toFile())))) {
            oos.writeObject(new ArrayList<>(data));
            LOG.fine("Saved " + data.size() + " records to " + path);
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Could not save " + fileName, e);
        }
    }

    /**
     * Load a collection from a binary file.
     *
     * @param fileName logical name (e.g. "users")
     * @return list of objects, or an empty list if the file doesn't exist
     */
    @SuppressWarnings("unchecked")
    public static <T extends Serializable> List<T> load(String fileName) {
        Path path = Paths.get(DATA_DIR, fileName + ".dat");
        if (!Files.exists(path)) return new ArrayList<>();
        try (ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(path.toFile())))) {
            Object obj = ois.readObject();
            if (obj instanceof List<?>) {
                return (List<T>) obj;
            }
        } catch (IOException | ClassNotFoundException e) {
            LOG.log(Level.WARNING, "Could not load " + fileName, e);
        }
        return new ArrayList<>();
    }

    /**
     * Append a plain-text line to a log file (used by AuditLogger).
     */
    public static void appendLine(String fileName, String line) {
        Path path = Paths.get(DATA_DIR, fileName + ".log");
        try (PrintWriter pw = new PrintWriter(new FileWriter(path.toFile(), true))) {
            pw.println(line);
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Could not append to " + fileName, e);
        }
    }

    /**
     * Read all lines from a text log file.
     */
    public static List<String> readLines(String fileName) {
        Path path = Paths.get(DATA_DIR, fileName + ".log");
        if (!Files.exists(path)) return new ArrayList<>();
        try {
            return Files.readAllLines(path);
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Could not read " + fileName, e);
            return new ArrayList<>();
        }
    }

    /** Delete all data files (useful for testing). */
    public static void clearAll() {
        try {
            Files.list(Paths.get(DATA_DIR))
                 .filter(p -> p.toString().endsWith(".dat"))
                 .forEach(p -> {
                     try { Files.deleteIfExists(p); }
                     catch (IOException ignored) {}
                 });
        } catch (IOException e) {
            LOG.log(Level.WARNING, "clearAll failed", e);
        }
    }
}
