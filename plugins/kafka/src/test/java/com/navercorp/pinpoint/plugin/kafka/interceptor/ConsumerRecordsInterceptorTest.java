package com.navercorp.pinpoint.plugin.kafka.interceptor;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.plugin.kafka.KafkaConfig;
import com.navercorp.pinpoint.plugin.kafka.KafkaConfigTest;
import com.navercorp.pinpoint.plugin.kafka.KafkaConstants;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.common.record.TimestampType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import static org.mockito.Mockito.doReturn;

/**
 * @author yjqg6666
 */
@RunWith(MockitoJUnitRunner.class)
public class ConsumerRecordsInterceptorTest {

    private static final String TOPIC1 = "topic-test1";

    private static final String TOPIC2 = "topic-test2";

    @Mock
    private TraceContext traceContext;

    @Mock
    private ProfilerConfig profilerConfig;

    @Before
    public void setup() {
        doReturn(true).when(profilerConfig).readBoolean(KafkaConfigTest.PRODUCER_ENABLE, false);
        doReturn(true).when(profilerConfig).readBoolean(KafkaConfigTest.CONSUMER_ENABLE, false);
        doReturn(true).when(profilerConfig).readBoolean(KafkaConfigTest.SPRING_CONSUMER_ENABLE, false);
        doReturn(profilerConfig).when(traceContext).getProfilerConfig();
    }

    @Test
    public void enabled() {
        doReturn(true).when(profilerConfig).readBoolean(KafkaConfigTest.HEADER_ENABLE, true);
        KafkaConfig config = new KafkaConfig(profilerConfig);
        Assert.assertTrue("producer", config.isProducerEnable());
        Assert.assertTrue("consumer", config.isConsumerEnable());
        Assert.assertTrue("spring consumer", config.isSpringConsumerEnable());

        Assert.assertTrue("header enabled", config.isHeaderEnable());
    }


    /**
     * test case: 2.1
     */
    @Test
    public void consumeNoTagRecordNoFilter() {
        doReturn(Collections.emptyList()).when(profilerConfig).readList(KafkaConfigTest.CONSUMER_FILTER_TAGS);
        final int randomCount = new Random().nextInt(100) + 1;
        Assert.assertEquals("left count", randomCount, interceptRecordWithoutTagGotLeft(randomCount));
    }

    /**
     * test case: 2.2
     */
    @Test
    public void consumeNoTagRecordHaveFilter() {
        doReturn(Arrays.asList("test1", "test2")).when(profilerConfig).readList(KafkaConfigTest.CONSUMER_FILTER_TAGS);
        final int randomCount = new Random().nextInt(100) + 1;
        Assert.assertEquals("left count", 0, interceptRecordWithoutTagGotLeft(randomCount));
    }

    /**
     * test case: 1.1
     */
    @Test
    public void consumeTagRecordNoFilter() {
        doReturn(Collections.emptyList()).when(profilerConfig).readList(KafkaConfigTest.CONSUMER_FILTER_TAGS);
        final int randomCount = new Random().nextInt(100) + 1;
        final String messageTags = "testTag1,testTag2";
        Assert.assertEquals("left count", randomCount, interceptRecordWithTagGotLeft(messageTags, randomCount));

        final int randomTagCount = new Random().nextInt(100) + 1;
        final int randomNoTagCount = new Random().nextInt(100) + 1;
        Assert.assertEquals("mixed left count", randomTagCount+randomNoTagCount, interceptRecordWithTagGotLeft(messageTags, randomTagCount, randomNoTagCount));
    }

    /**
     * test case: 1.3
     */
    @Test
    public void consumeTagRecordHaveSameTagFilter() {
        final String messageTags = "testTag1,testTag2";
        doReturn(StringUtils.tokenizeToStringList(messageTags, ",")).when(profilerConfig).readList(KafkaConfigTest.CONSUMER_FILTER_TAGS);
        final int randomCount = new Random().nextInt(100) + 1;
        Assert.assertEquals("left count", randomCount, interceptRecordWithTagGotLeft(messageTags, randomCount));

        final int randomTagCount = new Random().nextInt(100) + 1;
        final int randomNoTagCount = new Random().nextInt(100) + 1;
        Assert.assertEquals("mixed left count", randomTagCount, interceptRecordWithTagGotLeft(messageTags, randomTagCount, randomNoTagCount));
    }

    /**
     * test case: 1.3
     */
    @Test
    public void consumeTagRecordHaveCommonTagFilter() {
        final String commonTag = "testTag1";
        final String messageTags = "testTag1,"+commonTag;
        doReturn(Arrays.asList(commonTag, "anotherTag")).when(profilerConfig).readList(KafkaConfigTest.CONSUMER_FILTER_TAGS);
        final int randomCount = new Random().nextInt(100) + 1;
        Assert.assertEquals("left count", randomCount, interceptRecordWithTagGotLeft(messageTags, randomCount));

        final int randomTagCount = new Random().nextInt(100) + 1;
        final int randomNoTagCount = new Random().nextInt(100) + 1;
        Assert.assertEquals("mixed left count", randomTagCount, interceptRecordWithTagGotLeft(messageTags, randomTagCount, randomNoTagCount));
    }

    /**
     * test case: 1.2
     */
    @Test
    public void consumeTagRecordNoCommonTagFilter() {
        final String messageTags = "testTag1,testTag2";
        doReturn(Arrays.asList("testTag3", "testTag4")).when(profilerConfig).readList(KafkaConfigTest.CONSUMER_FILTER_TAGS);
        final int randomCount = new Random().nextInt(100) + 1;
        Assert.assertEquals("left count", 0, interceptRecordWithTagGotLeft(messageTags, randomCount));

        final int randomTagCount = new Random().nextInt(100) + 1;
        final int randomNoTagCount = new Random().nextInt(100) + 1;
        Assert.assertEquals("mixed left count", 0, interceptRecordWithTagGotLeft(messageTags, randomTagCount, randomNoTagCount));
    }

    private int interceptRecordWithTagGotLeft(String tags, int tagCount, int noTagCount) {
        return generateFilterRecordGotLeft(tagCount, noTagCount, tags);
    }

    private int interceptRecordWithTagGotLeft(String tags, int total) {
        return generateFilterRecordGotLeft(total, 0, tags);
    }

    private int interceptRecordWithoutTagGotLeft(int total) {
        return generateFilterRecordGotLeft(0, total, null);
    }

    private int generateFilterRecordGotLeft(int tagCount, int noTagCount, String tags) {
        ConsumerRecordsInterceptor interceptor = new ConsumerRecordsInterceptor(traceContext);

        if (!StringUtils.hasText(tags)) {
            noTagCount += tagCount;
        }
        final Map<TopicPartition, List<ConsumerRecord<String, String>>> records = consumerRecords(tags, tagCount, noTagCount);
        ConsumerRecords<String, String> consumerRecords = new ConsumerRecords<>(records);
        Object[] args = new Object[]{records};
        interceptor.before(consumerRecords, args);
        interceptor.after(consumerRecords, args, null, null);
        return consumerRecords.count();
    }

    private Map<TopicPartition, List<ConsumerRecord<String, String>>> consumerRecords(String tags, int tagCount, int noTagCount) {
        Map<TopicPartition, List<ConsumerRecord<String, String>>> map = new HashMap<>(2);
        final Random random = new Random(System.currentTimeMillis());
        int partition1 = random.nextInt(100);
        int partition2 = random.nextInt(100);
        int tagCount1 = tagCount;
        int tagCount2 = 0;
        int noTagCount1 = noTagCount;
        int noTagCount2 = 0;
        if (tagCount > 0) {
            tagCount1 = random.nextInt(tagCount);
            tagCount2 = tagCount - tagCount1;
        }
        if (noTagCount > 0) {
            noTagCount1 = random.nextInt(noTagCount);
            noTagCount2 = noTagCount - noTagCount1;
        }
        TopicPartition tp1 = new TopicPartition(TOPIC1, partition1);
        TopicPartition tp2 = new TopicPartition(TOPIC2, partition2);
        final List<ConsumerRecord<String, String>> recordList1 = recordList(TOPIC1, partition1, tags, tagCount1, noTagCount1);
        final List<ConsumerRecord<String, String>> recordList2 = recordList(TOPIC2, partition2, tags, tagCount2, noTagCount2);
        map.put(tp1, recordList1);
        map.put(tp2, recordList2);
        return map;
    }

    private List<ConsumerRecord<String, String>> recordList(String topic, int partition, String tags, int tagCount, int noTagCount) {
        List<ConsumerRecord<String, String>> records = new ArrayList<>(tagCount + noTagCount);
        for (int i = 0; i < tagCount; i++) {
            if (StringUtils.hasText(tags)) {
                records.add(record("record" + i, tags, topic, partition, i));
            }
        }
        for (int i = 0; i < noTagCount; i++) {
            records.add(record("record" + i, topic, partition, i));
        }
        return records;
    }

    private ConsumerRecord<String, String> record(String value, String topic, int partition, long offset) {
        String key = UUID.randomUUID().toString();
        return new ConsumerRecord<>(topic, partition, offset, key, value);
    }

    private ConsumerRecord<String, String> record(String value, String header, String topic, int partition, long offset) {
        String key = UUID.randomUUID().toString();
        final Headers headers = new RecordHeaders();
        final String tagHeaderKey = com.navercorp.pinpoint.bootstrap.context.Header.HTTP_TAGS.toString();
        final byte[] tagHeaderVal = String.valueOf(header).getBytes(KafkaConstants.DEFAULT_PINPOINT_HEADER_CHARSET);
        final Header recordHeader = new RecordHeader(tagHeaderKey, tagHeaderVal);
        headers.add(recordHeader);
        return new ConsumerRecord<>(topic, partition, offset, ConsumerRecord.NO_TIMESTAMP, TimestampType.NO_TIMESTAMP_TYPE,
                (long) ConsumerRecord.NULL_CHECKSUM, ConsumerRecord.NULL_SIZE, ConsumerRecord.NULL_SIZE, key, value, headers);
        }
    }