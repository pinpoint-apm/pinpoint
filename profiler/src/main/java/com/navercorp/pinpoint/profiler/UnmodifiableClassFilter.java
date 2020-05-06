/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.profiler;

import java.security.ProtectionDomain;

/**
 * @author emeroad
 */
public class UnmodifiableClassFilter implements ClassFileFilter {
    private static final String COMPLETABLE_FUTURE = "java/util/concurrent/CompletableFuture";
    private static final String PROCESS_BUILDER = "java/lang/ProcessBuilder";

    public UnmodifiableClassFilter() {
    }

    @Override
    public boolean accept(ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classFileBuffer) {
        if (className == null) {
            return SKIP;
        }

        // fast skip java classes
        if (className.startsWith("java")) {
            if (className.startsWith("/", 4) || className.startsWith("x/", 4)) {
                if (isCompletableFutureClass(className)) {
                    return CONTINUE;
                }
                if (isProcessBuilder(className)) {
                    return CONTINUE;
                }
                return SKIP;
            }
        }

        return CONTINUE;
    }

    private static boolean isCompletableFutureClass(final String className) {
        // Check java/util/concurrent/CompletableFuture
        if (!className.startsWith("u", 5) || !className.startsWith("/C", 20)) {
            return false;
        }
        return className.equals(COMPLETABLE_FUTURE);
    }

    private static boolean isProcessBuilder(final String className) {
        // Check java/lang/ProcessBuilder
        if (!className.startsWith("l", 5) || !className.startsWith("/P", 9)) {
            return false;
        }
        return className.equals(PROCESS_BUILDER);
    }
}