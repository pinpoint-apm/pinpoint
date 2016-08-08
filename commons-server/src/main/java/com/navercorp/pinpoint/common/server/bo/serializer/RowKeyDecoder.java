package com.navercorp.pinpoint.common.server.bo.serializer;

/**
 * @author Woonduk Kang(emeroad)
 */
public interface RowKeyDecoder<V> {

    V decodeRowKey(byte[] rowkey);

}
