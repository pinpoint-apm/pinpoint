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
package com.navercorp.pinpoint.profiler.context.exception.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author intr3p1d
 */
public class ExceptionWrapperFactory {
    private final int maxDepth;

    public ExceptionWrapperFactory(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    public List<ExceptionWrapper> newExceptionWrappers(ExceptionContext context) {
        if (context == null) {
            return null;
        }
        return newExceptionWrappers(context.getPrevious(), context.getStartTime(), context.getExceptionId());
    }

    public List<ExceptionWrapper> newExceptionWrappers(Throwable throwable, long startTime, long exceptionId) {
        if (throwable == null) {
            return Collections.emptyList();
        }
        List<ExceptionWrapper> exceptionWrappers = new ArrayList<>();
        Throwable curr = throwable;
        int depth = 0;
        while (curr != null && (maxDepth == 0 || depth < maxDepth)) {
            exceptionWrappers.add(ExceptionWrapper.newException(curr, startTime, exceptionId, depth));
            curr = curr.getCause();
            depth++;
        }
        return exceptionWrappers;
    }
}
