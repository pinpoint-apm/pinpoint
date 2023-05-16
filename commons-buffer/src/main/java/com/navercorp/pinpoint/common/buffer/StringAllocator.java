package com.navercorp.pinpoint.common.buffer;

import java.nio.charset.Charset;

public interface StringAllocator {

    StringAllocator DEFAULT_ALLOCATOR = new SimpleStringAllocator();

    String allocate(byte[] bytes, int offset, int length, Charset charset);


    class SimpleStringAllocator implements StringAllocator {

        public SimpleStringAllocator() {
        }

        @Override
        public String allocate(byte[] bytes, int offset, int length, Charset charset) {
            return new String(bytes, offset, length, charset);
        }
    }


}
