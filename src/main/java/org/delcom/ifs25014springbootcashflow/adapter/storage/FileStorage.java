package org.delcom.ifs25014springbootcashflow.adapter.storage;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public class FileStorage {

    private final Path baseDir;

    public FileStorage(String baseDirPath) {
        this.baseDir = Path.of(baseDirPath).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.baseDir);
        } catch (IOException e) {
            throw new UncheckedIOException("Gagal membuat direktori upload", e);
        }
    }

    public String store(String subDir, String originalFileName, InputStream content) {
        String extension = extractExtension(originalFileName);
        String fileName = UUID.randomUUID() + extension;
        Path dir = baseDir.resolve(subDir).normalize();
        try {
            Files.createDirectories(dir);
            Path target = dir.resolve(fileName);
            Files.copy(content, target, StandardCopyOption.REPLACE_EXISTING);
            return subDir + "/" + fileName;
        } catch (IOException e) {
            throw new UncheckedIOException("Gagal menyimpan file upload", e);
        }
    }

    public Path resolve(String relativePath) {
        return baseDir.resolve(relativePath).normalize();
    }

    public Path getBaseDir() {
        return baseDir;
    }

    static String extractExtension(String fileName) {
        if (fileName == null) {
            return "";
        }
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dot).toLowerCase();
    }
}

