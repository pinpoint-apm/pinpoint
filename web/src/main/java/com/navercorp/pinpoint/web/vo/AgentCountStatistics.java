package com.navercorp.pinpoint.web.vo;

import com.navercorp.pinpoint.common.util.DateUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Taejin Koo
 */
public class AgentCountStatistics {

    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private int agentCount;
    private long timestamp;

    private AgentCountStatistics() {
    }

    public AgentCountStatistics(int agentCount, long timestamp) {
        this.agentCount = agentCount;
        this.timestamp = timestamp;
    }

    public int getAgentCount() {
        return agentCount;
    }

    public void setAgentCount(int agentCount) {
        this.agentCount = agentCount;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getDateTime() {
        return DateUtils.longToDateStr(timestamp, DATE_TIME_FORMAT);
    }

    public void setDateTime(String dateTime) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat(DATE_TIME_FORMAT);
        Date parsedDate = format.parse(dateTime);

        this.timestamp = parsedDate.getTime();
    }

}
