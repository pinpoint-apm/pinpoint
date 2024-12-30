package com.navercorp.pinpoint.test.plugin.util;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class URLUtils {

    public static URL[] fileToUrls(String[] jars) {
        if (jars == null) {
            return new URL[0];
        }

        try {
            return FileUtils.toURLs(jars);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static URL[] pathToUrls(List<Path> jars) {
        if (jars == null) {
            return new URL[0];
        }
        List<URL> urls = new ArrayList<>(jars.size());
        for (Path jar : jars) {
            urls.add(toURL(jar));
        }
        return urls.toArray(new URL[0]);
    }

    private static URL toURL(Path jar) {
        try {
            return jar.toUri().toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }


}
