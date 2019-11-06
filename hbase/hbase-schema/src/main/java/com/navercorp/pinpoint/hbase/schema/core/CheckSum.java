/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.hbase.schema.core;

import org.springframework.util.DigestUtils;

import java.nio.charset.Charset;
import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
public class CheckSum {

    private static final int VERSION = 0;
    private static final String SEPARATOR = ":";

    private static final int VERSION_LENGTH = 1; // must be less than 10
    private static final int VERSION_OFFSET = VERSION_LENGTH + SEPARATOR.length();
    private static final int CHECKSUM_OFFSET = VERSION_OFFSET + VERSION_LENGTH + SEPARATOR.length();

    private final int version;
    private final String checkSum;

    public CheckSum(int version, String checkSum) {
        this.version = version;
        this.checkSum = checkSum;
    }

    public int getVersion() {
        return version;
    }

    public String getCheckSum() {
        return checkSum;
    }

    public static CheckSum parse(String checkSumString) {
        int versionLength = Integer.parseInt(checkSumString.substring(0, 1));
        int version = Integer.parseInt(checkSumString.substring(VERSION_OFFSET, VERSION_OFFSET + versionLength));
        String checkSum = checkSumString.substring(CHECKSUM_OFFSET);
        return new CheckSum(version, checkSum);
    }

    public static CheckSum compute(int version, String value) {
        if (version == 0) {
            byte[] bValue = value.getBytes(Charset.forName("UTF-8"));
            return new CheckSum(version, DigestUtils.md5DigestAsHex(bValue));
        }
        throw new IllegalArgumentException("Unsupported check sum version : " + version);
    }

    public static int getCurrentVersion() {
        return VERSION;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CheckSum checkSum1 = (CheckSum) o;
        return version == checkSum1.version &&
                Objects.equals(checkSum, checkSum1.checkSum);
    }

    @Override
    public int hashCode() {

        return Objects.hash(version, checkSum);
    }

    @Override
    public String toString() {
        return VERSION_LENGTH + SEPARATOR + version + SEPARATOR + checkSum;
    }
}
