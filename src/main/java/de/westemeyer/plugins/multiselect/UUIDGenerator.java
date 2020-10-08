package de.westemeyer.plugins.multiselect;

import java.security.SecureRandom;

/**
 * The random number generator used by this class to create random
 * based UUIDs. In a holder class to defer initialization until needed.
 */
class UUIDGenerator {
    /** Limited alphabet for UUIDs, only valid JavaScript identifier characters. */
    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABZDEFGHIJKLMNOPQRSTUVWXYZ";

    /** Random number generator. */
    static final SecureRandom numberGenerator = new SecureRandom();

    /**
     * Static method to generate a new UUID.
     * @return new UUID
     */
    static String generateUUID(int length) {
        // string builder to receive characters
        StringBuilder uuid = new StringBuilder();

        // concatenate 30 random characters
        for (int i = 0; i < length; ++i) {
            uuid.append(ALPHABET.charAt(numberGenerator.nextInt(ALPHABET.length() - 1)));
        }

        // return new string from builder object
        return uuid.toString();
    }
}
