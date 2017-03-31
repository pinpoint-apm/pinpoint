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
public class NotMatcherOperator extends AbstractMatcherOperand implements MatcherOperator  {
    private MatcherOperand rightOperand;

    @Override
    public int getPrecedence() {
        return 3;
    }

    @Override
    public boolean isOperator() {
        return true;
    }

    @Override
    public boolean isIndex() {
        return false;
    }

    @Override
    public MatcherOperand getLeftOperand() {
        return null;
    }

    @Override
    public void setLeftOperand(MatcherOperand leftOperand) {
        throw new UnsupportedOperationException("NotMatcherOperator does not have left operand.");
    }

    @Override
    public MatcherOperand getRightOperand() {
        return this.rightOperand;
    }

    @Override
    public void setRightOperand(MatcherOperand rightOperand) {
        this.rightOperand = rightOperand;
    }

    @Override
    public int getExecutionCost() {
        return this.rightOperand != null ? this.rightOperand.getExecutionCost() : 0;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("NOT");
        if (this.rightOperand != null) {
            sb.append(" (").append(this.rightOperand).append(")");
        }

        return sb.toString();
    }
}