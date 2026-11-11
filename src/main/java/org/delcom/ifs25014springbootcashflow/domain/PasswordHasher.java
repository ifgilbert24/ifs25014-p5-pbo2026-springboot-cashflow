package org.delcom.ifs25014springbootcashflow.domain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class PasswordHasher {

    private PasswordHasher() {
    }

    public static String hash(String rawPassword) {
        byte[] digest = messageDigest().digest(rawPassword.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(digest);
    }

    public static boolean matches(String rawPassword, String expectedHash) {
        return hash(rawPassword).equals(expectedHash);
    }

    static MessageDigest messageDigest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 tidak tersedia", e);
        }
    }
}

