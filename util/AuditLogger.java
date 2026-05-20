package com.pharmacy.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Audit logger — appends entries to a flat text log file.
 * Demonstrates: File I/O (via FileStore), Static utility
 */
public final class AuditLogger {

    private static final String FILE = "audit";
    private static final DateTimeFormatter FMT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private AuditLogger() {}

    public static void log(String action, String description) {
        String line = String.format("[%s] %s | %s",
            LocalDateTime.now().format(FMT), action, description);
        FileStore.appendLine(FILE, line);
    }

    public static java.util.List<String> readAll() {
        return FileStore.readLines(FILE);
    }
}
