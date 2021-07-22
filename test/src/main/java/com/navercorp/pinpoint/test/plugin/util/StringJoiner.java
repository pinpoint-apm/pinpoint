package com.navercorp.pinpoint.test.plugin.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * copy com.navercorp.pinpoint.common.util.StringJoiner
 */
public class StringJoiner {
    private final String delimiter;
    private final List<String> list = new ArrayList<>();

    public StringJoiner(String delimiter) {
        this.delimiter = Objects.requireNonNull(delimiter, "delimiter");
    }

    public void add(String str) {
        this.list.add(str);
    }

    public String toString() {
        int bufferSize = getBufferSize(list, delimiter);

        StringBuilder buffer = new StringBuilder(bufferSize);
        build(buffer, list, delimiter);
        return buffer.toString();
    }

    static void build(StringBuilder buffer, Collection<String> list, String separator) {
        boolean first = true;
        for (String str : list) {
            if (first) {
                first = false;
            } else {
                buffer.append(separator);
            }
            buffer.append(str);
        }
    }

    static int getBufferSize(Collection<String> list, String separator) {
        final int length = separator.length();

        int bufferSize = 0;
        boolean first = true;
        for (String str : list) {
            if (first) {
                first = false;
            } else {
                bufferSize += length;
            }
            // null == "null"
            bufferSize += StringUtils.getLength(str, 4);
        }
        return bufferSize;
    }
}
