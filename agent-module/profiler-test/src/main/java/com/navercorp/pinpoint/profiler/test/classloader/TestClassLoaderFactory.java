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

package com.navercorp.pinpoint.profiler.test.classloader;

import com.navercorp.pinpoint.profiler.context.module.DefaultApplicationContext;

import java.net.URL;
import java.util.Objects;

/**
 * @author hyungil.jeong
 */
public class TestClassLoaderFactory {


    public static TestClassLoader createTestClassLoader(DefaultApplicationContext applicationContext, URL[] urls, ClassLoader parentClassLoader) {
        Objects.requireNonNull(applicationContext, "applicationContext");
        Objects.requireNonNull(urls, "urls");
        return new TestClassLoader(applicationContext, urls, parentClassLoader);
    }

}
