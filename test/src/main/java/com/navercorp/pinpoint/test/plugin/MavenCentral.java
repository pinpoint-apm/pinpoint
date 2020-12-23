/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.test.plugin;

/**
 * @author Woonduk Kang(emeroad)
 */
public final class MavenCentral {
    public static final String MAVEN_CENTRAL_SECURE = "https://repo.maven.apache.org/maven2";

    public static final boolean JDK8_PLUS = isJDK8Plus();

    private static boolean isJDK8Plus() {
        try {
            Class.forName("java.util.Optional");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /*
     * for jdk 6, 7
     * Central 501 HTTPS Required
     * https://support.sonatype.com/hc/en-us/articles/360041287334
     * Discontinued support for TLSv1.1 and below
     * https://central.sonatype.org/articles/2018/May/04/discontinued-support-for-tlsv11-and-below/
     * */
    public static final String MAVEN_CENTRAL_INSECURE = "http://insecure.repo1.maven.org/maven2/";
    
    private MavenCentral() {
    }

    public static String getAddress() {
        if (JDK8_PLUS) {
            return MAVEN_CENTRAL_SECURE;
        } else {
            return MAVEN_CENTRAL_INSECURE;
        }
    }
}
