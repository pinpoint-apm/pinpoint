package com.navercorp.pinpoint.otlp.trace.collector.mapper;

import com.navercorp.pinpoint.common.server.bo.AttributeBo;
import com.navercorp.pinpoint.common.trace.attribute.AttributeKeyValue;
import com.navercorp.pinpoint.common.trace.attribute.AttributeValue;
import com.navercorp.pinpoint.common.trace.attribute.AttributeValueType;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OtlpAttributeBoMapperTest {

    // large enough that no test value is truncated
    private static final int NO_TRUNCATION_MAX_BYTES = 8192;

    @Test
    void negativeMaxBytes_rejected() {
        assertThatThrownBy(() -> new OtlpAttributeBoMapper(-1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // =======================================================================
    // filtering
    // =======================================================================

    @Test
    void toAttributeBoList_emptyMap_returnsEmptyList() {
        OtlpAttributeBoMapper mapper = new OtlpAttributeBoMapper(NO_TRUNCATION_MAX_BYTES);

        List<AttributeBo> result = mapper.toAttributeBoList(Map.of(), key -> false, new TruncationCounter());

        assertThat(result).isEmpty();
    }

    @Test
    void toAttributeBoList_noFilter_returnsAllEntries() {
        OtlpAttributeBoMapper mapper = new OtlpAttributeBoMapper(NO_TRUNCATION_MAX_BYTES);
        Map<String, AttributeValue> attrs = new HashMap<>();
        attrs.put("k1", AttributeValue.of("v1"));
        attrs.put("k2", AttributeValue.of(42L));

        List<AttributeBo> result = mapper.toAttributeBoList(attrs, key -> false, new TruncationCounter());

        assertThat(result).hasSize(2);
        assertThat(result).extracting(AttributeBo::getKey).containsExactlyInAnyOrder("k1", "k2");
    }

    @Test
    void toAttributeBoList_excludeFilter_removesKeys() {
        OtlpAttributeBoMapper mapper = new OtlpAttributeBoMapper(NO_TRUNCATION_MAX_BYTES);
        Map<String, AttributeValue> attrs = new HashMap<>();
        attrs.put("keep", AttributeValue.of("ok"));
        attrs.put("drop", AttributeValue.of("bye"));

        List<AttributeBo> result = mapper.toAttributeBoList(attrs, key -> key.equals("drop"), new TruncationCounter());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getKey()).isEqualTo("keep");
        assertThat(result.get(0).getValue().getValue()).isEqualTo("ok");
    }

    // =======================================================================
    // single-pass truncation
    // =======================================================================

    private static List<AttributeBo> truncate(int maxBytes, TruncationCounter counter, AttributeValue value) {
        final Map<String, AttributeValue> attrs = new LinkedHashMap<>();
        attrs.put("k", value);
        return new OtlpAttributeBoMapper(maxBytes).toAttributeBoList(attrs, key -> false, counter);
    }

    @Test
    void truncateAttributeValues_emptyMap_returnsZero() {
        TruncationCounter counter = new TruncationCounter();

        List<AttributeBo> result = new OtlpAttributeBoMapper(8).toAttributeBoList(Map.of(), key -> false, counter);

        assertThat(result).isEmpty();
        assertThat(counter.truncatedCount()).isZero();
    }

    @Test
    void truncateAttributeValues_shortString_unchanged() {
        TruncationCounter counter = new TruncationCounter();

        List<AttributeBo> list = truncate(64, counter, AttributeValue.of("ok"));

        assertThat(counter.truncatedCount()).isZero();
        assertThat(list.get(0).getValue().getValue()).isEqualTo("ok");
    }

    @Test
    void truncateAttributeValues_overLongString_truncatedAndCounted() {
        TruncationCounter counter = new TruncationCounter();

        List<AttributeBo> list = truncate(10, counter, AttributeValue.of("a".repeat(100)));

        assertThat(counter.truncatedCount()).isEqualTo(1);
        AttributeValue value = list.get(0).getValue();
        assertThat(value.getType()).isEqualTo(AttributeValueType.STRING);
        assertThat((String) value.getValue()).isEqualTo("a".repeat(10));
    }

    @Test
    void truncateAttributeValues_numericAndBoolean_neverTruncated() {
        TruncationCounter counter = new TruncationCounter();
        Map<String, AttributeValue> attrs = new LinkedHashMap<>();
        attrs.put("i", AttributeValue.of(1234567890L));
        attrs.put("d", AttributeValue.of(3.14159d));
        attrs.put("b", AttributeValue.of(true));

        // maxBytes=1: would truncate any string, but numbers/booleans are exempt per OTel spec
        List<AttributeBo> list = new OtlpAttributeBoMapper(1).toAttributeBoList(attrs, key -> false, counter);

        assertThat(counter.truncatedCount()).isZero();
        assertThat(list.get(0).getValue().getValue()).isEqualTo(1234567890L);
        assertThat(list.get(1).getValue().getValue()).isEqualTo(3.14159d);
        assertThat(list.get(2).getValue().getValue()).isEqualTo(true);
    }

    @Test
    void truncateAttributeValues_overLongBytes_truncatedToMaxBytes() {
        byte[] payload = new byte[100];
        for (int i = 0; i < payload.length; i++) {
            payload[i] = (byte) i;
        }
        TruncationCounter counter = new TruncationCounter();

        List<AttributeBo> list = truncate(16, counter, AttributeValue.of(payload));

        assertThat(counter.truncatedCount()).isEqualTo(1);
        AttributeValue value = list.get(0).getValue();
        assertThat(value.getType()).isEqualTo(AttributeValueType.BYTES);
        assertThat((byte[]) value.getValue()).hasSize(16)
                .containsExactly(Arrays.copyOf(payload, 16));
    }

    @Test
    void truncateAttributeValues_bytesWithinLimit_unchanged() {
        byte[] payload = new byte[]{1, 2, 3};
        TruncationCounter counter = new TruncationCounter();

        List<AttributeBo> list = truncate(16, counter, AttributeValue.of(payload));

        assertThat(counter.truncatedCount()).isZero();
        assertThat((byte[]) list.get(0).getValue().getValue()).containsExactly(1, 2, 3);
    }

    @Test
    void truncateAttributeValues_array_recursesAndCountsEachLeaf() {
        AttributeValue array = AttributeValue.of(
                AttributeValue.of("a".repeat(100)),
                AttributeValue.of("short"),
                AttributeValue.of("b".repeat(100))
        );
        TruncationCounter counter = new TruncationCounter();

        List<AttributeBo> list = truncate(10, counter, array);

        // two over-long leaves truncated, the "short" one untouched
        assertThat(counter.truncatedCount()).isEqualTo(2);
        @SuppressWarnings("unchecked")
        List<AttributeValue> result = (List<AttributeValue>) list.get(0).getValue().getValue();
        assertThat(result).extracting(AttributeValue::getValue)
                .containsExactly("a".repeat(10), "short", "b".repeat(10));
    }

    @Test
    void truncateAttributeValues_array_noOverLongLeaf_unchanged() {
        AttributeValue array = AttributeValue.of(AttributeValue.of("x"), AttributeValue.of("y"));
        TruncationCounter counter = new TruncationCounter();

        List<AttributeBo> list = truncate(10, counter, array);

        assertThat(counter.truncatedCount()).isZero();
        @SuppressWarnings("unchecked")
        List<AttributeValue> result = (List<AttributeValue>) list.get(0).getValue().getValue();
        assertThat(result).extracting(AttributeValue::getValue).containsExactly("x", "y");
    }

    @Test
    void truncateAttributeValues_keyValueList_recurses() {
        AttributeValue kvList = AttributeValue.ofAttributeKeyValueList(
                AttributeKeyValue.of("big", AttributeValue.of("a".repeat(100))),
                AttributeKeyValue.of("small", AttributeValue.of("ok"))
        );
        TruncationCounter counter = new TruncationCounter();

        List<AttributeBo> list = truncate(10, counter, kvList);

        assertThat(counter.truncatedCount()).isEqualTo(1);
        @SuppressWarnings("unchecked")
        List<AttributeKeyValue> result = (List<AttributeKeyValue>) list.get(0).getValue().getValue();
        assertThat(result.get(0).getKey()).isEqualTo("big");
        assertThat(result.get(0).getValue().getValue()).isEqualTo("a".repeat(10));
        assertThat(result.get(1).getValue().getValue()).isEqualTo("ok");
    }

    @Test
    void truncateAttributeValues_nestedArrayInsideKeyValueList_recursesDeeply() {
        AttributeValue kvList = AttributeValue.ofAttributeKeyValueList(
                AttributeKeyValue.of("tags", AttributeValue.of(
                        AttributeValue.of("a".repeat(100)),
                        AttributeValue.of("b".repeat(100))))
        );
        TruncationCounter counter = new TruncationCounter();

        List<AttributeBo> list = truncate(10, counter, kvList);

        assertThat(counter.truncatedCount()).isEqualTo(2);
        @SuppressWarnings("unchecked")
        List<AttributeKeyValue> outer = (List<AttributeKeyValue>) list.get(0).getValue().getValue();
        @SuppressWarnings("unchecked")
        List<AttributeValue> inner = (List<AttributeValue>) outer.get(0).getValue().getValue();
        assertThat(inner).extracting(AttributeValue::getValue)
                .containsExactly("a".repeat(10), "b".repeat(10));
    }

    @Test
    void truncateAttributeValues_multipleBos_sumsTruncations() {
        TruncationCounter counter = new TruncationCounter();
        Map<String, AttributeValue> attrs = new LinkedHashMap<>();
        attrs.put("a", AttributeValue.of("a".repeat(100)));
        attrs.put("b", AttributeValue.of("ok"));
        attrs.put("c", AttributeValue.of("c".repeat(100)));

        List<AttributeBo> list = new OtlpAttributeBoMapper(10).toAttributeBoList(attrs, key -> false, counter);

        assertThat(counter.truncatedCount()).isEqualTo(2);
        assertThat(list.get(0).getValue().getValue()).isEqualTo("a".repeat(10));
        assertThat(list.get(1).getValue().getValue()).isEqualTo("ok");
        assertThat(list.get(2).getValue().getValue()).isEqualTo("c".repeat(10));
    }
}
