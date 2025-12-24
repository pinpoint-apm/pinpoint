package com.navercorp.pinpoint.common.server.bo.serializer;

/**
 * @author Woonduk Kang(emeroad)
 */
public interface RowKeyDecoder<V> {

    V decodeRowKey(byte[] rowkey);

    default V decodeRowKey(byte[] rowkey, int offset, int length) {
        throw new UnsupportedOperationException("operation not supported");
    }

}
