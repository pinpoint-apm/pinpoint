package com.navercorp.pinpoint.bootstrap.instrument.matcher;

import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.InterfaceInternalNameMatcherOperand;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.MatcherOperand;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.PackageInternalNameMatcherOperand;
import com.navercorp.pinpoint.common.util.StringUtils;

import java.util.Objects;

public class LambdaExpressionMatcher implements PackageBasedMatcher {
    static final String LAMBDA_INSTANCE_NAME_PREFIX = "$$Lambda$";
    private final String basePackageName;
    private final MatcherOperand matcherOperand;

    public LambdaExpressionMatcher(String baseClassName, String functionalInterfaceName) {
        Objects.requireNonNull(baseClassName, "baseClassName");
        if (!StringUtils.hasText(baseClassName)) {
            throw new IllegalArgumentException("baseClassName must not be empty");
        }
        Objects.requireNonNull(functionalInterfaceName, "functionalInterfaceName");
        if (!StringUtils.hasText(functionalInterfaceName)) {
            throw new IllegalArgumentException("functionalInterfaceName must not be empty");
        }
        this.basePackageName = baseClassName + LAMBDA_INSTANCE_NAME_PREFIX;

        final MatcherOperand operand = new PackageInternalNameMatcherOperand(basePackageName);
        final MatcherOperand functionalInterfaceMatcherOperand = new InterfaceInternalNameMatcherOperand(functionalInterfaceName, false);
        this.matcherOperand = operand.and(functionalInterfaceMatcherOperand);
    }

    @Override
    public String getBasePackageName() {
        return this.basePackageName;
    }

    @Override
    public MatcherOperand getMatcherOperand() {
        return this.matcherOperand;
    }

    @Override
    public String toString() {
        return "LambdaExpressionMatcher{" +
                "basePackageName='" + basePackageName + '\'' +
                ", matcherOperand=" + matcherOperand +
                '}';
    }
}
