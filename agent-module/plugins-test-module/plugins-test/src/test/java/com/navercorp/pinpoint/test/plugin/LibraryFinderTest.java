package com.navercorp.pinpoint.test.plugin;

import com.navercorp.pinpoint.test.plugin.util.VersionUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class LibraryFinderTest {

    private static final String VERSION = VersionUtils.VERSION;

    @Test
    public void filter() throws MalformedURLException {
        LibraryFilter.LibraryMatcher matcher = LibraryFilter.createContainsMatcher(new String[]{"abc", "123"});
        LibraryFilter libraryFinder = new LibraryFilter(matcher);
        URL url1 = new URL("file:C:\\abc");
        URL url2 = new URL("file:C:\\edf");
        URL url3 = new URL("file:C:\\xyz");
        URL url4 = new URL("file:C:\\123");
        URL url5 = new URL("file:C:\\345");
        List<URL> list = Arrays.asList(url1, url2, url3, url4, url5);
        Collections.shuffle(list);

        Collection<String> result = new ArrayDeque<>();
        for (URL url : list) {
            String path = url.getPath();
            if (libraryFinder.filter(path)) {
                result.add(path);
            }
        }
        Assertions.assertEquals(2, result.size());
    }

    @Test
    public void globTest() throws MalformedURLException {
        String jarPath = File.separator + Paths.get("home",
                "pinpoint-mssql-jdbc-driver-plugin",
                VERSION,
                "pinpoint-mssql-jdbc-driver-plugin-" + VERSION + ".jar"
        );

        String matchPath = "pinpoint-*-plugin-" + VERSION + ".jar";
        Path result = globMatches(jarPath, matchPath);
        Assertions.assertNull(result);

        String matchPath2 = Paths.get("*", "pinpoint-*-plugin-" + VERSION + ".jar").toString();
        Path result2 = globMatches(jarPath, matchPath2);
        Assertions.assertNull(result2);

        String matchPath3 = Paths.get("**", "pinpoint-*-plugin-" + VERSION + ".jar").toString();
        Path result3 = globMatches(jarPath, matchPath3);
        Assertions.assertEquals(Paths.get(jarPath), result3);
    }

    private Path globMatches(String jarPath, String matchPath) throws MalformedURLException {
        LibraryFilter.LibraryMatcher matcher = LibraryFilter.createGlobMatcher(new String[]{matchPath});
        LibraryFilter libraryFinder = new LibraryFilter(matcher);

        if (libraryFinder.filter(jarPath)) {
            return Paths.get(jarPath);
        }
        return null;
    }
}