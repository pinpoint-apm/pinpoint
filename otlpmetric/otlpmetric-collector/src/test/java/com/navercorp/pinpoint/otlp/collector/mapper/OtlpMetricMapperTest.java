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
package com.navercorp.pinpoint.otlp.collector.mapper;

import com.navercorp.pinpoint.otlp.collector.model.OtlpMetricData;
import com.navercorp.pinpoint.otlp.collector.model.OtlpMetricDataPoint;
import com.navercorp.pinpoint.otlp.collector.model.OtlpResource;
import com.navercorp.pinpoint.otlp.collector.model.OtlpResourceAttributes;
import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.metrics.v1.Gauge;
import io.opentelemetry.proto.metrics.v1.Metric;
import io.opentelemetry.proto.metrics.v1.NumberDataPoint;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class OtlpMetricMapperTest {

    private final OtlpMetricMapper mapper = new OtlpMetricMapper(() -> "tenant", new OtlpMetricDataMapper[]{new GaugeMapper()});

    private Metric gauge() {
        NumberDataPoint point = NumberDataPoint.newBuilder()
                .setTimeUnixNano(1_000_000_000L)
                .setAsDouble(1.0)
                .build();
        return Metric.newBuilder()
                .setName("group.metric")
                .setGauge(Gauge.newBuilder().addDataPoints(point))
                .build();
    }

    private static KeyValue attribute(String key, String value) {
        return KeyValue.newBuilder().setKey(key).setValue(AnyValue.newBuilder().setStringValue(value)).build();
    }

    private static KeyValue attribute(String key, long value) {
        return KeyValue.newBuilder().setKey(key).setValue(AnyValue.newBuilder().setIntValue(value)).build();
    }

    private List<KeyValue> resourceAttributes() {
        List<KeyValue> attributes = new ArrayList<>();
        attributes.add(attribute(OtlpResourceAttributes.KEY_SERVICE_NAME, "applicationName"));
        attributes.add(attribute(OtlpResourceAttributes.KEY_PINPOINT_AGENTID, "agentId"));
        attributes.add(attribute("host", "h1"));
        return attributes;
    }

    private static List<KeyValue> without(List<KeyValue> attributes, String key) {
        List<KeyValue> result = new ArrayList<>(attributes);
        result.removeIf(attribute -> attribute.getKey().equals(key));
        return result;
    }

    @Test
    void mapResource() {
        List<KeyValue> attributes = resourceAttributes();
        attributes.add(attribute(OtlpResourceAttributes.KEY_SERVICE_NAMESPACE, "serviceName"));
        attributes.add(attribute(OtlpResourceAttributes.KEY_PINPOINT_METRIC_VERSION, "1"));

        OtlpResource resource = mapper.mapResource(attributes);

        assertNotNull(resource);
        assertEquals("tenant", resource.getTenantId());
        assertEquals("applicationName", resource.getServiceName());
        assertEquals("serviceName", resource.getServiceNamespace());
        assertEquals("agentId", resource.getAgentId());
        assertEquals("1", resource.getVersion());
        // attributes promoted to fields are not tags, the rest is
        assertEquals(Map.of("host", "h1"), resource.getTags());
    }

    @Test
    void mapResource_nonStringAttribute() {
        List<KeyValue> attributes = resourceAttributes();
        attributes.add(attribute("process.pid", 1234L));

        OtlpResource resource = mapper.mapResource(attributes);

        assertNotNull(resource);
        assertEquals("1234", resource.getTags().get("process.pid"));
    }

    @Test
    void map() {
        List<KeyValue> attributes = resourceAttributes();
        attributes.add(attribute(OtlpResourceAttributes.KEY_SERVICE_NAMESPACE, "serviceName"));
        OtlpResource resource = mapper.mapResource(attributes);

        OtlpMetricData data = mapper.map(gauge(), resource);

        assertNotNull(data);
        assertEquals("tenant", data.getTenantId());
        assertEquals("applicationName", data.getServiceName());
        assertEquals("serviceName", data.getServiceNamespace());
        assertEquals("agentId", data.getAgentId());
        assertEquals("", data.getVersion());

        OtlpMetricDataPoint point = data.getValues().get(0);
        assertEquals("h1", point.getTags().get("host"));
        assertFalse(point.getTags().containsKey(OtlpResourceAttributes.KEY_SERVICE_NAME));
        assertFalse(point.getTags().containsKey(OtlpResourceAttributes.KEY_SERVICE_NAMESPACE));
    }

    @Test
    void serviceNamespace_absent() {
        OtlpResource resource = mapper.mapResource(resourceAttributes());

        assertNotNull(resource);
        assertNull(resource.getServiceNamespace());
    }

    @Test
    void serviceNamespace_blank() {
        List<KeyValue> attributes = resourceAttributes();
        attributes.add(attribute(OtlpResourceAttributes.KEY_SERVICE_NAMESPACE, "  "));

        OtlpResource resource = mapper.mapResource(attributes);

        assertNotNull(resource);
        assertNull(resource.getServiceNamespace());
    }

    @Test
    void version_blank() {
        List<KeyValue> attributes = resourceAttributes();
        attributes.add(attribute(OtlpResourceAttributes.KEY_PINPOINT_METRIC_VERSION, "  "));

        OtlpResource resource = mapper.mapResource(attributes);

        assertNotNull(resource);
        assertEquals("", resource.getVersion());
        assertFalse(resource.getTags().containsKey(OtlpResourceAttributes.KEY_PINPOINT_METRIC_VERSION));
    }

    @Test
    void serviceName_required() {
        List<KeyValue> attributes = without(resourceAttributes(), OtlpResourceAttributes.KEY_SERVICE_NAME);
        assertNull(mapper.mapResource(attributes));

        attributes.add(attribute(OtlpResourceAttributes.KEY_SERVICE_NAME, "  "));
        assertNull(mapper.mapResource(attributes));
    }

    @Test
    void agentId_required() {
        List<KeyValue> attributes = without(resourceAttributes(), OtlpResourceAttributes.KEY_PINPOINT_AGENTID);
        assertNull(mapper.mapResource(attributes));

        attributes.add(attribute(OtlpResourceAttributes.KEY_PINPOINT_AGENTID, "  "));
        assertNull(mapper.mapResource(attributes));
    }
}
