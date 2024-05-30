package com.pinpoint.test.common.view;

public class HrefTag {
    private final String text;
    private final String path;

    public static HrefTag of(String path) {
        return new HrefTag(path, path);
    }

    public static HrefTag of(String text, String path) {
        return new HrefTag(text, path);
    }

    HrefTag(String text, String path) {
        this.text = text;
        this.path = path;
    }

    public String getText() {
        return text;
    }

    public String getPath() {
        return path;
    }
}
