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

/**
 * @author jaehong.kim
 */
public class AndMatcherOperator extends AbstractMatcherOperand implements MatcherOperator {
    private MatcherOperand leftOperand;
    private MatcherOperand rightOperand;

    @Override
    public int getPrecedence() {
        return 2;
    }

    @Override
    public int getExecutionCost() {
        // left operand + right operand.
        int executionCost = this.leftOperand != null ? this.leftOperand.getExecutionCost() : 0;
        executionCost += this.rightOperand != null ? this.rightOperand.getExecutionCost() : 0;

        return executionCost;
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

    public void setLeftOperand(MatcherOperand leftOperand) {
        this.leftOperand = leftOperand;
    }

    public MatcherOperand getRightOperand() {
        return rightOperand;
    }

    public void setRightOperand(MatcherOperand rightOperand) {
        this.rightOperand = rightOperand;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (this.leftOperand != null) {
            sb.append("(").append(this.leftOperand).append(" ");
        }
        sb.append("AND");
        if (this.rightOperand != null) {
            sb.append(" ").append(this.rightOperand).append(")");
        }

        return sb.toString();
    }
}