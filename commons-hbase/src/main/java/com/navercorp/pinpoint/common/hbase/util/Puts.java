/*
 * Copyright 2023 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.common.hbase.util;

import org.apache.hadoop.hbase.client.Put;

public final class Puts {
    private Puts() {
    }

    public static Put put(byte[] rowName, byte[] familyName, byte[] qualifier, byte[] value) {
        final Put put = new Put(rowName, true);
        if (familyName != null) {
            put.addColumn(familyName, qualifier, value);
        }
        return put;
    }

    public static Put put(byte[] rowName, byte[] familyName, long timestamp, byte[] qualifier, byte[] value) {
        final Put put = new Put(rowName, true);
        if (familyName != null) {
            put.addColumn(familyName, qualifier, timestamp, value);
        }
        return put;
    }
}
