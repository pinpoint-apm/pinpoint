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
package com.navercorp.pinpoint.exceptiontrace.collector.mapper;

import com.navercorp.pinpoint.common.server.mapper.MapStructUtils;
import com.navercorp.pinpoint.exceptiontrace.common.model.StackTraceElementWrapper;
import org.mapstruct.Qualifier;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
    public @interface StackTraceToClassNames {
    }

    @Qualifier
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.CLASS)
    public @interface StackTraceToFileNames {
    }

    @Qualifier
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.CLASS)
    public @interface StackTraceToLineNumbers {
    }

    @Qualifier
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.CLASS)
    public @interface StackTraceToMethodNames {
    }

    @StackTraceToClassNames
    public List<String> stackTraceToClassNames(List<StackTraceElementWrapper> classNames) {
        return classNames.stream()
                .map(StackTraceElementWrapper::getClassName)
                .collect(Collectors.toList());
    }

    @StackTraceToFileNames
    public List<String> stackTraceToFileNames(List<StackTraceElementWrapper> fileNames) {
        return fileNames.stream()
                .map(StackTraceElementWrapper::getFileName)
                .collect(Collectors.toList());
    }

    @StackTraceToLineNumbers
    public List<Integer> stackTraceToLineNumber(List<StackTraceElementWrapper> lineNumbers) {
        return lineNumbers.stream()
                .map(StackTraceElementWrapper::getLineNumber)
                .collect(Collectors.toList());
    }

    @StackTraceToMethodNames
    public List<String> stackTraceToMethodNames(List<StackTraceElementWrapper> methodNames) {
        return methodNames.stream()
                .map(StackTraceElementWrapper::getMethodName)
                .collect(Collectors.toList());
    }

}
