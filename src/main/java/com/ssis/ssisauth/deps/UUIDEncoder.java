package com.ssis.ssisauth.deps;

import java.math.BigInteger;

public class UUIDEncoder {
    private static final int BASE = 36;

    /**
     * Encodes a UUID to a compact base36 string (7-8 characters)
     * @param uuid UUID in format: 380df991-f603-344c-a090-369bad2a924a
     * @return Encoded string using alphanumeric characters (0-9, A-Z)
     */
    public static String encode(String uuid) {
        // Remove hyphens and convert from hex to base36
        String hex = uuid.replace("-", "");
        BigInteger num = new BigInteger(hex, 16);
        return num.toString(BASE).toUpperCase();
    }
}

