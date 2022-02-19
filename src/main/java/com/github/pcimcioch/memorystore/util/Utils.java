package com.github.pcimcioch.memorystore.util;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class Utils {

    private Utils() {
    }

    public static int countBits(long numberOfValues) {
        assertArgument(numberOfValues > 1, "Value must be greater than 1");

        int shift = 63;
        for (; (1L << shift & numberOfValues - 1) == 0; shift--) ;

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

    public static <K, V, NK, NV> Map<NK, NV> remap(Map<K, V> map, Function<K, NK> keyMapping,
                                                   Function<V, NV> valueMapping) {
        return map.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> keyMapping.apply(e.getKey()),
                        e -> valueMapping.apply(e.getValue())
                ));
    }
}
