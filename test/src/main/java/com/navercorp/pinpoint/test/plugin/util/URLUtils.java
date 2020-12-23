package com.navercorp.pinpoint.test.plugin.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
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

    public static URL[] fileToUrls(List<File> fileList)  {
        if (fileList == null) {
            return new URL[0];
        }
        try {
            final File[] files = fileList.toArray(new File[0]);
            return FileUtils.toURLs(files);
        } catch (IOException e) {
            throw new RuntimeException(e. getMessage(), e);
        }
    }

}
