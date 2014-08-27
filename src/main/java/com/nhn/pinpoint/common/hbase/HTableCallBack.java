package com.nhn.pinpoint.common.hbase;

import org.apache.hadoop.hbase.client.HTable;

import java.io.IOException;

/**
 * @author emeroad
 */
public interface HTableCallBack {
    void doExecute(HTable hTable) throws IOException;

//    void doMultiExecute(HTable... tables) throws IOException;
}
