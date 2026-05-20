package com.pharmacy.util;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

public final class FileHandler {
    private FileHandler() {}

    public static <T extends Serializable> void saveObjects(String file, List<T> data) {
        FileStore.save(stripExtension(file), data);
    }

    public static <T extends Serializable> List<T> loadObjects(String filePath) {
        return FileStore.load(stripExtension(filePath));
    }

    public static boolean fileExists(String filePath) {
        return java.nio.file.Files.exists(java.nio.file.Paths.get("data", stripExtension(filePath) + ".dat"));
    }

    public static void initDataFiles() {
        FileStore.save("users", FileStore.load("users"));
        FileStore.save("medicines", FileStore.load("medicines"));
        FileStore.save("patients", FileStore.load("patients"));
        FileStore.save("suppliers", FileStore.load("suppliers"));
        FileStore.save("sales", FileStore.load("sales"));
    }

    private static String stripExtension(String file) {
        if (file == null || file.isBlank()) return "objects";
        String normalized = file.replace("\\", "/");
        int slash = normalized.lastIndexOf('/');
        if (slash >= 0) normalized = normalized.substring(slash + 1);
        if (normalized.endsWith(".dat")) normalized = normalized.substring(0, normalized.length() - 4);
        return normalized;
    }
}
