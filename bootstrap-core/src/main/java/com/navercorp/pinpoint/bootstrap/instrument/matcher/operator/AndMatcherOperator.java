/*
 * Copyright 2017 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.bootstrap.instrument.matcher.operator;

import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.AbstractMatcherOperand;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.MatcherOperand;
import com.navercorp.pinpoint.common.annotations.InterfaceStability;
import com.navercorp.pinpoint.common.util.Assert;

/**
 * @author jaehong.kim
 */
@InterfaceStability.Unstable
public class AndMatcherOperator extends AbstractMatcherOperand implements MatcherOperator {
    private final MatcherOperand leftOperand;
    private final MatcherOperand rightOperand;

    public AndMatcherOperator(final MatcherOperand leftOperand, final MatcherOperand rightOperand) {
        this.leftOperand = Assert.requireNonNull(leftOperand, "leftOperand");
        this.rightOperand = Assert.requireNonNull(rightOperand, "rightOperand");
    }

    @Override
    public int getPrecedence() {
        return 2;
    }

    @Override
    public int getExecutionCost() {
        // left operand + right operand.
        return this.leftOperand.getExecutionCost() + this.rightOperand.getExecutionCost();
    }

    @Override
    public boolean isOperator() {
        return true;
    }

    @Override
    public boolean isIndex() {
        return false;
    }

    public MatcherOperand getLeftOperand() {
        return leftOperand;
    }

    public MatcherOperand getRightOperand() {
        return rightOperand;
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("(").append(this.leftOperand);
        sb.append(" AND ");
        sb.append(this.rightOperand).append(")");
        return sb.toString();
    }
}