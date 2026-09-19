/*
 * Copyright 2026 NAVER Corp.
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
package com.navercorp.pinpoint.alarm.util;

import java.util.Objects;

public final class ExceptionMessageUtils {

    private ExceptionMessageUtils() {
    }

    /**
     * The message to show the owner of a rule that could not be evaluated.
     *
     * <p>Not the root cause: a check failure is reported to the rule's own notification
     * channels, and one of those is a webhook at an operator-supplied url, so whatever text
     * comes back is leaving the process. The deepest cause is a backend's -- a driver error
     * carrying a connection string, an error body from an upstream service -- and none of it
     * was written with that in mind.
     *
     * <p>What is returned instead is the message of the outermost IllegalArgumentException in
     * the chain, which is how this module signals a rule that cannot be evaluated as
     * configured: the validators, the effective-rule resolver and the evaluation job all
     * raise it, with messages written to be read. When there is none, only the exception's
     * type is named, because the failure is then something the rule's owner cannot act on
     * anyway. The root cause is still recorded in the history context, which stays inside.
     */
    public static String ruleOwnerMessage(Throwable failure) {
        Objects.requireNonNull(failure, "failure");
        for (Throwable current = failure; current != null; current = current.getCause()) {
            if (current instanceof IllegalArgumentException) {
                String message = current.getMessage();
                if (message != null && !message.isBlank()) {
                    return message;
                }
            }
        }
        return failure.getClass().getSimpleName();
    }

    public static String rootCauseMessage(Throwable failure) {
        Throwable root = Objects.requireNonNull(failure, "failure");
        for (Throwable current = failure; current != null; current = current.getCause()) {
            root = current;
        }
        String message = root.getMessage();
        return message == null || message.isBlank() ? root.getClass().getSimpleName() : message;
    }
}
