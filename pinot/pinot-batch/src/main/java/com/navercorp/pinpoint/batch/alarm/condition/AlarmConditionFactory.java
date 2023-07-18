package com.navercorp.pinpoint.batch.alarm.condition;

import com.navercorp.pinpoint.batch.alarm.vo.PinotAlarmRule;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class AlarmConditionFactory {
    private static AlarmCondition<Long> BIGGER_THAN_LONG_VALUE = new AlarmCondition<>() {
        @Override
        public Boolean apply(BigDecimal threshold, Long collectedValue) {
            return BigDecimal.valueOf(collectedValue).compareTo(threshold) > 0;
        }
    };

    private static AlarmCondition<Long> BIGGER_OR_EQUALS_TO_LONG_VALUE = new AlarmCondition<>() {
        @Override
        public Boolean apply(BigDecimal threshold, Long collectedValue) {
            return BigDecimal.valueOf(collectedValue).compareTo(threshold) >= 0;
        }
    };

    private static AlarmCondition<Long> EQUALS_TO_LONG_VALUE = new AlarmCondition<>() {
        @Override
        public Boolean apply(BigDecimal threshold, Long collectedValue) {
            return BigDecimal.valueOf(collectedValue).compareTo(threshold) == 0;
        }
    };

    private static AlarmCondition<Long> SMALLER_THAN_LONG_VALUE = new AlarmCondition<>() {
        @Override
        public Boolean apply(BigDecimal threshold, Long collectedValue) {
            return BigDecimal.valueOf(collectedValue).compareTo(threshold) < 0;
        }
    };

    private static AlarmCondition<Long> SMALLER_OR_EQUALS_TO_LONG_VALUE = new AlarmCondition<>() {
        @Override
        public Boolean apply(BigDecimal threshold, Long collectedValue) {
            return BigDecimal.valueOf(collectedValue).compareTo(threshold) <= 0;
        }
    };


    private static AlarmCondition<Double> BIGGER_THAN_DOUBLE_VALUE = new AlarmCondition<>() {
        @Override
        public Boolean apply(BigDecimal threshold, Double collectedValue) {
            return BigDecimal.valueOf(collectedValue).compareTo(threshold) > 0;
        }
    };

    private static AlarmCondition<Double> BIGGER_OR_EQAULS_TO_DOUBLE_VALUE = new AlarmCondition<>() {
        @Override
        public Boolean apply(BigDecimal threshold, Double collectedValue) {
            return BigDecimal.valueOf(collectedValue).compareTo(threshold) >= 0;
        }
    };

    private static AlarmCondition<Double> EQUALS_TO_DOUBLE_VALUE = new AlarmCondition<>() {
        @Override
        public Boolean apply(BigDecimal threshold, Double collectedValue) {
            return BigDecimal.valueOf(collectedValue).compareTo(threshold) == 0;
        }
    };

    private static AlarmCondition<Double> SMALLER_THAN_DOUBLE_VALUE = new AlarmCondition<>() {
        @Override
        public Boolean apply(BigDecimal threshold, Double collectedValue) {
            return BigDecimal.valueOf(collectedValue).compareTo(threshold) < 0;
        }
    };

    private static AlarmCondition<Double> SMALLER_OR_EQUALS_TO_DOUBLE_VALUE = new AlarmCondition<>() {
        @Override
        public Boolean apply(BigDecimal threshold, Double collectedValue) {
            return BigDecimal.valueOf(collectedValue).compareTo(threshold) <= 0;
        }
    };

    public static AlarmCondition<Long> getLongAlarmCondition(PinotAlarmRule rule) {
        String condition = rule.getCondition();
        switch (AlarmConditionCategory.valueOf(condition)) {
            case BIGGER_THAN:
                return BIGGER_THAN_LONG_VALUE;
            case BIGGER_OR_EQUALS_TO:
                return BIGGER_OR_EQUALS_TO_LONG_VALUE;
            case EQUALS_TO:
                return EQUALS_TO_LONG_VALUE;
            case SMALLER_THAN:
                return SMALLER_THAN_LONG_VALUE;
            case SMALLER_OR_EQUALS_TO:
                return SMALLER_OR_EQUALS_TO_LONG_VALUE;
        }

        throw new IllegalArgumentException("unable to create AlarmCondition : " + rule.getCondition());
    }

    public static AlarmCondition<Double> getDoubleAlarmCondition(PinotAlarmRule rule) {
        String condition = rule.getCondition();
        switch (AlarmConditionCategory.valueOf(condition)) {
            case BIGGER_THAN:
                return BIGGER_THAN_DOUBLE_VALUE;
            case BIGGER_OR_EQUALS_TO:
                return BIGGER_OR_EQAULS_TO_DOUBLE_VALUE;
            case EQUALS_TO:
                return EQUALS_TO_DOUBLE_VALUE;
            case SMALLER_THAN:
                return SMALLER_THAN_DOUBLE_VALUE;
            case SMALLER_OR_EQUALS_TO:
                return SMALLER_OR_EQUALS_TO_DOUBLE_VALUE;
        }

        throw new IllegalArgumentException("unable to create AlarmCondition : " + rule.getCondition());
    }
}
