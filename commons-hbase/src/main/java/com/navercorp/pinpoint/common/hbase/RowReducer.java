package com.navercorp.pinpoint.common.hbase;

public interface RowReducer<T> {

    
    T reduce(T map) throws Exception;
}
