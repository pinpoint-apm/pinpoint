package com.navercorp.pinpoint.common.buffer;

import com.navercorp.pinpoint.common.util.LRUCache;
import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;

public class StringCacheableBufferTest {

    @Test
    public void stringCache () {

        Buffer writer = new AutomaticBuffer();
        writer.putPrefixedString("abc");
        writer.putPrefixedString("123");
        writer.putPrefixedString("abc");


        StringAllocator allocator = new CachedStringAllocator(new LRUCache<ByteBuffer, String>(2));

        Buffer buffer = new StringCacheableBuffer(writer.getBuffer(), allocator);
        String s1 = buffer.readPrefixedString();
        String s2 = buffer.readPrefixedString();
        String s3 = buffer.readPrefixedString();

        Assert.assertSame(s1, s3);
    }
}