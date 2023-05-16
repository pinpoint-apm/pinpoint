/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.common.hbase;

import java.util.Arrays;
import java.util.Objects;

public enum HBaseClientVersion {
    V1("0.9", "1."),
    V2("2."),
    V3("3.");

    private final String[] supportedVersion;

    HBaseClientVersion(String... supportedVersion) {
        this.supportedVersion = supportedVersion;
    }

    @Override
    public String toString() {
        return "HBaseVersion{" +
                "supportedVersion=" + Arrays.toString(supportedVersion) +
                "} " + super.toString();
    }

    public boolean acceptVersion(String actualVersion) {
        Objects.requireNonNull(actualVersion, "actualVersion");
        for (String version : supportedVersion) {
            if (actualVersion.startsWith(version)) {
                return true;
            }
        }
        return false;
    }

    public static HBaseClientVersion getHBaseVersion(String hbaseClientVersion) {
        for (HBaseClientVersion hBaseClientVersion : values()) {
            if(hBaseClientVersion.acceptVersion(hbaseClientVersion)) {
                return hBaseClientVersion;
            }
        }
        return null;
    }
}
