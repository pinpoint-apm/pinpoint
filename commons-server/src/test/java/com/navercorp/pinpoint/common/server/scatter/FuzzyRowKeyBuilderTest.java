package com.navercorp.pinpoint.common.server.scatter;

import com.navercorp.pinpoint.common.server.bo.serializer.agent.ApplicationNameRowKeyEncoder;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.filter.Filter;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class FuzzyRowKeyBuilderTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final FuzzyRowKeyFactory<Byte> fuzzyRowKeyFactory = new OneByteFuzzyRowKeyFactory();

    @Test
    public void build_include() throws IOException {
        final long high = 100;
        final long low = 0;
        Byte slotNumber = fuzzyRowKeyFactory.getKey(high);

        FuzzyRowKeyBuilder filterBuilder = new FuzzyRowKeyBuilder();
        Filter filter = filterBuilder.build(high, low);

        byte[] rowKey = newRowKeyV2(slotNumber);
        KeyValue keyValue = new KeyValue(rowKey, 1L);
        Filter.ReturnCode returnCode = filter.filterKeyValue(keyValue);
        Assert.assertEquals(Filter.ReturnCode.INCLUDE, returnCode);
    }

    @Test
    public void build_skip() throws IOException {
        final long high = 100;
        final long low = 0;
        Byte slotNumber = fuzzyRowKeyFactory.getKey(high + 1);

        FuzzyRowKeyBuilder builder = new FuzzyRowKeyBuilder();
        Filter build = builder.build(high, low);

        byte[] rowKey = newRowKeyV2(slotNumber);
        KeyValue keyValue = new KeyValue(rowKey, 1L);
        Filter.ReturnCode returnCode = build.filterKeyValue(keyValue);
        Assert.assertEquals(Filter.ReturnCode.SEEK_NEXT_USING_HINT, returnCode);
    }

    @Test
    public void build_include_range() throws IOException {
//               0    1,    2,  3,   4,    5,    6,     7,     8,    9,      10,     11,    12
//        slot=[100, 200, 400, 800, 1600, 3200, 6400, 12800, 25600, 51200, 102400, 204800, 409600]
        final long high = 400;
        final long low = 0;
        Byte slotNumber = fuzzyRowKeyFactory.getKey(300);

        FuzzyRowKeyBuilder filterBuilder = new FuzzyRowKeyBuilder();
        Filter filter = filterBuilder.build(high, low);

        byte[] rowKey = newRowKeyV2(slotNumber);
        KeyValue keyValue = new KeyValue(rowKey, 1L);
        Filter.ReturnCode returnCode = filter.filterKeyValue(keyValue);
        Assert.assertEquals(Filter.ReturnCode.INCLUDE, returnCode);
    }

    @Test
    public void build_skip_high() throws IOException {
        final long high = 400;
        final long low = 200;
        Byte slotNumber = fuzzyRowKeyFactory.getKey(800);

        FuzzyRowKeyBuilder builder = new FuzzyRowKeyBuilder();
        Filter build = builder.build(high, low);

        byte[] rowKey = newRowKeyV2(slotNumber);
        KeyValue keyValue = new KeyValue(rowKey, 1L);
        Filter.ReturnCode returnCode = build.filterKeyValue(keyValue);
        Assert.assertEquals(Filter.ReturnCode.SEEK_NEXT_USING_HINT, returnCode);
    }

    @Test
    public void build_skip_low() throws IOException {
        final long high = 400;
        final long low = 200;
        Byte slotNumber = fuzzyRowKeyFactory.getKey(10);

        FuzzyRowKeyBuilder builder = new FuzzyRowKeyBuilder();
        Filter build = builder.build(high, low);

        byte[] rowKey = newRowKeyV2(slotNumber);
        KeyValue keyValue = new KeyValue(rowKey, 1L);
        Filter.ReturnCode returnCode = build.filterKeyValue(keyValue);
        Assert.assertEquals(Filter.ReturnCode.SEEK_NEXT_USING_HINT, returnCode);
    }

    @Test
    public void build_skip_low2() throws IOException {
        final long high = 400;
        final long low = 200;
        Byte slotNumber = fuzzyRowKeyFactory.getKey(409600+1);

        FuzzyRowKeyBuilder builder = new FuzzyRowKeyBuilder();
        Filter build = builder.build(high, low);

        byte[] rowKey = newRowKeyV2(slotNumber);
        KeyValue keyValue = new KeyValue(rowKey, 1L);
        Filter.ReturnCode returnCode = build.filterKeyValue(keyValue);
        Assert.assertEquals(Filter.ReturnCode.SEEK_NEXT_USING_HINT, returnCode);
    }


    private byte[] newRowKeyV2(byte fuzzyKey) {
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