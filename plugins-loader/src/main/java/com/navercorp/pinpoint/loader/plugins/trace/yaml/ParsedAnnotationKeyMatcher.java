/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.loader.plugins.trace.yaml;

import com.navercorp.pinpoint.common.trace.AnnotationKeyMatcher;
import com.navercorp.pinpoint.common.trace.AnnotationKeyMatchers;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.StringUtils;

/**
 * @author HyunGil Jeong
 */
public class ParsedAnnotationKeyMatcher {

    private String type;
    private Integer code;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    AnnotationKeyMatcher toAnnotationKeyMatcher() {
        if (StringUtils.isEmpty(type)) {
            throw new IllegalArgumentException("matcher type must not be empty");
        }
        if (type.equalsIgnoreCase("exact")) {
            Assert.requireNonNull(code, "code must not be null for matcher type 'exact'");
            return AnnotationKeyMatchers.exact(code);
        } else if (type.equalsIgnoreCase("args")) {
            return AnnotationKeyMatchers.ARGS_MATCHER;
        } else if (type.equalsIgnoreCase("none")) {
            return AnnotationKeyMatchers.NOTHING_MATCHER;
        }
        throw new IllegalStateException("Unknown matcher type : " + type);
    }
}
