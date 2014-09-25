package com.nhn.pinpoint.profiler.monitor.codahale.gc;

import com.nhn.pinpoint.thrift.dto.TJvmGc;
import com.nhn.pinpoint.thrift.dto.TJvmGcType;

/**
 * Unknown Garbage collector
 * @author hyungil.jeong
 */
public class UnknownGarbageCollector implements GarbageCollector {

    public static final TJvmGcType GC_TYPE = TJvmGcType.UNKNOWN;

    @Override
    public int getTypeCode() {
        return GC_TYPE.ordinal();
    }

    @Override
    public TJvmGc collect() {
        // 아예 전송을 안하려고 null을 리턴한다.
        //(Thrift DTO에서 gc 필드는 optional)
        return null;
    }

    @Override
    public String toString() {
        return "Unknown Garbage collector";
    }

}
