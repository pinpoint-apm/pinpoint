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

package com.navercorp.pinpoint.web.util;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.util.Bytes;

/**
 * @author emeroad
 */
public class KeyValueUtils {

	@SuppressWarnings("deprecation")
    public static boolean equalsFamily(KeyValue keyValue, byte[] familyName) {
        if (keyValue == null) {
            throw new NullPointerException("keyValue must not be null");
        }
        if (familyName == null) {
            throw new NullPointerException("familyName must not be null");
        }
        final byte[] buffer = keyValue.getBuffer();
        final int familyOffset = keyValue.getFamilyOffset();
        final byte familyLength = keyValue.getFamilyLength(familyOffset);
        return Bytes.equals(buffer, familyOffset, familyLength, familyName, 0, familyName.length);
    }
}
