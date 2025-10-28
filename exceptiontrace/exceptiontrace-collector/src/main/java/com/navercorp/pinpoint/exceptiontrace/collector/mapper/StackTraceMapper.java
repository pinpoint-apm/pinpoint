/*
 * Copyright 2025 NAVER Corp.
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
package com.navercorp.pinpoint.exceptiontrace.collector.mapper;

import com.navercorp.pinpoint.common.timeseries.array.IntArray;
import com.navercorp.pinpoint.exceptiontrace.common.model.StackTraceElementWrapper;
import org.mapstruct.Qualifier;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;

/**
 * @author intr3p1d
 */
@Component
public class StackTraceMapper {

    public StackTraceMapper() {
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
        List<String> list = new ArrayList<>(classNames.size());
        for (StackTraceElementWrapper className : classNames) {
            String name = className.getClassName();
            list.add(name);
        }
        return list;
    }

    @StackTraceToFileNames
    public List<String> stackTraceToFileNames(List<StackTraceElementWrapper> fileNames) {
        List<String> list = new ArrayList<>(fileNames.size());
        for (StackTraceElementWrapper fileName : fileNames) {
            String name = fileName.getFileName();
            list.add(name);
        }
        return list;
    }

    @StackTraceToLineNumbers
    public List<Integer> stackTraceToLineNumber(List<StackTraceElementWrapper> lineNumbers) {
        return IntArray.asList(lineNumbers, StackTraceElementWrapper::getLineNumber);
    }

    @StackTraceToMethodNames
    public List<String> stackTraceToMethodNames(List<StackTraceElementWrapper> methodNames) {
        List<String> list = new ArrayList<>(methodNames.size());
        for (StackTraceElementWrapper methodName : methodNames) {
            String name = methodName.getMethodName();
            list.add(name);
        }
        return list;
    }

}
