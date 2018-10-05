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
public class ApiMetaData {

    private final int apiId; // required
    private final String apiInfo; // required
    private int line; // optional
    private int type; // optional

    public ApiMetaData(int apiId, String apiInfo) {
        this.apiId = apiId;
        this.apiInfo = apiInfo;
    }

    public int getApiId() {
        return apiId;
    }


    public String getApiInfo() {
        return apiInfo;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
