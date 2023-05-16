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
package com.navercorp.pinpoint.bootstrap.instrument.matcher.operand;

import com.navercorp.pinpoint.bootstrap.instrument.matcher.operator.AndMatcherOperator;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operator.NotMatcherOperator;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operator.OrMatcherOperator;
import com.navercorp.pinpoint.common.annotations.InterfaceStability;

/**
 * @author jaehong.kim
 */
@InterfaceStability.Unstable
public abstract class AbstractMatcherOperand implements MatcherOperand {
    public boolean isOperator() {
        return false;
    }

    // this and operand
    public MatcherOperand and(MatcherOperand operand) {
        return new AndMatcherOperator(this, operand);
    }

    // this or operand
    public MatcherOperand or(MatcherOperand operand) {
        return new OrMatcherOperator(this, operand);
    }

    // not this
    public MatcherOperand not() {
        return new NotMatcherOperator(this);
    }
}