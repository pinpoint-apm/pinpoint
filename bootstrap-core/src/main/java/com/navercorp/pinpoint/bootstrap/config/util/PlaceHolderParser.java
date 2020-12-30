package com.navercorp.pinpoint.bootstrap.config.util;

public class PlaceHolderParser {

    public PlaceHolderParser() {
    }

    public PlaceHolder parse(String placeholder) {
        final int start = placeholder.indexOf(PlaceHolder.START);
        if (start == -1) {
            return null;
        }
        final int end = placeholder.indexOf(PlaceHolder.END, start);
        if (end == -1) {
            return null;
        }
        String contents = placeholder.substring(PlaceHolder.START.length(), end);
        int delimiter = contents.indexOf(PlaceHolder.DELIMITER);
        if (delimiter == -1) {
            return new PlaceHolder(contents, null);
        }

        String key = contents.substring(0, delimiter);
        String defaultValue = contents.substring(delimiter + 1);
        return new PlaceHolder(key, defaultValue);
    }

}
