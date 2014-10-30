package com.nhn.pinpoint.thrift.io;

public interface ChunkHeaderBufferedTBaseSerializerFlushHandler {

    void handle(byte[] buffer, int offset, int length);
}
