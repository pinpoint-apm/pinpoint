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

package com.navercorp.pinpoint.bootstrap.java9.classloader;


import java.util.List;

/**
 * @author emeroad
 */
public class ProfilerLibClass {

    static final boolean ON_LOAD_CLASS = true;
    static final boolean DELEGATE_PARENT = false;

    private final String[] libClass;

    public ProfilerLibClass(List<String> libClass) {
        if (libClass == null) {
            throw new NullPointerException("libClass");
        }
        this.libClass = libClass.toArray(new String[0]);
    }


    public boolean onLoadClass(String clazzName) {
        final int length = libClass.length;
        for (int i = 0; i < length; i++) {
            if (clazzName.startsWith(libClass[i])) {
                return ON_LOAD_CLASS;
            }
        }
        return DELEGATE_PARENT;
    }
}
