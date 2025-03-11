package com.navercorp.pinpoint.web.vo;

public interface RuleInterface {
    String getCheckerName();

    /**
     * @deprecated Since 3.1.0. Use {@link #getApplicationName()} instead.
     */
    @Deprecated
    String getApplicationId();
    String getApplicationName();

    String getServiceType();
    Number getThreshold();
    String getNotes();
}
