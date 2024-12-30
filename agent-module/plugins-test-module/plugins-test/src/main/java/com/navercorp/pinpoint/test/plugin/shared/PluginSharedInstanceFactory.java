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
import com.navercorp.pinpoint.test.plugin.util.ClassLoaderUtils;
import com.navercorp.pinpoint.test.plugin.util.URLUtils;

import java.net.URL;
import java.nio.file.Path;
import java.util.List;

public class PluginSharedInstanceFactory {

    public PluginSharedInstance create(String testClassName, String sharedClassName, List<Path> libs) throws ClassNotFoundException {
        final URL[] urls = URLUtils.pathToUrls(libs);
        final ClassLoader contextClassLoader = ClassLoaderUtils.getContextClassLoader();
        final PluginTestSharedTestClassLoader classLoader = new PluginTestSharedTestClassLoader(urls, contextClassLoader);

        return new PluginSharedInstance(testClassName, sharedClassName, classLoader);
    }
}
