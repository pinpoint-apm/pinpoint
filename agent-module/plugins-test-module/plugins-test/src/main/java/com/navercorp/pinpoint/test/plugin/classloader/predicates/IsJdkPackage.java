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

package com.navercorp.pinpoint.test.plugin.classloader.predicates;

import com.navercorp.pinpoint.common.util.JvmUtils;
import com.navercorp.pinpoint.common.util.JvmVersion;

import java.util.function.Predicate;

// exclude "javax.servlet.", "javax.xml.bind.", "javax.annotation.", "javax.ws", "javax.jms", "java.", "jdk.", ...
public class IsJdkPackage implements Predicate<String> {

    public static final String[] PACKAGES = new String[] {
            "java.",
            "jdk.",
            "javax.",
            "sun.",
            "com.sun.",
            "org.w3c.",
            "org.xml.",
            "org.ietf.jgss.",
    };

    public static final String[] FRAMEWORK_PACKAGE = new String[] {
            "javax.servlet.",
            "javax.jms",
            "javax.ws",
    };

    public static final String[] JAVA9_PACKAGE = new String[] {
            "javax.xml.bind.",
            "javax.annotation",
    };

    private final PackageFilter jdkFilter = new PackageFilter(PACKAGES);

    private final PackageFilter frameworkFilter = new PackageFilter(FRAMEWORK_PACKAGE);

    private final boolean isJava9 = JvmUtils.getVersion().onOrAfter(JvmVersion.JAVA_9);
    private final PackageFilter java9Filter = new PackageFilter(JAVA9_PACKAGE);

    public IsJdkPackage() {
    }

    @Override
    public boolean test(String name) {
        if (frameworkFilter.test(name)) {
            return false;
        }

        if (isJava9) {
            if (java9Filter.test(name)) {
                return false;
            }
        }
        return jdkFilter.test(name);

    }
}
