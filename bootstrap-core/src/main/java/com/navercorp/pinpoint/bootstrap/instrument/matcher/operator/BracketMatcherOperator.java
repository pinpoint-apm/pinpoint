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

/**
 * @author jaehong.kim
 */
@InterfaceStability.Unstable
public class BracketMatcherOperator extends AbstractMatcherOperand implements MatcherOperator {
    @Override
    public int getPrecedence() {
        return -1;
    }

    @Override
    public int getExecutionCost() {
        return 0;
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
    public MatcherOperand getRightOperand() {
        return null;
    }

    @Override
    public MatcherOperand and(MatcherOperand operand) {
        throw new UnsupportedOperationException("BracketMatcherOperator is not an operation target.");
    }

    @Override
    public MatcherOperand or(MatcherOperand operand) {
        throw new UnsupportedOperationException("BracketMatcherOperator is not an operation target.");
    }

    @Override
    public MatcherOperand not() {
        throw new UnsupportedOperationException("BracketMatcherOperator is not an operation target.");
    }

    public String toString() {
        return "(";
    }
}