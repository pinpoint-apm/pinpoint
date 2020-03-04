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
import com.navercorp.pinpoint.common.trace.DefaultServiceTypeInfo;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeBuilder;
import com.navercorp.pinpoint.common.trace.ServiceTypeInfo;
import com.navercorp.pinpoint.common.util.StringUtils;

/**
 * @author HyunGil Jeong
 */
public class ParsedServiceType {

    private Short code;
    private String name;
    private String desc;
    private ParsedServiceTypeProperty property;
    private ParsedAnnotationKeyMatcher matcher;

    public Short getCode() {
        return code;
    }

    public void setCode(Short code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public ParsedServiceTypeProperty getProperty() {
        return property;
    }

    public void setProperty(ParsedServiceTypeProperty property) {
        this.property = property;
    }

    public ParsedAnnotationKeyMatcher getMatcher() {
        return matcher;
    }

    public void setMatcher(ParsedAnnotationKeyMatcher matcher) {
        this.matcher = matcher;
    }

    ServiceTypeInfo toServiceTypeInfo() {
        if (code == null) {
            throw new NullPointerException("service type code");
        }
        if (StringUtils.isEmpty(name)) {
            throw new IllegalArgumentException("service type name must not be empty");
        }
        ServiceTypeBuilder builder;
        if (StringUtils.isEmpty(desc)) {
            builder = new ServiceTypeBuilder(code, name);
        } else {
            builder = new ServiceTypeBuilder(code, name, desc);
        }
        if (property != null) {
            builder.terminal(property.isTerminal());
            builder.queue(property.isQueue());
            builder.recordStatistics(property.isRecordStatistics());
            builder.includeDestinationId(property.isIncludeDestinationId());
            builder.alias(property.isAlias());
        }
        ServiceType serviceType = builder.build();

        if (matcher == null) {
            return new DefaultServiceTypeInfo(serviceType);
        }
        AnnotationKeyMatcher annotationKeyMatcher = matcher.toAnnotationKeyMatcher();
        return new DefaultServiceTypeInfo(serviceType, annotationKeyMatcher);
    }
}
