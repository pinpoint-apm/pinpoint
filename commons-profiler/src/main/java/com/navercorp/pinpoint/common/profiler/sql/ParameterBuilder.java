package com.navercorp.pinpoint.common.profiler.sql;

import static com.navercorp.pinpoint.common.profiler.sql.Tokens.SEPARATOR;

public class ParameterBuilder {
    private final StringBuilder builder;
    private boolean change = false;

    public ParameterBuilder() {
        this.builder = new StringBuilder(32);
    }

    public void touch() {
        this.change = true;
    }

    public boolean isChange() {
        if (change) {
            return true;
        }
        return builder.length() > 0;
    }

    public void separator() {
        if (builder.length() == 0) {
            // first parameter
            return;
        }
        builder.append(SEPARATOR);
    }

    public void append(String str) {
        builder.append(str);
    }

    public void append(String str, int start, int end) {
        builder.append(str, start, end);
    }

    public void appendSeparatorCheck(char ch) {
        if (ch == ',') {
            builder.append(",,");
        } else {
            builder.append(ch);
        }
    }

    public void append(char ch) {
        this.builder.append(ch);
    }

    StringBuilder getBuilder() {
        return builder;
    }

    public String build() {
        StringBuilder copy = builder;
        if (copy.length() > 0) {
            return copy.toString();
        }
        return "";
    }
}
