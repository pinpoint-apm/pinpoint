package com.profiler.common.hbase;

import org.apache.hadoop.hbase.client.HTable;

public interface HBaseBatchJob {
	void doBatch(HTable htable);
}
