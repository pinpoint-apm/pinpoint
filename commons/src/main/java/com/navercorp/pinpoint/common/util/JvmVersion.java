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

package com.navercorp.pinpoint.common.util;

/**
 * @author hyungil.jeong
 */
public enum JvmVersion {
    JAVA_5(1.5, 49),
    JAVA_6(1.6, 50),
    JAVA_7(1.7, 51),
    JAVA_8(1.8, 52),
    UNSUPPORTED(-1, -1);

    private final double version;
    private final int classVersion;

    JvmVersion(double version, int classVersion) {
        this.version = version;
        this.classVersion = classVersion;
    }

    public boolean onOrAfter(JvmVersion other) {
        if (this == UNSUPPORTED || other == UNSUPPORTED) {
            return false;
        }
        return this == other || this.version > other.version;
    }

    public static JvmVersion getFromVersion(String javaVersion) {
        try {
            double version = Double.parseDouble(javaVersion);
            return getFromVersion(version);
        } catch (NumberFormatException e) {
            return UNSUPPORTED;
        }
    }

    public static JvmVersion getFromVersion(double javaVersion) {
        for (JvmVersion version : JvmVersion.values()) {
            if (Double.compare(version.version, javaVersion) == 0) {
                return version;
            }
        }
        return JvmVersion.UNSUPPORTED;
    }

    public static JvmVersion getFromClassVersion(int classVersion) {
        for (JvmVersion version : JvmVersion.values()) {
            if (version.classVersion == classVersion) {
                return version;
            }
        }
        return JvmVersion.UNSUPPORTED;
    }
}
