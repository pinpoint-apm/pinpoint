package com.navercorp.pinpoint.batch.alarm.checker;

import java.util.List;

public class PinotAlarmCheckers {
    private final List<PinotAlarmChecker> children;

    public PinotAlarmCheckers(List<PinotAlarmChecker> children) {
        this.children = children;
    }

    public void check(long now) {
        for (PinotAlarmChecker<?> child : this.children) {
            child.check(now);
        }
    }

    public List<PinotAlarmChecker> getChildren() {
        return children;
    }

}
