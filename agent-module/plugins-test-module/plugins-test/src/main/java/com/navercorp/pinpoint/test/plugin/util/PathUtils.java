package com.navercorp.pinpoint.test.plugin.util;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class PathUtils {

    private static final char separatorChar = File.separatorChar;

    public static String wrap(String first, String... more) {
        Objects.requireNonNull(first, "paths");
        Path path = Paths.get(first, more);
        return separatorChar + path.toString() + separatorChar;
    }
}
