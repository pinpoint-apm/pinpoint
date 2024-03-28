package com.navercorp.pinpoint.test.plugin;

import com.navercorp.pinpoint.common.Version;
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

        Collection<Path> result = new ArrayDeque<>();
        for (URL url : list) {
            if (libraryFinder.filter(url)) {
                result.add(Paths.get(url.getPath()));
            }
        }
        Assertions.assertEquals(2, result.size());
    }

    @Test
    public void globTest() throws MalformedURLException {
        String jarPath = File.separator + "home"
                + File.separator + "pinpoint-mssql-jdbc-driver-plugin"
                + File.separator + Version.VERSION
                + File.separator + "pinpoint-mssql-jdbc-driver-plugin-" + Version.VERSION + ".jar";

        String matchPath = "pinpoint-*-plugin-" + Version.VERSION + ".jar";
        Path result = globMatches(jarPath, matchPath);
        Assertions.assertNull(result);

        matchPath = "*" + File.separator + "pinpoint-*-plugin-" + Version.VERSION + ".jar";
        result = globMatches(jarPath, matchPath);
        Assertions.assertNull(result);

        matchPath = "**" + File.separator + "pinpoint-*-plugin-" + Version.VERSION + ".jar";
        result = globMatches(jarPath, matchPath);
        Assertions.assertEquals(Paths.get(jarPath), result);
    }

    private Path globMatches(String jarPath, String matchPath) throws MalformedURLException {
        LibraryFilter.LibraryMatcher matcher = LibraryFilter.createGlobMatcher(new String[]{matchPath});
        LibraryFilter libraryFinder = new LibraryFilter(matcher);

        File file = new File(jarPath);
        URL url = file.toURI().toURL();
        if (libraryFinder.filter(url)) {
            return Paths.get(jarPath);
        }
        return null;
    }
}