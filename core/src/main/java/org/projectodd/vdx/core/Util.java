package org.projectodd.vdx.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;

public class Util {
    public static Set<String> extractXMLNS(final List<String> lines) {
        final Pattern pattern = Pattern.compile("xmlns\\s*=\\s*[\"'](.*?)[\"']");
        final Set<String> xmlnses = new TreeSet<>();
        lines.forEach(l -> {
            final Matcher m = pattern.matcher(l);
            if (m.find()) {
                xmlnses.add(m.group(1));
            }
        });

        return xmlnses;
    }

    public static boolean providesXMLNS(final Set<String> xmlnses, URL url) throws IOException {
        final Pattern pattern = Pattern.compile("targetNamespace\\s*=\\s*[\"'](.*?)[\"']");
        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
            String line = reader.readLine();
            while (line != null) {
                final Matcher m = pattern.matcher(line);
                if (m.find() &&
                        xmlnses.contains(m.group(1))) {

                    return true;
                }
                line = reader.readLine();
            }


        }
        return false;
    }

    public static String alternateSpelling(final String current, final List<String> alternates) {
        final String alternate = alternates.stream()
                .map(s -> {
                    final int dist = StringUtils.getLevenshteinDistance(current, s);
                    if (dist < 4) { // difference threshold
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

    public static <T> List<T> asSortedList(Collection<? extends T> col) {
        return col.stream()
                .sorted()
                .collect(Collectors.toList());
    }
}
