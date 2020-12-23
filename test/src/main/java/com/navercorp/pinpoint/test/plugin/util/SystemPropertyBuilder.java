package com.navercorp.pinpoint.test.plugin.util;


import java.util.ArrayList;
import java.util.List;

public final class SystemPropertyBuilder {
    private List<String> list = new ArrayList<>();

    public SystemPropertyBuilder add(String key, String value) {
        list.add(format(key, value));
        return this;
    }

    public static String format(String key, String value) {
        return String.format("-D%s=%s", key, value);
    }

    public List<String> build() {
        return new ArrayList<>(list);
    }

    public void clear() {
        this.list.clear();
    }
}
