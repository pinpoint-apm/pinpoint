/*
 * Copyright 2023 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.common.server.util;

import java.util.function.Function;
import java.util.stream.Stream;

public final class CallerUtils {

    private CallerUtils() {
    }

    public static final long DEFAULT_SKIP_FRAME = 1;
    public static final Function<Stream<StackWalker.StackFrame>, StackWalker.StackFrame> DEFAULT_CALLER_METHOD_NAME = new StackFrameFunction(DEFAULT_SKIP_FRAME);

    private static final StackWalker WALKER = StackWalker.getInstance();
    public static String getCallerMethodName() {
        // log4j
        // StackLocatorUtil.getStackTraceElement(1).getMethodName();
        StackWalker.StackFrame stackFrame = WALKER.walk(DEFAULT_CALLER_METHOD_NAME);
        return defaultMethodName(stackFrame);
    }

    public static String getCallerMethodName(long skip) {
        skip += DEFAULT_SKIP_FRAME;
        Function<Stream<StackWalker.StackFrame>, StackWalker.StackFrame> fun = new StackFrameFunction(skip);
        StackWalker.StackFrame stackFrame = WALKER.walk(fun);
        return defaultMethodName(stackFrame);
    }

    private static String defaultMethodName(StackWalker.StackFrame stackFrame) {
        if (stackFrame == null) {
            return null;
        }
        return stackFrame.getMethodName();
    }

    public static class StackFrameFunction implements Function<Stream<StackWalker.StackFrame>, StackWalker.StackFrame> {
        private final long skip;

        public StackFrameFunction(long skip) {
            this.skip = skip;
        }

        @Override
        public StackWalker.StackFrame apply(Stream<StackWalker.StackFrame> frames) {
            return frames
                    .skip(skip)
                    .findFirst()
                    .orElse(null);
        }
    }
}
