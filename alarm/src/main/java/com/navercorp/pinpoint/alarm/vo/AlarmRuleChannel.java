package com.navercorp.pinpoint.alarm.vo;

public class AlarmRuleChannel {

    private Long ruleId;
    private Long channelId;

    public AlarmRuleChannel() {
    }

    public Long getRuleId() {
        return ruleId;
    }

    public void setRuleId(Long ruleId) {
        this.ruleId = ruleId;
    }

    public Long getChannelId() {
        return channelId;
    }

    public void setChannelId(Long channelId) {
        this.channelId = channelId;
    }
}
