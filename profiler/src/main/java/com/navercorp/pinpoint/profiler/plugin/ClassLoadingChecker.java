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
 */

package com.navercorp.pinpoint.profiler.plugin;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ClassLoadingChecker {

    private final Set<String> loadClass = new HashSet<>();

    public boolean isFirstLoad(String className) {
        Objects.requireNonNull(className, "className");

        if (this.loadClass.add(className)) {
            // first load
            return true;
        }
        // already exist
        return false;
    }
}
