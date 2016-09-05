package com.navercorp.pinpoint.common.server.bo.serializer;

/**
 * @author Woonduk Kang(emeroad)
 */
public interface RowKeyEncoder<V> {

    byte[] encodeRowKey(V value);

}
