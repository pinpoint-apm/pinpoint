package com.navercorp.pinpoint.alarm.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.navercorp.pinpoint.alarm.validation.AlarmValidationConstants;
import com.navercorp.pinpoint.alarm.vo.AlarmMethodType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class AlarmNotificationChannelRequest {

    @NotBlank(message = "channelName must not be blank")
    @Size(max = AlarmValidationConstants.MAX_CHANNEL_NAME_LENGTH,
            message = "channelName is too long")
    private String channelName;

    @NotNull(message = "methodType must not be null")
    private AlarmMethodType methodType;

    @NotBlank(message = "destination must not be blank")
    @Size(max = AlarmValidationConstants.MAX_DESTINATION_LENGTH,
            message = "destination is too long")
    private String destination;

    private JsonNode config;

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public AlarmMethodType getMethodType() {
        return methodType;
    }

    public void setMethodType(AlarmMethodType methodType) {
        this.methodType = methodType;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public JsonNode getConfig() {
        return config;
    }

    public void setConfig(JsonNode config) {
        this.config = config;
    }
}
