/*
 * Copyright 2023 NAVER Corp.
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
package com.navercorp.pinpoint.exceptiontrace.web.mapper;

import com.navercorp.pinpoint.common.server.mapper.MapStructUtils;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.exceptiontrace.common.model.StackTraceElementWrapper;
import com.navercorp.pinpoint.exceptiontrace.web.entity.ExceptionMetaDataEntity;
import org.mapstruct.Qualifier;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author intr3p1d
 */
@Component
public class StackTraceMapper {
    private final MapStructUtils mapStructUtils;

    public StackTraceMapper(MapStructUtils mapStructUtils) {
        this.mapStructUtils = Objects.requireNonNull(mapStructUtils, "mapStructUtils");
    }

    @Qualifier
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.CLASS)
    public @interface StringsToStackTrace {

    }

    @StringsToStackTrace
    public List<StackTraceElementWrapper> stackTrace(ExceptionMetaDataEntity entity) {
        List<String> classNameIterable = mapStructUtils.jsonStrToList(entity.getStackTraceClassName());
        List<String> fileNameIterable = mapStructUtils.jsonStrToList(entity.getStackTraceFileName());
        List<Integer> lineNumberIterable = mapStructUtils.jsonStrToList(entity.getStackTraceLineNumber());
        List<String> methodNameIterable = mapStructUtils.jsonStrToList(entity.getStackTraceMethodName());

        List<StackTraceElementWrapper> wrappers = new ArrayList<>();
        for (int i = 0; i < classNameIterable.size(); i++) {
            wrappers.add(
                    new StackTraceElementWrapper(
                            abbreviate(classNameIterable.get(i)),
                            abbreviate(fileNameIterable.get(i)),
                            lineNumberIterable.get(i),
                            abbreviate(methodNameIterable.get(i))
                    )
            );
        }
        return wrappers;
    }

    private static String abbreviate(String str) {
        return StringUtils.abbreviate(str, 2048);
    }
}
