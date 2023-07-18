package com.navercorp.pinpoint.batch.alarm.condition;

public enum AlarmConditionCategory {
    BIGGER_THAN(">"),
    BIGGER_OR_EQUALS_TO(">="),
    EQUALS_TO("="),
    SMALLER_THAN("<"),
    SMALLER_OR_EQUALS_TO("<=");

    private final String name;

    AlarmConditionCategory(String name) {
        this.name = name;
    }

}
