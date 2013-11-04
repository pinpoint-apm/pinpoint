package com.nhn.pinpoint.web.vo.callstacks;

import java.util.List;

/**
 * @author netspider
 * @author emeroad
 */
public class RecordSet {

    private long startTime = -1;
	private long endTime = -1;

	private List<Record> recordList;
	private String applicationName;
	private long beginTimestamp;

    private String agentId;
    private String applicationId;

	public RecordSet() {
	}

    public void setRecordList(List<Record> recordList) {
        this.recordList = recordList;
    }

    public List<Record> getRecordList() {
        return recordList;
    }

    public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public boolean isStartTimeSet() {
		return startTime != -1;
	}

	public boolean isEndTimeSet() {
		return endTime != -1;
	}

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public void setBeginTimestamp(long beginTimestamp) {
        this.beginTimestamp = beginTimestamp;
    }

    public String getApplicationName() {
		return applicationName;
	}

	public long getBeginTimestamp() {
		return beginTimestamp;
	}


    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }
}
