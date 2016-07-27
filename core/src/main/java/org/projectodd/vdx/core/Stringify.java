package org.projectodd.vdx.core;

import java.util.ArrayList;
import java.util.List;

public class Stringify {
    private Stringify() {}

    public static void registerStringifier(final Stringifier stringifier) {
        stringifiers.add(stringifier);
    }

    public static String asString(final Object value) {
        registerDefaultStringifiers();
        Stringifier s = stringifiers.stream()
                .filter(x -> x.handles(value))
                .findFirst()
                .orElse(Stringifier.DEFAULT_STRINGIFIER);

        return s.asString(value);
    }

    private static void registerDefaultStringifiers() {
        if (!defaultsRegistered) {
            registerStringifier(new ElementStringifier());
            registerStringifier(new ElementListStringifier());
            registerStringifier(new NonElementListStringifier(10));
            defaultsRegistered = true;
        }
    }

    private static boolean defaultsRegistered = false;
    private static final List<Stringifier> stringifiers = new ArrayList<>();
}
