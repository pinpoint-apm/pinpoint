package org.apache.hadoop.hbase.client;

public final class BufferedMutatorUtils {
    private BufferedMutatorUtils() {
    }

    public static long getCurrentWriteBufferSize(BufferedMutatorImpl mutator) {
        return mutator.getCurrentWriteBufferSize();
    }
}
