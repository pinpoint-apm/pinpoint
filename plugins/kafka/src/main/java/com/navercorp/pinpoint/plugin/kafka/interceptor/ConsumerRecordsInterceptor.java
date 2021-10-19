/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.kafka.interceptor;

import com.navercorp.pinpoint.bootstrap.context.Header;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.plugin.kafka.KafkaClientUtils;
import com.navercorp.pinpoint.plugin.kafka.KafkaConfig;
import com.navercorp.pinpoint.plugin.kafka.KafkaConstants;
import com.navercorp.pinpoint.plugin.kafka.field.accessor.EndPointFieldAccessor;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Taejin Koo
 * @author yjqg6666
 */
public class ConsumerRecordsInterceptor implements AroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();
    private final List<String> filterTagList;
    private final boolean recordSupportHeaders;
    private static final String NULL_KEY = "NULL";

    public ConsumerRecordsInterceptor(TraceContext traceContext) {
        KafkaConfig config = new KafkaConfig(traceContext.getProfilerConfig());
        this.filterTagList = config.getConsumerFilterTagList();
        this.recordSupportHeaders = KafkaClientUtils.isClientSupportHeader();
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }

        if (ArrayUtils.getLength(args) != 1) {
            return;
        }
        if (!(args[0] instanceof Map)) {
            return;
        }

        Map consumerRecordsMap = (Map) args[0];
        Set<Map.Entry> entrySet = consumerRecordsMap.entrySet();
        for (Map.Entry entry : entrySet) {
            if (entry == null) {
                continue;
            }

            final String endPoint = getEndPoint(entry.getKey());
            if (StringUtils.hasText(endPoint)) {
                Object value = entry.getValue();
                if (value instanceof List) {
                    List consumerRecordList = (List) value;
                    for (Object endPointFieldAccessor : consumerRecordList) {
                        if (endPointFieldAccessor instanceof EndPointFieldAccessor) {
                            ((EndPointFieldAccessor) endPointFieldAccessor)._$PINPOINT$_setEndPoint(endPoint);
                        }
                    }
                }
            }
        }
        if (recordSupportHeaders) {
            //noinspection unchecked
            filterConsumerRecords(consumerRecordsMap);
        }
    }

    private String getEndPoint(Object endPointFieldAccessor) {
        if (endPointFieldAccessor instanceof EndPointFieldAccessor) {
            return ((EndPointFieldAccessor) endPointFieldAccessor)._$PINPOINT$_getEndPoint();
        }

        return null;
    }

    /**
     * filter consumer records as the following strategy:
     * <ul>
     *   <li>1. If a message have one or more tags, and on the consumer side
     *      <ul>
     *          <li>1.1. if no tag filtering, include the message.</li>
     *          <li>1.2. if tag filtering enabled, but no common tag(s), exclude/filter out the message.</li>
     *          <li>1.3. if tag filtering enabled and have common tag(s), include the message.</li>
     *      </ul>
     *   </li>
     *   <li>2. If a message have no tags, and on the consumer side
     *      <ul>
     *          <li>2.1. if no tag filtering, include the message.</li>
     *          <li>2.2. if tag filtering enabled, exclude/filter out the message.</li>
     *      </ul>
     *   </li>
     * </ul>
     * @param records consumer records map used to construct ConsumerRecords
     */
    private void filterConsumerRecords(Map<Object, Object> records) {
        final boolean emptyCheckTagList = CollectionUtils.isEmpty(filterTagList);
        for (Map.Entry<Object, Object> entry : records.entrySet()) {
            final Object value = entry.getValue();
            if (value instanceof List) {
                final List<?> recordList = (List<?>) value;
                List<Object> newList = new ArrayList<>(recordList.size());
                boolean changed = false;
                for (Object lKey : recordList) {
                    if (lKey instanceof ConsumerRecord) {
                        ConsumerRecord<?, ?> consumerRecord = (ConsumerRecord<?, ?>) lKey;
                        final org.apache.kafka.common.header.Headers headers = consumerRecord.headers();

                        final org.apache.kafka.common.header.Header tagHeader = headers.lastHeader(Header.HTTP_TAGS.toString());
                        if (tagHeader == null) {
                            if (emptyCheckTagList) {
                                newList.add(consumerRecord); //case 2.2, include message
                            } else {
                                logFilter(consumerRecord, true, 21, null);
                                changed = true; //case 2.1, exclude message
                            }
                            continue;
                        }
                        final String headerValue = new String(tagHeader.value(), KafkaConstants.DEFAULT_PINPOINT_HEADER_CHARSET);
                        final List<String> currentTagList = StringUtils.tokenizeToStringList(headerValue, ",");
                        if (emptyCheckTagList && !CollectionUtils.isEmpty(currentTagList)) {
                            logFilter(consumerRecord, false, 11, headerValue);
                            newList.add(consumerRecord); //case 1.1, include message
                        }
                        currentTagList.retainAll(filterTagList);
                        if (!currentTagList.isEmpty()) {
                            logFilter(consumerRecord, false, 13, headerValue);
                            newList.add(consumerRecord); //case 1.3, include message
                        } else {
                            logFilter(consumerRecord, true, 12, headerValue);
                            changed = true; //case 1.2, exclude message
                        }
                    }
                }
                if (changed) {
                    entry.setValue(newList);
                }
            }
        }
    }

    private void logFilter(ConsumerRecord<?, ?> consumerRecord, boolean excluded, int type, String tags) {
        if (!logger.isDebugEnabled()) {
            return;
        }
        final String topic = consumerRecord.topic();
        final int partition = consumerRecord.partition();
        final Object key = consumerRecord.key();
        final String keyStr = key != null ? key.toString() : NULL_KEY;
        final long offset = consumerRecord.offset();
        logger.debug("Kafka message received, filtered:{}, case:{}, topic:{}, partition:{}, offset:{}, key:{}, tags:{}", excluded, type, topic, partition, offset, keyStr, tags);
    }

}
