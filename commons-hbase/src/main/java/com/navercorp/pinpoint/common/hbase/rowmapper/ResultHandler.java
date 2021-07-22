package com.navercorp.pinpoint.common.hbase.rowmapper;

import org.apache.hadoop.hbase.client.Result;

public interface ResultHandler {
    void mapRow(Result result, int rowNum);
}
