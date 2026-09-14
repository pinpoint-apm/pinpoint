package com.navercorp.pinpoint.alarm.vo;

public class AlarmChannelBinding {

    private AlarmChannelOwnerType ownerType;
    private Long ownerId;
    private Long channelId;

    public AlarmChannelBinding() {
    }

    public AlarmChannelBinding(AlarmChannelOwnerType ownerType, Long ownerId, Long channelId) {
        this.ownerType = ownerType;
        this.ownerId = ownerId;
        this.channelId = channelId;
    }

    public AlarmChannelOwnerType getOwnerType() {
        return ownerType;
    }

    public void setOwnerType(AlarmChannelOwnerType ownerType) {
        this.ownerType = ownerType;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public Long getChannelId() {
        return channelId;
    }

    public void setChannelId(Long channelId) {
        this.channelId = channelId;
    }
}
