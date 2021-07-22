package com.navercorp.pinpoint.test.plugin;

import org.junit.Assert;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class LibraryFinderTest {

    @Test
    public void filter() throws MalformedURLException {
        AbstractPinpointPluginTestSuite.LibraryFilter libraryFinder = new AbstractPinpointPluginTestSuite.LibraryFilter(new String[]{"abc", "123"});
        URL url1 = new URL("file:C:\\abc");
        URL url2 = new URL("file:C:\\edf");
        URL url3 = new URL("file:C:\\xyz");
        URL url4 = new URL("file:C:\\123");
        URL url5 = new URL("file:C:\\345");
        List<URL> list = Arrays.asList(url1, url2, url3, url4, url5);
        Collections.shuffle(list);

        Collection<String> result = new ArrayDeque<>();
        for (URL url : list) {
            String filter = libraryFinder.filter(url);
            if (filter != null) {
                result.add(filter);
            }
        }
        Assert.assertEquals(2, result.size());
    }
}