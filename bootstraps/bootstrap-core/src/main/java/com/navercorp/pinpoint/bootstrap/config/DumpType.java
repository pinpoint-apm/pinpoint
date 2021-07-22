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

package com.navercorp.pinpoint.bootstrap.config;

/**
 * @author emeroad
 */
public enum DumpType {
//  NONE(-1),  comment out because of duplicated configuration.
    ALWAYS(0), EXCEPTION(1);

    private final int code;
    DumpType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }


}
