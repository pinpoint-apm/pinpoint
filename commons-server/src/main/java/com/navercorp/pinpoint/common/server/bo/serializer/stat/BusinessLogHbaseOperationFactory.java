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

package com.navercorp.pinpoint.common.server.bo.serializer.stat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.shaded.org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.navercorp.pinpoint.common.server.bo.serializer.HbaseSerializer;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatType;
import com.navercorp.pinpoint.common.server.bo.stat.BusinessLogDataPoint;
import com.navercorp.pinpoint.common.server.bo.stat.BusinessLogType;
import com.sematext.hbase.wd.AbstractRowKeyDistributor;

/**
 * [XINGUANG]
 */
@Component
public class BusinessLogHbaseOperationFactory {
	
	 	private final BusinessLogRowKeyEncoder rowKeyEncoder;

	    private final BusinessLogRowKeyDecoder rowKeyDecoder;

	    private final AbstractRowKeyDistributor rowKeyDistributor;

	    @Autowired
	    public BusinessLogHbaseOperationFactory(
	    		BusinessLogRowKeyEncoder rowKeyEncoder,
	    		BusinessLogRowKeyDecoder rowKeyDecoder,
	            @Qualifier("businessLogRowKeyDistributor") AbstractRowKeyDistributor rowKeyDistributor) {
	        Assert.notNull(rowKeyEncoder, "rowKeyEncoder must not be null");
	        Assert.notNull(rowKeyDecoder, "rowKeyDecoder must not be null");
	        Assert.notNull(rowKeyDistributor, "rowKeyDistributor must not be null");
	        this.rowKeyEncoder = rowKeyEncoder;
	        this.rowKeyDecoder = rowKeyDecoder;
	        this.rowKeyDistributor = rowKeyDistributor;
	    }

	    public <T extends BusinessLogDataPoint> List<Put> createPuts(String agentId, BusinessLogType businessLogType, List<T> businessLogDataPoints, HbaseSerializer<List<T>, Put> businessLogSerializer) {
	    	if (CollectionUtils.isEmpty(businessLogDataPoints)) {
	    		return Collections.emptyList();
	    	}
	    	Map<String, List<T>> transactionIdAndSpanIdslots = slotBusinessLogDataPoints(businessLogDataPoints);
	    	List<Put> puts = new ArrayList<Put>();
	    	for (Map.Entry<String , List<T>> tANDsIdslot : transactionIdAndSpanIdslots.entrySet()) {
	    		String tANDsId = tANDsIdslot.getKey();
	    		List<T> slottedBusinessLogDataPoints = tANDsIdslot.getValue();
	    		
	    		final BusinessLogRowKeyComponent rowkeyComponent = new BusinessLogRowKeyComponent(agentId, businessLogType, tANDsId);
	    		byte[] rowKey = this.rowKeyEncoder.encodeRowKey(rowkeyComponent);


	    		Put put = new Put(rowKey);
	    		businessLogSerializer.serialize(slottedBusinessLogDataPoints, put, null);
	    		puts.add(put);
	    	}
	    	return puts;
	    }
	    
	    private <T extends BusinessLogDataPoint> Map<String, List<T>> slotBusinessLogDataPoints(List<T> businessLogDataPoints) {
	    	Map<String, List<T>> transactionIdAndSpanIdslots = new TreeMap<String, List<T>>();
	    	for (T businessLogDataPoint : businessLogDataPoints) {
	    		String transactionIdAndSpanId = businessLogDataPoint.getTransactionIdANDSpanId();
	    		List<T> slottedDataPoints = transactionIdAndSpanIdslots.get(transactionIdAndSpanId);
	    		if (slottedDataPoints == null) {
	    			slottedDataPoints = new ArrayList<T>();
	    			transactionIdAndSpanIdslots.put(transactionIdAndSpanId, slottedDataPoints);
	    		}
	    		slottedDataPoints.add(businessLogDataPoint);
	    	}
	    	return transactionIdAndSpanIdslots;
	    }
	    
	    public String getAgentId(byte[] RowKey) {
	        return this.rowKeyDecoder.decodeRowKey(RowKey).getAgentId();
	    }
	    
	    public BusinessLogType getAgentStatType(byte[] RowKey) {
	        return this.rowKeyDecoder.decodeRowKey(RowKey).getBusinessLogType();
	    }
	    
	    public String getTransactionIdANDSpanId(byte[] RowKey){
	        return this.rowKeyDecoder.decodeRowKey(RowKey).getTransactionIdANDSpanId();
	    }
}
