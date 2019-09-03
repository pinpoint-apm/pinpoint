/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.common.util;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.assertEquals;


/**
 * @author Woonduk Kang(emeroad)
 */
public class FileUtilsTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final URL testURL = CodeSourceUtils.getCodeLocation(this.getClass());
    private final File testFile = new File(testURL.getFile());
    private final String testPath = testURL.getFile();


    @Test
    public void toURL_file() throws IOException {
        URL url = FileUtils.toURL(testFile);
        assertEquals(url, testURL);
    }

    @Test
    public void toURLs_file() throws IOException {
        File[] files = new File[]{testFile};
        URL[] urls = FileUtils.toURLs(files);
        assertEquals(urls[0], testURL);
    }

    @Test
    public void toURL_URL() throws IOException {
        URL url = FileUtils.toURL(testPath);
        assertEquals(url, testURL);
    }

    @Test
    public void toURLs_URL() throws IOException {
        String[] filePaths = new String[]{testPath};
        URL[] urls = FileUtils.toURLs(filePaths);
        assertEquals(urls[0], testURL);
    }
}