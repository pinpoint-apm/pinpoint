/*
 * Copyright 2016 NAVER Corp.
 *
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
 *
 */

package com.navercorp.pinpoint.bootstrap;

import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;

/**
 * @author Woonduk Kang(emeroad)
 */
public class BootstrapJarFile {

    private final List<JarFile> jarFileEntry = new ArrayList<JarFile>();

    public BootstrapJarFile() {
    }

    public void append(JarFile jarFile) {
        if (jarFile == null) {
            throw new NullPointerException("jarFile must not be null");
        }

        this.jarFileEntry.add(jarFile);
    }

    public List<JarFile> getJarFileList() {
        return jarFileEntry;
    }

    public List<String> getJarNameList() {
        List<String> bootStrapJarLIst = new ArrayList<String>(jarFileEntry.size());
        for (JarFile jarFile : jarFileEntry) {
            bootStrapJarLIst.add(jarFile.getName());
        }
        return bootStrapJarLIst;
    }

}
