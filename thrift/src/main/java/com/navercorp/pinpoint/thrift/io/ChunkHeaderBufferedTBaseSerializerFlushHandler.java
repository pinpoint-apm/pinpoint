package com.nhn.pinpoint.thrift.io;

/**
 * 
 * @author jaehong.kim
 */
public interface ChunkHeaderBufferedTBaseSerializerFlushHandler {

    void handle(byte[] buffer, int offset, int length);
}