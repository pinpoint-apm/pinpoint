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

package com.navercorp.pinpoint.otlp.trace.collector.mapper;

import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.attribute.AttributeValue;
import com.navercorp.pinpoint.otlp.trace.collector.util.AttributeUtils;

import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Consumer;

/**
 * Promotes the GenAI (LLM call) semconv attributes of a span to first-class annotations,
 * so the model and token usage are readable on the call-tree row without opening the raw
 * attribute list.
 *
 * <ul>
 *   <li>{@code gen_ai.model} (409): {@code gen_ai.response.model} — the model that actually
 *       served the call, which may differ from the requested alias — over
 *       {@code gen_ai.request.model}.</li>
 *   <li>{@code gen_ai.usage} (410): a composed summary such as
 *       {@code "in:1200 out:340 cache_r:5000 cache_w:0"}. Only the parts present on the span
 *       are included; when no token attribute is present the annotation is omitted entirely.</li>
 *   <li>{@code gen_ai.ttft} (411): time to first token, e.g. {@code "2659 ms"} — the streaming
 *       latency half of the span duration (the rest is token generation). Sourced from the bare
 *       {@code ttft_ms} key; OTel semconv defines TTFT only as a metric, not a span attribute.</li>
 * </ul>
 *
 * <p>Each token part resolves through a key ladder, mirroring {@link OtlpGrpcStatusResolver}:
 * the current semconv key ({@code gen_ai.usage.input_tokens} / {@code ...output_tokens}), then
 * the pre-rename semconv key ({@code ...prompt_tokens} / {@code ...completion_tokens}), then the
 * bare nonstandard key emitted by Claude Code ({@code input_tokens} / {@code output_tokens}).
 * The cache pair ({@code cache_read_tokens} / {@code cache_creation_tokens}) exists only in the
 * bare form — there is no semconv equivalent.</p>
 *
 * <p>Only the key actually consumed by a promotion is added to {@code consumedKeys} (and thereby
 * excluded from the raw attribute list); a non-promoted variant — e.g. a request.model that lost
 * to response.model, or a token carried as a string value — survives as a raw attribute.</p>
 */
public final class OtlpGenAiRecorder {

    private static final String[] MODEL_KEYS = {
            OtlpTraceConstants.ATTRIBUTE_KEY_GEN_AI_RESPONSE_MODEL,
            OtlpTraceConstants.ATTRIBUTE_KEY_GEN_AI_REQUEST_MODEL,
    };
    private static final String[] INPUT_TOKEN_KEYS = {
            OtlpTraceConstants.ATTRIBUTE_KEY_GEN_AI_USAGE_INPUT_TOKENS,
            OtlpTraceConstants.ATTRIBUTE_KEY_GEN_AI_USAGE_PROMPT_TOKENS,
            OtlpTraceConstants.ATTRIBUTE_KEY_INPUT_TOKENS,
    };
    private static final String[] OUTPUT_TOKEN_KEYS = {
            OtlpTraceConstants.ATTRIBUTE_KEY_GEN_AI_USAGE_OUTPUT_TOKENS,
            OtlpTraceConstants.ATTRIBUTE_KEY_GEN_AI_USAGE_COMPLETION_TOKENS,
            OtlpTraceConstants.ATTRIBUTE_KEY_OUTPUT_TOKENS,
    };
    private static final String[] CACHE_READ_TOKEN_KEYS = {
            OtlpTraceConstants.ATTRIBUTE_KEY_CACHE_READ_TOKENS,
    };
    private static final String[] CACHE_CREATION_TOKEN_KEYS = {
            OtlpTraceConstants.ATTRIBUTE_KEY_CACHE_CREATION_TOKENS,
    };
    private static final String[] TTFT_KEYS = {
            OtlpTraceConstants.ATTRIBUTE_KEY_TTFT_MS,
    };

    private static final long ABSENT = -1;

    private OtlpGenAiRecorder() {
    }

    /**
     * Records the gen_ai.model / gen_ai.usage annotations when the corresponding attributes are
     * present; a span without GenAI attributes is a no-op.
     */
    public static void record(Consumer<AnnotationBo> sink, Map<String, AttributeValue> attributes, Set<String> consumedKeys) {
        final String model = firstString(attributes, consumedKeys, MODEL_KEYS);
        if (model != null) {
            sink.accept(AnnotationBo.of(AnnotationKey.GEN_AI_MODEL.getCode(), model));
        }
        final String usage = composeUsage(attributes, consumedKeys);
        if (usage != null) {
            sink.accept(AnnotationBo.of(AnnotationKey.GEN_AI_USAGE.getCode(), usage));
        }
        // Kept out of the usage line: usage is the token namespace and a time value inside it
        // would read as a token count.
        final long ttftMillis = firstLong(attributes, consumedKeys, TTFT_KEYS);
        if (ttftMillis != ABSENT) {
            sink.accept(AnnotationBo.of(AnnotationKey.GEN_AI_TTFT.getCode(), ttftMillis + " ms"));
        }
    }

    private static String composeUsage(Map<String, AttributeValue> attributes, Set<String> consumedKeys) {
        final long input = firstLong(attributes, consumedKeys, INPUT_TOKEN_KEYS);
        final long output = firstLong(attributes, consumedKeys, OUTPUT_TOKEN_KEYS);
        final long cacheRead = firstLong(attributes, consumedKeys, CACHE_READ_TOKEN_KEYS);
        final long cacheCreation = firstLong(attributes, consumedKeys, CACHE_CREATION_TOKEN_KEYS);
        if (input == ABSENT && output == ABSENT && cacheRead == ABSENT && cacheCreation == ABSENT) {
            return null;
        }
        final StringJoiner joiner = new StringJoiner(" ");
        appendPart(joiner, "in", input);
        appendPart(joiner, "out", output);
        appendPart(joiner, "cache_r", cacheRead);
        appendPart(joiner, "cache_w", cacheCreation);
        return joiner.toString();
    }

    private static void appendPart(StringJoiner joiner, String label, long value) {
        if (value != ABSENT) {
            joiner.add(label + ':' + value);
        }
    }

    private static String firstString(Map<String, AttributeValue> attributes, Set<String> consumedKeys, String[] keys) {
        for (String key : keys) {
            final String value = AttributeUtils.getAttributeStringValue(attributes, key, null);
            if (value != null && !value.isEmpty()) {
                consumedKeys.add(key);
                return value;
            }
        }
        return null;
    }

    private static long firstLong(Map<String, AttributeValue> attributes, Set<String> consumedKeys, String[] keys) {
        for (String key : keys) {
            final long value = AttributeUtils.getAttributeIntValue(attributes, key, ABSENT);
            if (value >= 0) {
                consumedKeys.add(key);
                return value;
            }
        }
        return ABSENT;
    }
}
