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
import java.util.List;

/**
 * @author intr3p1d
 */
public class ExceptionWrapperFactory {
    private final int maxDepth;
    private final int maxErrorMessageLength;

    public ExceptionWrapperFactory(int maxDepth, int maxErrorMessageLength) {
        this.maxDepth = maxDepth;
        this.maxErrorMessageLength = maxErrorMessageLength;
    }

    public List<ExceptionWrapper> newExceptionWrappers(ExceptionContext context) {
        if (context == null) {
            return null;
        }
        return traverseAndWrap(
                context.getContextValue(), context.getExceptionId()
        );
    }

    private List<ExceptionWrapper> traverseAndWrap(ExceptionContextValue topExceptionContextValue, long exceptionId) {
        List<ExceptionWrapper> exceptionWrappers = new ArrayList<>();
        int depth = 0;

        for (ExceptionContextValue curr = topExceptionContextValue;
             curr.getPrevious() != null;
             curr = curr.getPrevious()) {
            int newDepth = addAllExceptionWrappers(
                    exceptionWrappers,
                    curr.getThrowable(), curr.getPrevious().getThrowable(),
                    curr.getStartTime(), exceptionId,
                    depth
            );
            depth = newDepth;
        }

        return exceptionWrappers;
    }

    public int addAllExceptionWrappers(
            List<ExceptionWrapper> exceptionWrappers,
            Throwable current, Throwable next,
            long startTime, long exceptionId,
            int depthOffset
    ) {
        if (current == null) {
            return depthOffset;
        }
        Throwable curr = current;
        int depth = depthOffset;
        while (
                curr != null
                        && (maxDepth == 0 || depth < maxDepth)
                        && curr != next
        ) {
            exceptionWrappers.add(
                    ExceptionWrapper.newException(
                            curr, startTime, exceptionId, depth, maxErrorMessageLength
                    )
            );
            curr = curr.getCause();
            depth++;
        }
        return depth;
    }
}
