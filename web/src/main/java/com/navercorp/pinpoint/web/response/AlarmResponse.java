package com.navercorp.pinpoint.web.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.navercorp.pinpoint.common.server.response.Result;
import com.navercorp.pinpoint.common.server.response.SimpleResponse;

public class AlarmResponse extends SimpleResponse {
    private final String ruleId;

    public AlarmResponse(Result result, String ruleId) {
        super(result);
        this.ruleId = ruleId;
    }


    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getRuleId() {
        return ruleId;
    }
}
