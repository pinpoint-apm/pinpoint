package com.navercorp.pinpoint.common.hbase;


public interface LastRowHandler<T> {
    void handleLastRow(T lastRow);

    T getLastRow();
}
