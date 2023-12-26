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
    @Override
    public boolean test(String name) {
        if(name.startsWith("javax.servlet.")) {
            return false;
        }

        final JvmVersion version = JvmUtils.getVersion();
        if (version.onOrAfter(JvmVersion.JAVA_9)) {
            if (name.startsWith("javax.xml.bind.")
                    || name.startsWith("javax.annotation.")) {
                return false;
            }
        }

        if (name.startsWith("javax.jms")
                || name.startsWith("javax.ws")) {
            return false;
        }
        return name.startsWith("java.")
                || name.startsWith("jdk.")
                || name.startsWith("javax.")
                || name.startsWith("sun.")
                || name.startsWith("com.sun.")
                || name.startsWith("org.w3c.")
                || name.startsWith("org.xml.")
                || name.startsWith("org.ietf.jgss.");

    }
}
