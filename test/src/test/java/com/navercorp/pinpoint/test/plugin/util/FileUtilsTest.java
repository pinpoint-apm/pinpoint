/*
 * Copyright 2020 NAVER Corp.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.test.plugin.util;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author WonChul Heo(heowc)
 */
public class FileUtilsTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private String prefix;

    @Before
    public void init() {
        prefix = folder.getRoot().getAbsolutePath() + File.separator;
    }

    @Test(expected = NullPointerException.class)
    public void testToURLWhenNull() throws IOException {
        FileUtils.toURL((File) null);
        FileUtils.toURL((String) null);
        FileUtils.toURLs((File[]) null);
        FileUtils.toURLs((String[]) null);
    }

    @Test
    public void testToURLWithFile() throws IOException {
        final File file = folder.newFile("test.txt");
        final URL url = FileUtils.toURL(file);
        assertThat(url.getPath(), is(prefix + "test.txt"));
    }

    @Test
    public void testToURLWithURL() throws IOException {
        final File file = folder.newFile("test.txt");
        final URL url = FileUtils.toURL(prefix + "test.txt");
        assertThat(url, is(file.toURI().toURL()));
    }

    @Test
    public void testToURLsWithFiles() throws IOException {
        final File aTxt = folder.newFile("a.txt");
        final File bTxt = folder.newFile("b.txt");
        final URL[] urls = FileUtils.toURLs(new File[]{ aTxt, bTxt });
        assertThat(urls.length, is(2));
        assertThat(urls[0].getPath(), is(prefix + "a.txt"));
        assertThat(urls[1].getPath(), is(prefix + "b.txt"));
    }

    @Test
    public void testToURLsWithURLs() throws IOException {
        final File aTxt = folder.newFile("a.txt");
        final File bTxt = folder.newFile("b.txt");
        final URL[] urls = FileUtils.toURLs(new String[]{ prefix + "a.txt", prefix + "b.txt"});
        assertThat(urls.length, is(2));
        assertThat(urls[0], is(aTxt.toURI().toURL()));
        assertThat(urls[1], is(bTxt.toURI().toURL()));
    }
}
