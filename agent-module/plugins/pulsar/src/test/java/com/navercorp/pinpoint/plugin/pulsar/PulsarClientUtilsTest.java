/*
 * Copyright 2026 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.pulsar;

import com.navercorp.pinpoint.plugin.pulsar.field.accessor.ClientConfigAccessor;
import com.navercorp.pinpoint.plugin.pulsar.field.getter.ServiceUrlGetter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PulsarClientUtilsTest {

    @Test
    public void getServiceUrl() {
        final ServiceUrlGetter clientConfig = () -> "pulsar://localhost:6650";
        Assertions.assertEquals("pulsar://localhost:6650", PulsarClientUtils.getServiceUrl(new Holder(clientConfig)));
    }

    @Test
    public void getServiceUrl_unknownTarget() {
        Assertions.assertNull(PulsarClientUtils.getServiceUrl(null));
        Assertions.assertNull(PulsarClientUtils.getServiceUrl(new Object()));
        Assertions.assertNull(PulsarClientUtils.getServiceUrl(new Holder(null)));
        Assertions.assertNull(PulsarClientUtils.getServiceUrl(new Holder(new Object())));
    }

    @Test
    public void getPartitionIndex() {
        Assertions.assertEquals(0, PulsarClientUtils.getPartitionIndex("persistent://public/default/topic-partition-0"));
        Assertions.assertEquals(12, PulsarClientUtils.getPartitionIndex("persistent://public/default/topic-partition-12"));
        Assertions.assertEquals(3, PulsarClientUtils.getPartitionIndex("topic-partition-1-partition-3"));
    }

    @Test
    public void getPartitionIndex_notPartitioned() {
        Assertions.assertEquals(-1, PulsarClientUtils.getPartitionIndex(null));
        Assertions.assertEquals(-1, PulsarClientUtils.getPartitionIndex("persistent://public/default/topic"));
        Assertions.assertEquals(-1, PulsarClientUtils.getPartitionIndex("topic-partition-"));
        Assertions.assertEquals(-1, PulsarClientUtils.getPartitionIndex("topic-partition-01"));
        Assertions.assertEquals(-1, PulsarClientUtils.getPartitionIndex("topic-partition--1"));
        Assertions.assertEquals(-1, PulsarClientUtils.getPartitionIndex("topic-partition-a"));
    }

    @Test
    public void getPackageName() {
        Assertions.assertEquals("org.apache.pulsar.client.impl",
                PulsarClientUtils.getPackageName("org.apache.pulsar.client.impl.ProducerImpl"));
        Assertions.assertEquals("org.apache.pulsar.shade.org.apache.pulsar.client.impl",
                PulsarClientUtils.getPackageName("org.apache.pulsar.shade.org.apache.pulsar.client.impl.ProducerImpl"));
        Assertions.assertNull(PulsarClientUtils.getPackageName("ProducerImpl"));
        Assertions.assertNull(PulsarClientUtils.getPackageName(null));
    }

    private static class Holder implements ClientConfigAccessor {
        private Object clientConfig;

        private Holder(Object clientConfig) {
            this.clientConfig = clientConfig;
        }

        @Override
        public void _$PINPOINT$_setClientConfig(Object clientConfig) {
            this.clientConfig = clientConfig;
        }

        @Override
        public Object _$PINPOINT$_getClientConfig() {
            return clientConfig;
        }
    }
}
