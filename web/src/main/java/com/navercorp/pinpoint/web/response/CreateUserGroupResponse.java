package com.navercorp.pinpoint.web.response;

import com.navercorp.pinpoint.common.server.response.Result;
import com.navercorp.pinpoint.common.server.response.SimpleResponse;

import java.util.Objects;

public class CreateUserGroupResponse extends SimpleResponse {
    private final String number;

    public CreateUserGroupResponse(Result result, String number) {
        super(result);
        this.number = Objects.requireNonNull(number, "number");
    }

    public String getNumber() {
        return number;
    }
}
