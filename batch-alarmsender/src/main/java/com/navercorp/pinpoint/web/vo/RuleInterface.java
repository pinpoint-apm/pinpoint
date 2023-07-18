package com.navercorp.pinpoint.web.vo;

public interface RuleInterface {
    String getCheckerName();
    String getApplicationId();
    String getServiceType();
    Number getThreshold();
    String getNotes();
}
