package com.navercorp.pinpoint.common.hbase;

public class DefaultLastRowHandler<T> implements LastRowHandler<T> {
    private T lastRow;

    @Override
    public void handleLastRow(T lastRow) {
        this.lastRow = lastRow;
    }

    @Override
    public T getLastRow() {
        return lastRow;
    }
}
