/*
 * Copyright 2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
            registerStringifier(new ListListStringifier(20));
            registerStringifier(new NonElementListStringifier(5, Integer.MAX_VALUE));
            defaultsRegistered = true;
        }
    }

    private static boolean defaultsRegistered = false;
    private static final List<Stringifier> stringifiers = new ArrayList<>();
}
