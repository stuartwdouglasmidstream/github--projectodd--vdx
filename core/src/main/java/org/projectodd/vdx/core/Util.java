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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.projectodd.vdx.core.thirdparty.Levenshtein;

public class Util {
    private static final Pattern XMLNS_RE = Pattern.compile("xmlns\\s*=\\s*[\"'](.*?)[\"']");

    public static Set<String> extractXMLNS(final List<String> lines) {
        final Set<String> xmlnses = new TreeSet<>();
        lines.forEach(l -> {
            final Matcher m = XMLNS_RE.matcher(l);
            if (m.find()) {
                xmlnses.add(m.group(1));
            }
        });

        return xmlnses;
    }

    private static final Pattern TARGET_NS_RE = Pattern.compile("targetNamespace\\s*=\\s*[\"'](.*?)[\"']");

    public static boolean providesXMLNS(final Set<String> xmlnses, URL url) throws IOException {
        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
            String line = reader.readLine();
            while (line != null) {
                final Matcher m = TARGET_NS_RE.matcher(line);
                if (m.find() &&
                        xmlnses.contains(m.group(1))) {

                    return true;
                }
                line = reader.readLine();
            }


        }
        return false;
    }

    public static String alternateSpelling(final String current, final Collection<String> alternates) {
        return alternateSpelling(current, alternates, dynamicThreshold(current));
    }

    // we want a smaller threshold for short words, and want a max threshold, no matter the length
    private static int dynamicThreshold(final String s) {
        final int len = s.length();

        if (len < 6)  return 2;
        if (len < 10) return 3;
        if (len < 14) return 4;
                      return 5;
    }

    public static String alternateSpelling(final String current, final Collection<String> alternates, final int threshold) {
        final String alternate = alternates.stream()
                .map(s -> {
                    int dist = Levenshtein.getLevenshteinDistance(current, s, threshold);
                    if (dist > 0) {
                        return String.format("%s:%s", dist, s);
                    } else {
                        return null;
                    }
                })
                .filter(x -> x != null)
                .sorted()
                .findFirst()
                .orElse(null);

        if (alternate != null) {
            final String[] parts = alternate.split(":");

            return parts[1];
        }

        return null;
    }

    public static String withPrefix(final String prefix, final String v) {
        return String.join("\n",
                           Arrays.stream(v.split("\\n"))
                                   .map(x -> String.format("%s%s", prefix, x))
                                   .collect(Collectors.toList()));
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> asSortedList(Collection<? extends T> col) {
        if (col == null) {
            return Collections.EMPTY_LIST;
        }

        return col.stream()
                .sorted()
                .collect(Collectors.toList());
    }

    public static String asCommaString(Collection<?> col) {
        return String.join(", ", col.stream()
                .map(Object::toString)
                .collect(Collectors.toList()));
    }

    public static String pathToString(List<String> path) {
        return String.join(" > ", path);
    }

    public static Function<String, String> possiblyUnderscoredName(final Set<String> possibles) {
        return s -> {
            if (possibles.contains(s)) return s;
            final String sans_ = s.replace("_", "-");
            if (possibles.contains(sans_)) return sans_;

            return s;
        };
    }


}
