package com.navercorp.pinpoint.web.response;

import java.util.Objects;

public class CreateUserGroupResponse extends SuccessResponse {
    private final String number;

    public CreateUserGroupResponse(String result, String number) {
        super(result);
        this.number = Objects.requireNonNull(number, "number");
    }

    public String getNumber() {
        return number;
    }
}
