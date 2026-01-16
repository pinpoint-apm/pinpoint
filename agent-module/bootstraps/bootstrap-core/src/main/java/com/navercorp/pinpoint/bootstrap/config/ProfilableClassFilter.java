/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.config;

import com.navercorp.pinpoint.common.util.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author emeroad
 */
public class ProfilableClassFilter implements Filter<String> {

    private static final String SUFFIX = ".*";

    private final Set<String> profileInclude = new HashSet<>();
    private final String[] profileIncludeSub;

    public ProfilableClassFilter(String profilableClass) {
        if (StringUtils.isEmpty(profilableClass)) {
            this.profileIncludeSub = new String[0];
            return;
        }
        String[] classNames = profilableClass.split(",");

        Set<String> subPackages = new HashSet<>();
        for (String className : classNames) {
            if (className.endsWith(SUFFIX)) {
                // "com.foo.*" -> "com/foo/"
                String packagePath = className.substring(0, className.length() - SUFFIX.length()).replace('.', '/') + "/";
                subPackages.add(packagePath);
            } else {
                String classPath = className.trim().replace('.', '/');
                this.profileInclude.add(classPath);
            }
        }
        this.profileIncludeSub = subPackages.toArray(new String[0]);
    }

    /**
     * TODO remove this. Added this method to test the "call stack view" on a test server
     *
     * @param className
     * @return
     */
    @Override
    public boolean filter(String className) {
        if (profileInclude.contains(className)) {
            return true;
        } else {
            for (String pkg : profileIncludeSub) {
                if (className.startsWith(pkg)) {
                    return true;
                }
            }
        }
        return false;
    }


    @Override
    public String toString() {
        return "ProfilableClassFilter{"
                + "profileInclude=" + profileInclude +
                ", profileIncludeSub=" + Arrays.toString(profileIncludeSub) +
                '}';
    }
}
