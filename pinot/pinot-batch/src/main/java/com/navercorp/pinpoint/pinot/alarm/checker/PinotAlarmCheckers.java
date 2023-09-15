package com.navercorp.pinpoint.pinot.alarm.checker;

import java.util.List;

public class PinotAlarmCheckers {
    private final List<PinotAlarmChecker<? extends Number>> children;

    public PinotAlarmCheckers(List<PinotAlarmChecker<? extends Number>> children) {
        this.children = children;
    }

    public void check(long now) {
        for (PinotAlarmChecker<? extends Number> child : this.children) {
            child.check(now);
        }
    }

    public List<PinotAlarmChecker<? extends Number>> getChildren() {
        return children;
    }

}
