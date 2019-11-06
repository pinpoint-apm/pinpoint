/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.web.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.navercorp.pinpoint.web.view.CodeResultSerializer;

/**
 * @author Taejin Koo
 */
@JsonSerialize(using = CodeResultSerializer.class)
public class CodeResult {

    private final int code;
    private final Object message;

    public CodeResult(int code) {
        this(code, null);
    }

    public CodeResult(int code, Object message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public Object getMessage() {
        return message;
    }

}
