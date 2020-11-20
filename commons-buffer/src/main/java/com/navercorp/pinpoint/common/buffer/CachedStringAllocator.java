package com.navercorp.pinpoint.common.buffer;

import com.navercorp.pinpoint.common.util.Assert;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Map;

public class CachedStringAllocator implements StringAllocator {
    private final Map<ByteBuffer, String> cache;

    public CachedStringAllocator(Map<ByteBuffer, String> cache) {
        this.cache = Assert.requireNonNull(cache, "cache");
    }

    @Override
    public String allocate(byte[] bytes, int offset, int length, Charset charset) {
        final ByteBuffer wrapByteBuffer = ByteBuffer.wrap(bytes, offset, length);

        final String hit = cache.get(wrapByteBuffer);
        if (hit != null) {
            return hit;
        }

        final String newString = StringAllocator.DEFAULT_ALLOCATOR.allocate(bytes, offset, length, charset);
        cache.put(wrapByteBuffer, newString);
        return newString;
    }
}

