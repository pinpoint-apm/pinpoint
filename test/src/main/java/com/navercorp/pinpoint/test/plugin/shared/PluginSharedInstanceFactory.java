/*
 * Copyright 2023 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.test.plugin.shared;

import com.navercorp.pinpoint.test.plugin.classloader.PluginTestSharedTestClassLoader;
import com.navercorp.pinpoint.test.plugin.util.URLUtils;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class PluginSharedInstanceFactory {

    public PluginSharedInstance create(String testClassName, String sharedClassName, List<String> libs) throws ClassNotFoundException {
        final List<File> fileList = new ArrayList<>();
        for (String classPath : libs) {
            File file = new File(classPath);
            fileList.add(file);
        }
        final URL[] urls = URLUtils.fileToUrls(fileList);
        final PluginTestSharedTestClassLoader classLoader = new PluginTestSharedTestClassLoader(urls, Thread.currentThread().getContextClassLoader());

        return new PluginSharedInstance(testClassName, sharedClassName, classLoader);
    }
}
