package com.navercorp.pinpoint.pinot.alarm.condition;

import java.math.BigDecimal;
import java.util.function.BiFunction;

public abstract class AlarmCondition<T> implements BiFunction<BigDecimal, T, Boolean> {

    public boolean isConditionMet(BigDecimal threshold, T collectedValue) {
        return this.apply(threshold, collectedValue);
    }
}
