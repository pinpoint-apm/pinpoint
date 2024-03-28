package com.navercorp.pinpoint.test.plugin.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CommandLineOption {
    private final List<String> options = new ArrayList<>();

    public CommandLineOption() {
    }

    public void addOption(String option) {
        options.add(option);
    }

    public void addOptions(List<String> options) {
        Objects.requireNonNull(options, "options");
        this.options.addAll(options);
    }

    public void addSystemProperty(String key, String value) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");

        String option = format(key, value);
        this.options.add(option);
    }

    private String format(String key, String value) {
        return String.format("-D%s=%s", key, value);
    }

    public List<String> build() {
        return new ArrayList<>(options);
    }

    @Override
    public String toString() {
        return "CommandLineOption{" +
                "options=" + options +
                '}';
    }
}
