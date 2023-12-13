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

import org.apache.hadoop.hbase.client.Get;

public final class Gets {

    private Gets() {
    }

    public static Get get(byte[] rowName, byte[] familyName, byte[] qualifier) {
        final Get get = new Get(rowName);
        if (familyName != null) {
            if (qualifier != null) {
                get.addColumn(familyName, qualifier);
            } else {
                get.addFamily(familyName);
            }
        }
        return get;
    }
}
