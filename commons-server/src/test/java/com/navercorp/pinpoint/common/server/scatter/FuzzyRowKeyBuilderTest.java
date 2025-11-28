package com.navercorp.pinpoint.common.server.scatter;

import com.navercorp.pinpoint.common.server.bo.serializer.agent.ApplicationNameRowKeyEncoder;
import com.navercorp.pinpoint.common.server.bo.serializer.agent.TraceIndexRowUtils;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.trace.ServiceType;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class FuzzyRowKeyBuilderTest {
    private final Logger logger = LogManager.getLogger(this.getClass());
    private final FuzzyRowKeyFactory<Byte> fuzzyRowKeyFactory = new OneByteFuzzyRowKeyFactory();

    private final FuzzyRowKeyBuilder v1Builder = new FuzzyRowKeyBuilder();

    @Test
    public void build_include() throws IOException {
        Jdk17Utils.assumeFalse();

        final long high = 100;
        final long low = 0;
        Byte slotNumber = fuzzyRowKeyFactory.getKey(high);

        Filter filter = v1Builder.build(high, low);

        byte[] rowKey = newRowKeyV1(slotNumber);
        KeyValue keyValue = new KeyValue(rowKey, 1L);
        Filter.ReturnCode returnCode = filter.filterCell(keyValue);
        Assertions.assertEquals(Filter.ReturnCode.INCLUDE, returnCode);
    }

    @Test
    public void build_skip() throws IOException {
        final long high = 100;
        final long low = 0;
        Byte slotNumber = fuzzyRowKeyFactory.getKey(high + 1);

        Filter build = v1Builder.build(high, low);

        byte[] rowKey = newRowKeyV1(slotNumber);
        KeyValue keyValue = new KeyValue(rowKey, 1L);
        Filter.ReturnCode returnCode = build.filterCell(keyValue);
        Assertions.assertEquals(Filter.ReturnCode.SEEK_NEXT_USING_HINT, returnCode);
    }

    @Test
    public void build_include_range() throws IOException {
        Jdk17Utils.assumeFalse();

//               0    1,    2,  3,   4,    5,    6,     7,     8,    9,      10,     11,    12
//        slot=[100, 200, 400, 800, 1600, 3200, 6400, 12800, 25600, 51200, 102400, 204800, 409600]
        final long high = 400;
        final long low = 0;
        Byte slotNumber = fuzzyRowKeyFactory.getKey(300);

        Filter filter = v1Builder.build(high, low);

        byte[] rowKey = newRowKeyV1(slotNumber);
        KeyValue keyValue = new KeyValue(rowKey, 1L);
        Filter.ReturnCode returnCode = filter.filterCell(keyValue);
        Assertions.assertEquals(Filter.ReturnCode.INCLUDE, returnCode);
    }

    @Test
    public void build_skip_high() throws IOException {
        final long high = 400;
        final long low = 200;
        Byte slotNumber = fuzzyRowKeyFactory.getKey(800);

        FuzzyRowKeyBuilder builder = new FuzzyRowKeyBuilder();
        Filter build = builder.build(high, low);

        byte[] rowKey = newRowKeyV1(slotNumber);
        KeyValue keyValue = new KeyValue(rowKey, 1L);
        Filter.ReturnCode returnCode = build.filterCell(keyValue);
        Assertions.assertEquals(Filter.ReturnCode.SEEK_NEXT_USING_HINT, returnCode);
    }

    @Test
    public void build_skip_low() throws IOException {
        final long high = 400;
        final long low = 200;
        Byte slotNumber = fuzzyRowKeyFactory.getKey(10);

        Filter build = v1Builder.build(high, low);

        byte[] rowKey = newRowKeyV1(slotNumber);
        KeyValue keyValue = new KeyValue(rowKey, 1L);
        Filter.ReturnCode returnCode = build.filterCell(keyValue);
        Assertions.assertEquals(Filter.ReturnCode.SEEK_NEXT_USING_HINT, returnCode);
    }

    @Test
    public void build_skip_low2() throws IOException {
        final long high = 400;
        final long low = 200;
        Byte slotNumber = fuzzyRowKeyFactory.getKey(409600 + 1);

        Filter build = v1Builder.build(high, low);

        byte[] rowKey = newRowKeyV1(slotNumber);
        KeyValue keyValue = new KeyValue(rowKey, 1L);
        Filter.ReturnCode returnCode = build.filterCell(keyValue);
        Assertions.assertEquals(Filter.ReturnCode.SEEK_NEXT_USING_HINT, returnCode);
    }

    private byte[] newRowKeyV1(byte fuzzyKey) {
        ApplicationNameRowKeyEncoder encoder = new ApplicationNameRowKeyEncoder();
        final byte[] apps = encoder.encodeRowKey("app", 100);
        // salt + fuzzy
        int etcSize = 2;
        final byte[] copy = new byte[apps.length + etcSize];
        System.arraycopy(apps, 0, copy, 0, apps.length);
        copy[copy.length - 1] = fuzzyKey;
        return copy;
    }
}