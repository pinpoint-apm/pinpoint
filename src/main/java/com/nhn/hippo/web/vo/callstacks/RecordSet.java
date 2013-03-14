package com.nhn.hippo.web.vo.callstacks;

import java.util.List;

/**
 * @author netspider
 */
public class RecordSet {

    private long startTime = -1;
	private long endTime = -1;

	private List<Record> recordList;
	private String applicationName;
	private long beginTimestamp;

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



}
