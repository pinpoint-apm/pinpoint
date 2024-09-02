package com.navercorp.pinpoint.common.hbase.async;

import org.apache.hadoop.hbase.client.AsyncConnection;

public interface ConnectionSelector  {
    AsyncConnection getConnection();
}
