package com.navercorp.pinpoint.thrift.io;

/**
 * @Author Taejin Koo
 */
public interface ResettableOutputStream {

    void mark();

    void resetToMarkIndex();

}
