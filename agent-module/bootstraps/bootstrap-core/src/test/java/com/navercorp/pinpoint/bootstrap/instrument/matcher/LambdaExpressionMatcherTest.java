package com.navercorp.pinpoint.bootstrap.instrument.matcher;

import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.InterfaceInternalNameMatcherOperand;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.MatcherOperand;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.PackageInternalNameMatcherOperand;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operator.AndMatcherOperator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class LambdaExpressionMatcherTest {

    @Test
    public void getMatcherOperand() {
        LambdaExpressionMatcher matcher = new LambdaExpressionMatcher("java.lang.Runnable", "java.util.function.Function");
        assertEquals("java.lang.Runnable" + LambdaExpressionMatcher.LAMBDA_INSTANCE_NAME_PREFIX, matcher.getBasePackageName());

        MatcherOperand operand = matcher.getMatcherOperand();
        assertThat(operand).isInstanceOf(AndMatcherOperator.class);

        AndMatcherOperator operator = (AndMatcherOperator) operand;
        assertThat(operator.getLeftOperand()).isInstanceOf(PackageInternalNameMatcherOperand.class);
        assertThat(operator.getRightOperand()).isInstanceOf(InterfaceInternalNameMatcherOperand.class);
    }

    @Test
    public void getMatcherOperandWithBaseClassNameIsNull() {
        assertThatThrownBy(() -> {
            LambdaExpressionMatcher lambdaExpressionMatcher = new LambdaExpressionMatcher(null, "java.util.function.Function");
        }).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void getMatcherOperandWithBaseClassNameIsEmpty() {
        assertThatThrownBy(() -> {
            LambdaExpressionMatcher lambdaExpressionMatcher = new LambdaExpressionMatcher("", "java.util.function.Function");
        }).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void getMatcherOperandWithFunctionalInterfaceNameIsNull() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            LambdaExpressionMatcher lambdaExpressionMatcher = new LambdaExpressionMatcher("java.lang.Runnable", null);
        });
    }

    @Test
    public void getMatcherOperandWithFunctionalInterfaceNameIsEmpty() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            LambdaExpressionMatcher lambdaExpressionMatcher = new LambdaExpressionMatcher("java.lang.Runnable", "");
        });
    }
}