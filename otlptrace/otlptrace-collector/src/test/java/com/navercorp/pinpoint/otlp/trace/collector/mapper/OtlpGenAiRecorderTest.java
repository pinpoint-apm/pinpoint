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
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class OtlpGenAiRecorderTest {

    private final List<AnnotationBo> annotations = new ArrayList<>();
    private final Set<String> consumedKeys = new HashSet<>();

    // GenAI context marker for bare-key fixtures — Claude Code always carries it
    private static final String GEN_AI_SYSTEM = "gen_ai.system";

    private void record(Map<String, AttributeValue> attributes) {
        OtlpGenAiRecorder.record(annotations::add, attributes, consumedKeys);
    }

    private String annotationValue(AnnotationKey key) {
        return annotations.stream()
                .filter(a -> a.getKey() == key.getCode())
                .map(a -> String.valueOf(a.getValue()))
                .findFirst()
                .orElse(null);
    }

    // =======================================================================
    // model — response.model over request.model
    // =======================================================================

    @Test
    void model_semconvResponseModelWins() {
        record(Map.of(
                OtlpTraceConstants.ATTRIBUTE_KEY_GEN_AI_RESPONSE_MODEL, AttributeValue.of("claude-sonnet-4-5-20250929"),
                OtlpTraceConstants.ATTRIBUTE_KEY_GEN_AI_REQUEST_MODEL, AttributeValue.of("claude-sonnet-4-5")));

        assertThat(annotationValue(AnnotationKey.GEN_AI_MODEL)).isEqualTo("claude-sonnet-4-5-20250929");
        // only the winning key is consumed — the losing request.model survives as a raw attribute
        assertThat(consumedKeys).containsExactly(OtlpTraceConstants.ATTRIBUTE_KEY_GEN_AI_RESPONSE_MODEL);
    }

    @Test
    void model_requestModelFallback() {
        record(Map.of(
                OtlpTraceConstants.ATTRIBUTE_KEY_GEN_AI_REQUEST_MODEL, AttributeValue.of("claude-sonnet-4-5")));

        assertThat(annotationValue(AnnotationKey.GEN_AI_MODEL)).isEqualTo("claude-sonnet-4-5");
        assertThat(consumedKeys).containsExactly(OtlpTraceConstants.ATTRIBUTE_KEY_GEN_AI_REQUEST_MODEL);
    }

    @Test
    void model_emptyStringNotPromoted() {
        record(Map.of(
                OtlpTraceConstants.ATTRIBUTE_KEY_GEN_AI_REQUEST_MODEL, AttributeValue.of("")));

        assertThat(annotations).isEmpty();
        assertThat(consumedKeys).isEmpty();
    }

    // =======================================================================
    // usage — key ladder and composition
    // =======================================================================

    @Test
    void usage_claudeCodeBareKeys() {
        // Claude Code shape: bare nonstandard token keys on a span that always carries
        // gen_ai.system (the GenAI context that admits the bare ladder steps)
        record(Map.of(
                GEN_AI_SYSTEM, AttributeValue.of("anthropic"),
                OtlpTraceConstants.ATTRIBUTE_KEY_INPUT_TOKENS, AttributeValue.of(1200L),
                OtlpTraceConstants.ATTRIBUTE_KEY_OUTPUT_TOKENS, AttributeValue.of(340L),
                OtlpTraceConstants.ATTRIBUTE_KEY_CACHE_READ_TOKENS, AttributeValue.of(5000L),
                OtlpTraceConstants.ATTRIBUTE_KEY_CACHE_CREATION_TOKENS, AttributeValue.of(0L)));

        // zero is a legitimate value and stays visible
        assertThat(annotationValue(AnnotationKey.GEN_AI_USAGE)).isEqualTo("in:1200 out:340 cache_r:5000 cache_w:0");
        assertThat(consumedKeys).containsExactlyInAnyOrder(
                OtlpTraceConstants.ATTRIBUTE_KEY_INPUT_TOKENS,
                OtlpTraceConstants.ATTRIBUTE_KEY_OUTPUT_TOKENS,
                OtlpTraceConstants.ATTRIBUTE_KEY_CACHE_READ_TOKENS,
                OtlpTraceConstants.ATTRIBUTE_KEY_CACHE_CREATION_TOKENS);
    }

    @Test
    void usage_semconvKeysWinOverBare() {
        record(Map.of(
                OtlpTraceConstants.ATTRIBUTE_KEY_GEN_AI_USAGE_INPUT_TOKENS, AttributeValue.of(100L),
                OtlpTraceConstants.ATTRIBUTE_KEY_INPUT_TOKENS, AttributeValue.of(999L)));

        assertThat(annotationValue(AnnotationKey.GEN_AI_USAGE)).isEqualTo("in:100");
        assertThat(consumedKeys).containsExactly(OtlpTraceConstants.ATTRIBUTE_KEY_GEN_AI_USAGE_INPUT_TOKENS);
    }

    @Test
    void usage_preRenameSemconvLadder() {
        // gen_ai.usage.prompt_tokens / completion_tokens — semconv names before the input/output rename
        record(Map.of(
                OtlpTraceConstants.ATTRIBUTE_KEY_GEN_AI_USAGE_PROMPT_TOKENS, AttributeValue.of(70L),
                OtlpTraceConstants.ATTRIBUTE_KEY_GEN_AI_USAGE_COMPLETION_TOKENS, AttributeValue.of(30L)));

        assertThat(annotationValue(AnnotationKey.GEN_AI_USAGE)).isEqualTo("in:70 out:30");
    }

    @Test
    void usage_partialPartsOnly() {
        record(Map.of(
                GEN_AI_SYSTEM, AttributeValue.of("anthropic"),
                OtlpTraceConstants.ATTRIBUTE_KEY_OUTPUT_TOKENS, AttributeValue.of(42L)));

        assertThat(annotationValue(AnnotationKey.GEN_AI_USAGE)).isEqualTo("out:42");
    }

    @Test
    void usage_stringTypedTokenNotPromoted() {
        // a token carried as a string value cannot be promoted and must survive as a raw attribute
        record(Map.of(
                GEN_AI_SYSTEM, AttributeValue.of("anthropic"),
                OtlpTraceConstants.ATTRIBUTE_KEY_INPUT_TOKENS, AttributeValue.of("1200")));

        assertThat(annotations).isEmpty();
        assertThat(consumedKeys).isEmpty();
    }

    @Test
    void usage_bareKeysWithoutGenAiContextNotPromoted() {
        // bare names are generic — without any gen_ai.* attribute they are not LLM evidence
        record(Map.of(
                OtlpTraceConstants.ATTRIBUTE_KEY_INPUT_TOKENS, AttributeValue.of(1200L),
                OtlpTraceConstants.ATTRIBUTE_KEY_CACHE_READ_TOKENS, AttributeValue.of(5000L),
                OtlpTraceConstants.ATTRIBUTE_KEY_TTFT_MS, AttributeValue.of(2659L)));

        assertThat(annotations).isEmpty();
        assertThat(consumedKeys).isEmpty();
    }

    @Test
    void usage_totalOnlyFallback() {
        // some SDKs report only the total — without it the whole usage line would be lost
        record(Map.of(
                OtlpTraceConstants.ATTRIBUTE_KEY_GEN_AI_USAGE_TOTAL_TOKENS, AttributeValue.of(410L)));

        assertThat(annotationValue(AnnotationKey.GEN_AI_USAGE)).isEqualTo("tot:410");
        assertThat(consumedKeys).containsExactly(OtlpTraceConstants.ATTRIBUTE_KEY_GEN_AI_USAGE_TOTAL_TOKENS);
    }

    @Test
    void usage_totalNextToInputOutputStaysRaw() {
        // next to a resolved half the total is a derived duplicate — not consumed, survives raw
        record(Map.of(
                OtlpTraceConstants.ATTRIBUTE_KEY_GEN_AI_USAGE_INPUT_TOKENS, AttributeValue.of(100L),
                OtlpTraceConstants.ATTRIBUTE_KEY_GEN_AI_USAGE_OUTPUT_TOKENS, AttributeValue.of(50L),
                OtlpTraceConstants.ATTRIBUTE_KEY_GEN_AI_USAGE_TOTAL_TOKENS, AttributeValue.of(150L)));

        assertThat(annotationValue(AnnotationKey.GEN_AI_USAGE)).isEqualTo("in:100 out:50");
        assertThat(consumedKeys).doesNotContain(OtlpTraceConstants.ATTRIBUTE_KEY_GEN_AI_USAGE_TOTAL_TOKENS);
    }

    // =======================================================================
    // ttft — time to first token (bare ttft_ms only; no semconv span attribute exists)
    // =======================================================================

    @Test
    void ttft_promotedWithMillisUnit() {
        record(Map.of(
                GEN_AI_SYSTEM, AttributeValue.of("anthropic"),
                OtlpTraceConstants.ATTRIBUTE_KEY_TTFT_MS, AttributeValue.of(2659L)));

        assertThat(annotationValue(AnnotationKey.GEN_AI_TTFT)).isEqualTo("2659 ms");
        assertThat(consumedKeys).containsExactly(OtlpTraceConstants.ATTRIBUTE_KEY_TTFT_MS);
    }

    @Test
    void ttft_zeroIsPromoted() {
        record(Map.of(
                GEN_AI_SYSTEM, AttributeValue.of("anthropic"),
                OtlpTraceConstants.ATTRIBUTE_KEY_TTFT_MS, AttributeValue.of(0L)));

        assertThat(annotationValue(AnnotationKey.GEN_AI_TTFT)).isEqualTo("0 ms");
    }

    @Test
    void ttft_stringTypedNotPromoted() {
        record(Map.of(
                GEN_AI_SYSTEM, AttributeValue.of("anthropic"),
                OtlpTraceConstants.ATTRIBUTE_KEY_TTFT_MS, AttributeValue.of("2659")));

        assertThat(annotations).isEmpty();
        assertThat(consumedKeys).isEmpty();
    }

    @Test
    void ttft_keptOutOfUsageLine() {
        record(Map.of(
                GEN_AI_SYSTEM, AttributeValue.of("anthropic"),
                OtlpTraceConstants.ATTRIBUTE_KEY_INPUT_TOKENS, AttributeValue.of(3L),
                OtlpTraceConstants.ATTRIBUTE_KEY_TTFT_MS, AttributeValue.of(2659L)));

        assertThat(annotationValue(AnnotationKey.GEN_AI_USAGE)).isEqualTo("in:3");
        assertThat(annotationValue(AnnotationKey.GEN_AI_TTFT)).isEqualTo("2659 ms");
    }

    // =======================================================================
    // no-op on non-GenAI spans
    // =======================================================================

    @Test
    void noOp_whenNoGenAiAttributes() {
        record(Map.of(
                "http.method", AttributeValue.of("GET")));

        assertThat(annotations).isEmpty();
        assertThat(consumedKeys).isEmpty();
    }

    @Test
    void modelAndUsageTogether() {
        record(Map.of(
                OtlpTraceConstants.ATTRIBUTE_KEY_GEN_AI_REQUEST_MODEL, AttributeValue.of("claude-sonnet-4-5"),
                OtlpTraceConstants.ATTRIBUTE_KEY_INPUT_TOKENS, AttributeValue.of(3L),
                OtlpTraceConstants.ATTRIBUTE_KEY_OUTPUT_TOKENS, AttributeValue.of(7L)));

        assertThat(annotationValue(AnnotationKey.GEN_AI_MODEL)).isEqualTo("claude-sonnet-4-5");
        assertThat(annotationValue(AnnotationKey.GEN_AI_USAGE)).isEqualTo("in:3 out:7");
        assertThat(annotations).hasSize(2);
    }
}
