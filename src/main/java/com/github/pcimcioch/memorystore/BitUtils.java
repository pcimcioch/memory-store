package com.github.pcimcioch.memorystore;

public final class BitUtils {

    private BitUtils() {
    }

    public static int countBits(long numberOfValues) {
        assertArgument(numberOfValues > 1, "Value must be greater than 1");

        int shift = 63;
        for(; (1L << shift & numberOfValues - 1) == 0; shift--);

        return shift + 1;
    }

    public static long buildLong(int big, int little) {
        return (((long) big) << 32) | (little & 0xffffffffL);
    }

    public static void assertArgument(boolean check, String messageFormat, Object... args) {
        if (!check) {
            throw new IllegalArgumentException(String.format(messageFormat, args));
        }
    }
}
