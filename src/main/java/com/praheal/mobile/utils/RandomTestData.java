package com.praheal.mobile.utils;

import java.security.SecureRandom;

public final class RandomTestData {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String UPPER = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijkmnpqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL = "@#$%&*!";
    private static final String UNREGISTERED_MOBILE_PREFIX = "90000";
    private static final int PASSWORD_LENGTH = 12;
    private static final int MOBILE_SUFFIX_LENGTH = 5;

    private RandomTestData() {
    }

    public static String randomPassword() {
        StringBuilder password = new StringBuilder()
                .append(randomChar(UPPER))
                .append(randomChar(LOWER))
                .append(randomChar(DIGITS))
                .append(randomChar(SPECIAL));
        String all = UPPER + LOWER + DIGITS + SPECIAL;
        while (password.length() < PASSWORD_LENGTH) {
            password.append(randomChar(all));
        }
        return password.toString();
    }

    public static String randomUnregisteredMobileNumber() {
        StringBuilder mobile = new StringBuilder(UNREGISTERED_MOBILE_PREFIX);
        while (mobile.length() < UNREGISTERED_MOBILE_PREFIX.length() + MOBILE_SUFFIX_LENGTH) {
            mobile.append(randomChar(DIGITS));
        }
        return mobile.toString();
    }

    private static char randomChar(String characters) {
        return characters.charAt(RANDOM.nextInt(characters.length()));
    }
}
