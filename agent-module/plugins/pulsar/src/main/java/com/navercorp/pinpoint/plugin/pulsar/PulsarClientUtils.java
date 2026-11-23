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

/**
 * The shaded {@code pulsar-client} 5.x relocates the implementation packages to
 * {@code org.apache.pulsar.shade.org.apache.pulsar.*}, while 4.x and the unshaded
 * {@code pulsar-client-original} keep {@code org.apache.pulsar.*}. Only {@code org.apache.pulsar.client.api}
 * has the same name everywhere, so the interceptors use that API and the accessors added by the plugin only.
 */
public final class PulsarClientUtils {

    public static final String PACKAGE = "org.apache.pulsar";
    public static final String SHADED_PACKAGE = "org.apache.pulsar.shade.org.apache.pulsar";

    static final String PARTITIONED_TOPIC_SUFFIX = "-partition-";

    private PulsarClientUtils() {
    }

    /**
     * @param target a client, producer, consumer or message builder holding the client configuration
     * @return the service URL of the client, or null
     */
    public static String getServiceUrl(Object target) {
        if (!(target instanceof ClientConfigAccessor)) {
            return null;
        }
        final Object clientConfig = ((ClientConfigAccessor) target)._$PINPOINT$_getClientConfig();
        if (clientConfig instanceof ServiceUrlGetter) {
            return ((ServiceUrlGetter) clientConfig)._$PINPOINT$_getServiceUrl();
        }
        return null;
    }

    /**
     * Same result as {@code org.apache.pulsar.common.naming.TopicName.getPartitionIndex(String)}.
     *
     * @return the partition index of a "...-partition-N" topic, otherwise -1
     */
    public static int getPartitionIndex(String topic) {
        if (topic == null) {
            return -1;
        }
        final int suffixIndex = topic.lastIndexOf(PARTITIONED_TOPIC_SUFFIX);
        if (suffixIndex == -1) {
            return -1;
        }
        final String index = topic.substring(suffixIndex + PARTITIONED_TOPIC_SUFFIX.length());
        try {
            final int partitionIndex = Integer.parseInt(index);
            // "topic-partition-01", "topic-partition--1" are not partitions
            if (partitionIndex < 0 || index.length() != String.valueOf(partitionIndex).length()) {
                return -1;
            }
            return partitionIndex;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * @return the package of the class, e.g. "org.apache.pulsar.client.impl", or null
     */
    public static String getPackageName(String className) {
        if (className == null) {
            return null;
        }
        final int index = className.lastIndexOf('.');
        if (index == -1) {
            return null;
        }
        return className.substring(0, index);
    }
}
