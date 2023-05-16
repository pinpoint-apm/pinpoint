package com.navercorp.pinpoint.web.response;

import com.fasterxml.jackson.annotation.JsonInclude;

public class AlarmResponse extends SuccessResponse {
    private final String ruleId;

    public AlarmResponse(String result, String ruleId) {
        super(result);
        this.ruleId = ruleId;
    }


    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getRuleId() {
        return ruleId;
    }
}
