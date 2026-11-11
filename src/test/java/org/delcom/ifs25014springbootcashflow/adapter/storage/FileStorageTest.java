package org.delcom.ifs25014springbootcashflow.adapter.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileStorageTest {

    @TempDir
    Path tempDir;

    @Test
    void storeResolveAndExtractExtension() throws Exception {
        FileStorage storage = new FileStorage(tempDir.toString());
        assertEquals(tempDir.toAbsolutePath().normalize(), storage.getBaseDir());

        byte[] data = "isi gambar".getBytes(StandardCharsets.UTF_8);
        String relPath = storage.store("proofs", "Foto.PNG", new ByteArrayInputStream(data));
        assertTrue(relPath.startsWith("proofs/"));
        assertTrue(relPath.endsWith(".png"));

        Path resolved = storage.resolve(relPath);
        assertTrue(Files.exists(resolved));
        assertEquals("isi gambar", Files.readString(resolved, StandardCharsets.UTF_8));

        assertEquals("", FileStorage.extractExtension(null));
        assertEquals("", FileStorage.extractExtension("tanpaekstensi"));
        assertEquals("", FileStorage.extractExtension("akhirtitik."));
        assertEquals(".jpg", FileStorage.extractExtension("a.b.JPG"));
    }

    @Test
    void constructorAndStoreHandleIoErrors() throws Exception {
        Path blockingFile = tempDir.resolve("bukan-folder");
        Files.writeString(blockingFile, "isi");

        assertThrows(UncheckedIOException.class,
                () -> new FileStorage(blockingFile.resolve("sub").toString()));

        FileStorage storage = new FileStorage(tempDir.resolve("valid").toString());
        InputStream failingStream = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("gagal baca");
            }
        };
        assertThrows(UncheckedIOException.class,
                () -> storage.store("proofs", "a.png", failingStream));
    }
}

