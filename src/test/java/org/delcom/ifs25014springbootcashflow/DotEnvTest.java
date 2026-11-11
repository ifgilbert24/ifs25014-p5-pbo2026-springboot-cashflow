package org.delcom.ifs25014springbootcashflow;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DotEnvTest {

    @TempDir
    Path tempDir;

    @Test
    void loadParsesKeyValuesAndSkipsCommentsAndExisting() throws Exception {
        String existingKey = "DOTENV_EXISTING_" + System.nanoTime();
        String newKey = "DOTENV_NEW_" + System.nanoTime();
        String dquoteKey = "DOTENV_DQ_" + System.nanoTime();
        String squoteKey = "DOTENV_SQ_" + System.nanoTime();
        String mismatchKey = "DOTENV_MM_" + System.nanoTime();
        String shortKey = "DOTENV_SH_" + System.nanoTime();

        System.setProperty(existingKey, "sudah-ada");
        try {
            Path envFile = tempDir.resolve(".env");
            Files.writeString(envFile, String.join("\n",
                    "# komentar",
                    "   ",
                    "tanpasamadengan",
                    "=tanpakunci",
                    existingKey + "=ditimpa",
                    newKey + "=nilai_baru",
                    dquoteKey + "=\"dalam kutip ganda\"",
                    squoteKey + "='dalam kutip tunggal'",
                    mismatchKey + "=\"tidak cocok'",
                    shortKey + "=x",
                    "PATH=tidak_ditimpa"
            ));

            DotEnv.load(envFile);

            assertEquals("sudah-ada", System.getProperty(existingKey));
            assertEquals("nilai_baru", System.getProperty(newKey));
            assertEquals("dalam kutip ganda", System.getProperty(dquoteKey));
            assertEquals("dalam kutip tunggal", System.getProperty(squoteKey));
            assertEquals("\"tidak cocok'", System.getProperty(mismatchKey));
            assertEquals("x", System.getProperty(shortKey));
        } finally {
            System.clearProperty(existingKey);
            System.clearProperty(newKey);
            System.clearProperty(dquoteKey);
            System.clearProperty(squoteKey);
            System.clearProperty(mismatchKey);
            System.clearProperty(shortKey);
        }
    }

    @Test
    void loadIgnoresMissingFileAndDefaultOverloadWorks() {
        DotEnv.load(tempDir.resolve("tidak-ada.env"));
        DotEnv.load();
        assertNull(System.getProperty("TIDAK_PERNAH_DISET_XYZ"));
    }

    @Test
    void loadThrowsWhenPathIsDirectory() {
        assertThrows(UncheckedIOException.class, () -> DotEnv.load(tempDir));
    }

    @Test
    void stripQuotesEdgeCases() {
        assertEquals("a\"b", DotEnv.stripQuotes("a\"b"));
        assertEquals("'tidak ditutup", DotEnv.stripQuotes("'tidak ditutup"));
    }
}

