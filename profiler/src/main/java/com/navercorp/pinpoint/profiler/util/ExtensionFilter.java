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

package com.navercorp.pinpoint.profiler.util;

import com.navercorp.pinpoint.common.util.Assert;

import java.util.jar.JarEntry;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ExtensionFilter implements JarEntryFilter {

    public static final JarEntryFilter CLASS_FILTER = new ExtensionFilter(".class");

    private final String extension;

    public ExtensionFilter(String extension) {
        this.extension = Assert.requireNonNull(extension, "extension");
    }

    @Override
    public boolean filter(JarEntry jarEntry) {
        final String name = jarEntry.getName();
        return name.endsWith(extension);
    }
}
