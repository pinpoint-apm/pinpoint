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

import com.navercorp.pinpoint.common.util.apache.IntHashMap;

import java.util.EnumSet;
import java.util.Set;

/**
 * @author hyungil.jeong
 */
public enum JvmVersion {
    JAVA_5(1.5f, 49),
    JAVA_6(1.6f, 50),
    JAVA_7(1.7f, 51),
    JAVA_8(1.8f, 52),
    UNSUPPORTED(-1, -1);

    private final float version;
    private final int classVersion;

    private static final Set<JvmVersion> JVM_VERSION = EnumSet.allOf(JvmVersion.class);
    private static final IntHashMap<JvmVersion> CLASS_VERSION_MAP = toClassVersionMap();


    JvmVersion(float version, int classVersion) {
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
            float version = Float.parseFloat(javaVersion);
            return getFromVersion(version);
        } catch (NumberFormatException e) {
            return UNSUPPORTED;
        }
    }

     /**
     * @deprecated Since 1.7.0. Use {@link JvmVersion#getFromVersion(float)}
     */
    @Deprecated
    public static JvmVersion getFromVersion(double javaVersion) {
        return getFromVersion((float)javaVersion);
    }

    public static JvmVersion getFromVersion(float javaVersion) {
        for (JvmVersion version : JVM_VERSION) {
            if (Float.compare(version.version, javaVersion) == 0) {
                return version;

            }
        }
        return JvmVersion.UNSUPPORTED;
    }


    private static IntHashMap<JvmVersion> toClassVersionMap() {
        final IntHashMap<JvmVersion> jvmVersionIntHashMap = new IntHashMap<JvmVersion>();
        for (JvmVersion version : values()) {
            jvmVersionIntHashMap.put(version.classVersion, version);
        }
        return jvmVersionIntHashMap;
    }

    public static JvmVersion getFromClassVersion(int classVersion) {
        final JvmVersion jvmVersion = CLASS_VERSION_MAP.get(classVersion);
        if (jvmVersion == null) {
            return JvmVersion.UNSUPPORTED;
        }

        return jvmVersion;
    }
}
