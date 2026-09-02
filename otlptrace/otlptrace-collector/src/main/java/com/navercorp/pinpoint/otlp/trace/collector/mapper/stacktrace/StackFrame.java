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

package com.navercorp.pinpoint.otlp.trace.collector.mapper.stacktrace;

/**
 * One parsed stack frame, language-neutral. {@code className} and {@code methodName} must be
 * non-empty (the storage model rejects empty values); {@code fileName} may be empty when the
 * source format carries no file info. {@code lineNumber} follows the Java convention:
 * {@code -1} = unknown, {@code -2} = native method.
 */
public record StackFrame(String className, String fileName, int lineNumber, String methodName) {
}
