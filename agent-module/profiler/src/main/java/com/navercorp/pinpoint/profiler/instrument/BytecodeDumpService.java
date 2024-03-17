package com.navercorp.pinpoint.profiler.instrument;

/**
 * @author Woonduk Kang(emeroad)
 */
public interface BytecodeDumpService {

    void dumpBytecode(String dumpMessage, String jvmClassName, byte[] bytes, ClassLoader classLoader);
}
