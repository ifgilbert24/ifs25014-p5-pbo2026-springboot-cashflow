package org.delcom.ifs25014springbootcashflow;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Memuat variabel dari file {@code .env} di root proyek ke System properties
 * sebelum Spring Boot membaca {@code application.yaml}. Nilai yang sudah ada di
 * environment OS atau System property tidak ditimpa.
 */
public final class DotEnv {

    private DotEnv() {
    }

    public static void load() {
        load(Path.of(".env"));
    }

    public static void load(Path path) {
        if (!Files.exists(path)) {
            return;
        }
        List<String> lines;
        try {
            lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Gagal membaca file .env", e);
        }
        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            int eq = line.indexOf('=');
            if (eq <= 0) {
                continue;
            }
            String key = line.substring(0, eq).trim();
            String value = stripQuotes(line.substring(eq + 1).trim());
            if (System.getenv(key) == null && System.getProperty(key) == null) {
                System.setProperty(key, value);
            }
        }
    }

    static String stripQuotes(String value) {
        if (value.length() >= 2) {
            char first = value.charAt(0);
            char last = value.charAt(value.length() - 1);
            if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                return value.substring(1, value.length() - 1);
            }
        }
        return value;
    }
}

