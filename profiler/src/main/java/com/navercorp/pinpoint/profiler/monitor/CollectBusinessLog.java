/*
 * Copyright 2017 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.monitor;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.profiler.monitor.collector.BusinessLogMetaCollector;
import com.navercorp.pinpoint.profiler.sender.DataSender;
import com.navercorp.pinpoint.thrift.dto.TAgentStat;
import com.navercorp.pinpoint.thrift.dto.TAgentStatBatch;
import com.navercorp.pinpoint.thrift.dto.TBusinessLog;
import com.navercorp.pinpoint.thrift.dto.TBusinessLogBatch;

/**
 * [XINGUANG]
 */
public class CollectBusinessLog implements Runnable{

	private final Logger logger = LoggerFactory.getLogger(CollectBusinessLog.class);
	
	private DataSender dataSender;
    private String agentId;
    private long agentStartTimestamp;
    private BusinessLogMetaCollector<TBusinessLog> businessLogMetaCollector;
    private int numCollectionsPerBatch;
	
	private int collectCount = 0;
    private long prevCollectionTimestamp = System.currentTimeMillis();
    private List<TBusinessLog> businessLogs;
    
    public CollectBusinessLog(DataSender dataSender, String agentId, long agentStartTimestamp,
                               BusinessLogMetaCollector<TBusinessLog> businessLogMetaCollector, int numCollectionsPerBatch) {
    	this.dataSender = dataSender;
    	this.agentId = agentId;
    	this.agentStartTimestamp = agentStartTimestamp;
    	this.businessLogMetaCollector = businessLogMetaCollector;
    	this.numCollectionsPerBatch = numCollectionsPerBatch;
    	this.businessLogs = new ArrayList<TBusinessLog>(numCollectionsPerBatch);
    }
	
	@Override
	public void run() {
        final long currentCollectionTimestamp = System.currentTimeMillis();
        final long collectInterval = currentCollectionTimestamp - this.prevCollectionTimestamp;
        try {
            final TBusinessLog businessLog = businessLogMetaCollector.collect();
            logger.info(businessLog.toString());
            businessLog.setTimestamp(currentCollectionTimestamp);
            businessLog.setCollectInterval(collectInterval);
            this.businessLogs.add(businessLog);
            if (++this.collectCount >= numCollectionsPerBatch) {
                sendBusinessLogs();
                businessLogMetaCollector.saveLogMark();
                this.collectCount = 0;
            }
        } catch (Exception ex) {
            logger.warn("businessLog collect failed. Caused:{}", ex.getMessage(), ex);
        } finally {
            this.prevCollectionTimestamp = currentCollectionTimestamp;
        }
	}
	
	private void sendBusinessLogs() {
        // prepare TBusinessLog object.
        // TODO multi thread issue.
        // If we reuse TBusinessLog, there could be concurrency issue because data sender runs in a different thread.
        final TBusinessLogBatch businessLogBatch = new TBusinessLogBatch();
        businessLogBatch.setAgentId(agentId);
        businessLogBatch.setStartTimestamp(agentStartTimestamp);
        businessLogBatch.setBusinessLogs(this.businessLogs);
        // If we reuse businessLogs list, there could be concurrency issue because data sender runs in a different
        // thread.
        // So create new list.
        this.businessLogs = new ArrayList<TBusinessLog>(numCollectionsPerBatch);
        logger.trace("collect businessLogBatch:{}", businessLogBatch);
        dataSender.send(businessLogBatch);
	}

}
