package com.navercorp.pinpoint.bootstrap.instrument.matcher;

import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.InterfaceInternalNameMatcherOperand;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.MatcherOperand;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.PackageInternalNameMatcherOperand;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operator.AndMatcherOperator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LambdaExpressionMatcherTest {

    @Test
    public void getMatcherOperand() {
        LambdaExpressionMatcher matcher = new LambdaExpressionMatcher("java.lang.Runnable", "java.util.function.Function");
        assertEquals("java.lang.Runnable" + LambdaExpressionMatcher.LAMBDA_INSTANCE_NAME_PREFIX, matcher.getBasePackageName());

        MatcherOperand operand = matcher.getMatcherOperand();
        assertTrue(operand instanceof AndMatcherOperator);

        AndMatcherOperator operator = (AndMatcherOperator) operand;
        assertTrue(operator.getLeftOperand() instanceof PackageInternalNameMatcherOperand);
        assertTrue(operator.getRightOperand() instanceof InterfaceInternalNameMatcherOperand);
    }

    @Test
    public void getMatcherOperandWithBaseClassNameIsNull() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            LambdaExpressionMatcher lambdaExpressionMatcher = new LambdaExpressionMatcher(null, "java.util.function.Function");
        });
    }

    @Test
    public void getMatcherOperandWithBaseClassNameIsEmpty() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            LambdaExpressionMatcher lambdaExpressionMatcher = new LambdaExpressionMatcher("", "java.util.function.Function");
        });
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