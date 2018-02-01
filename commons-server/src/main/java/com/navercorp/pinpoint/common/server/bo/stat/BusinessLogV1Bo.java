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

package com.navercorp.pinpoint.common.server.bo.stat;

/**
 * [XINGUANG]
 */
public class BusinessLogV1Bo implements BusinessLogDataPoint{
	
	private String agentId;
    private long startTimestamp;
    private long timestamp;
    private String time;
    private String threadName;
    private String logLevel;
    private String className;
    private String transactionId;
    private String spanId; 
    private String message;

    
	public String getAgentId() {
		return agentId;
	}


	public void setAgentId(String agentId) {
		this.agentId = agentId;
	}


	public long getStartTimestamp() {
		return startTimestamp;
	}


	public void setStartTimestamp(long startTimestamp) {
		this.startTimestamp = startTimestamp;
	}


	public long getTimestamp() {
		return timestamp;
	}


	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}


	public String getTime() {
		return time;
	}


	public void setTime(String time) {
		this.time = time;
	}


	public String getThreadName() {
		return threadName;
	}


	public void setThreadName(String threadName) {
		this.threadName = threadName;
	}


	public String getLogLevel() {
		return logLevel;
	}


	public void setLogLevel(String logLevel) {
		this.logLevel = logLevel;
	}


	public String getClassName() {
		return className;
	}


	public void setClassName(String className) {
		this.className = className;
	}


	public String getTransactionId() {
		return transactionId;
	}


	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}


	public String getSpanId() {
		return spanId;
	}


	public void setSpanId(String spanId) {
		this.spanId = spanId;
	}


	public String getMessage() {
		return message;
	}


	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public BusinessLogType geTBusinessLogType() {
		return BusinessLogType.BUSINESS_LOG_V1;
	}


	@Override
	public String getTransactionIdANDSpanId() {
		// TODO Auto-generated method stub
		StringBuilder sb = new StringBuilder();
		sb.append(this.transactionId).append("#").append(this.spanId);
		return sb.toString();
	}


}
