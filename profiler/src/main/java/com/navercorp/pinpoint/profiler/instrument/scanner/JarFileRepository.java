/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.instrument.scanner;

import com.navercorp.pinpoint.common.util.Assert;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class JarFileRepository {
    private final JarFileScanner[] scanners;


    public JarFileRepository(List<String> jarFilePathList) {
        this.scanners = newJarScanner(jarFilePathList);
    }

    private JarFileScanner[] newJarScanner(List<String> jarFilePathList) {
        Assert.requireNonNull(jarFilePathList, "jarFilePathList");

        final List<JarFileScanner> jarFileList = new ArrayList<JarFileScanner>(jarFilePathList.size());
        for (String jarFilePath : jarFilePathList) {
            JarFileScanner jarFileScanner = new JarFileScanner(jarFilePath);
            jarFileList.add(jarFileScanner);
        }
        return jarFileList.toArray(new JarFileScanner[0]);
    }


    public InputStream openStream(String resourceName) {
        Assert.requireNonNull(resourceName, "resourceName");

        for (Scanner scanner : scanners) {
            final InputStream inputStream = scanner.openStream(resourceName);
            if (inputStream != null) {
                return inputStream;
            }
        }
        return null;
    }

    public void close() {
        for (Scanner scanner : scanners) {
            scanner.close();
        }
    }


}
