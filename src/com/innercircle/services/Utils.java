package com.innercircle.services;

import java.util.Random;

import org.apache.commons.codec.binary.Base64;

public class Utils {
    private Utils(){}

    public static String tokenGeneratorByUID(final String uid) {
        final Random generator = new Random();
        final long randomValue = generator.nextLong() + System.currentTimeMillis();
        final String keySource = uid + String.valueOf(randomValue);

        new Base64(true);
        final byte [] tokenByte = Base64.encodeBase64(keySource.getBytes());

        final String token = new String(tokenByte);
        return token;
    }
}
