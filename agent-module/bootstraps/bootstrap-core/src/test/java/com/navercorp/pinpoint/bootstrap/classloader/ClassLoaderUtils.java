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

package com.navercorp.pinpoint.bootstrap.classloader;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author Woonduk Kang(emeroad)
 */
class ClassLoaderUtils {
    static void close(ClassLoader cl) throws IOException {
        // for JDK 1.7
        if (cl instanceof Closeable) {
            ((Closeable) cl).close();
        }
    }
}
