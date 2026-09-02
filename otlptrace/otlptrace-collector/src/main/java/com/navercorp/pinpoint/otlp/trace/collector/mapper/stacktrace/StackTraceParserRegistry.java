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

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Selects the stacktrace parser in two stages:
 * <ol>
 *   <li>the {@code telemetry.sdk.language} resource attribute (OTel spec values: java, nodejs,
 *       webjs, python, dotnet, go, ...) — deterministic when present;</li>
 *   <li>content sniffing over the stacktrace text, ordered from the most distinctive format to
 *       the least (Python's header, Go's goroutine dump, .NET's " in file:line N", V8's
 *       ":line:col" tail, then the JVM shape).</li>
 * </ol>
 * Unknown languages/formats fall back to {@link RawStackTraceParser}, which is also the caller's
 * second pass when a selected parser yields zero frames (e.g. a Ruby stack under an unmapped
 * language value).
 */
public final class StackTraceParserRegistry {

    private final Map<String, StackTraceParser> byLanguage;
    private final List<StackTraceParser> sniffOrder;
    private final StackTraceParser rawFallback = new RawStackTraceParser();

    public StackTraceParserRegistry() {
        final StackTraceParser java = new JavaStackTraceParser();
        final StackTraceParser node = new NodeStackTraceParser();
        final StackTraceParser python = new PythonStackTraceParser();
        final StackTraceParser dotNet = new DotNetStackTraceParser();
        final StackTraceParser go = new GoStackTraceParser();

        this.byLanguage = Map.of(
                "java", java,
                "nodejs", node,
                "webjs", node, // browser JS shares the V8 stack shape
                "python", python,
                "dotnet", dotNet,
                "go", go
        );
        this.sniffOrder = List.of(python, go, dotNet, node, java);
    }

    /**
     * @param sdkLanguage the {@code telemetry.sdk.language} resource attribute, or {@code null}
     */
    public StackTraceParser select(String sdkLanguage, String stackTrace) {
        if (sdkLanguage != null) {
            final StackTraceParser byAttr = byLanguage.get(sdkLanguage.trim().toLowerCase(Locale.ROOT));
            if (byAttr != null) {
                return byAttr;
            }
        }
        for (StackTraceParser parser : sniffOrder) {
            if (parser.matches(stackTrace)) {
                return parser;
            }
        }
        return rawFallback;
    }

    public StackTraceParser rawFallback() {
        return rawFallback;
    }
}
