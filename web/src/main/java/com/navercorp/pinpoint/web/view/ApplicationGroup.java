package com.nhn.pinpoint.web.view;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.nhn.pinpoint.web.vo.Application;

import java.util.List;

/**
 * @author emeroad
 */
@JsonSerialize(using = ApplicationGroupSerializer.class)
public class ApplicationGroup {

    private final List<Application> applicationList;

    public ApplicationGroup(List<Application> applicationList) {
        if (applicationList == null) {
            throw new NullPointerException("applicationList must not be null");
        }
        this.applicationList = applicationList;
    }

    public List<Application> getApplicationList() {
        return applicationList;
    }
}
