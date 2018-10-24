/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.metadata;

/**
 * @author Woonduk Kang(emeroad)
 */
public class StringMetaData {

    private final int stringId; // required
    private final String stringValue; // required

    public StringMetaData(int stringId, String stringValue) {
        this.stringId = stringId;
        this.stringValue = stringValue;
    }

    public int getStringId() {
        return stringId;
    }

    public String getStringValue() {
        return stringValue;
    }
}
