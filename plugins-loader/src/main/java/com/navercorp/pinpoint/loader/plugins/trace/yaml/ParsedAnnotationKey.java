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

import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.AnnotationKeyBuilder;
import com.navercorp.pinpoint.common.util.StringUtils;

/**
 * @author HyunGil Jeong
 */
public class ParsedAnnotationKey {

    private Integer code;
    private String name;
    private ParsedAnnotationKeyProperty property;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ParsedAnnotationKeyProperty getProperty() {
        return property;
    }

    public void setProperty(ParsedAnnotationKeyProperty property) {
        this.property = property;
    }

    AnnotationKey toAnnotationKey() {
        if (code == null) {
            throw new NullPointerException("code");
        }
        if (StringUtils.isEmpty(name)) {
            throw new IllegalArgumentException("annotationKey name must not be empty");
        }
        AnnotationKeyBuilder builder = new AnnotationKeyBuilder(code, name);
        if (property != null) {
            builder.viewInRecordSet(property.isViewInRecordSet());
        }
        return builder.build();
    }
}
