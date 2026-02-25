package com.classpets.backend.auth.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

public final class PasswordUtil {

    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;

    private PasswordUtil() {
    }

    public static String hash(String plain) {
        try {
            byte[] salt = new byte[16];
            SecureRandom.getInstanceStrong().nextBytes(salt);
            byte[] digest = pbkdf2(plain.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
            return Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(digest);
        } catch (Exception ex) {
            throw new IllegalStateException("Password hash failed", ex);
        }
    }

    public static boolean verify(String plain, String stored) {
        try {
            if (stored == null || !stored.contains(":")) {
                return false;
            }
            String[] parts = stored.split(":", 2);
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] expected = Base64.getDecoder().decode(parts[1]);
            byte[] actual = pbkdf2(plain.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
            if (actual.length != expected.length) {
                return false;
            }
            int result = 0;
            for (int i = 0; i < actual.length; i++) {
                result |= actual[i] ^ expected[i];
            }
            return result == 0;
        } catch (Exception ex) {
            return false;
        }
    }

    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int bits) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, bits);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        return skf.generateSecret(spec).getEncoded();
    }
}
