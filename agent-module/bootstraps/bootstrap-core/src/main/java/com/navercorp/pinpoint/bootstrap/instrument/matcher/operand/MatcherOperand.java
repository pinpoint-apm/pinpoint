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

import com.navercorp.pinpoint.common.annotations.InterfaceStability;

/**
 * @author jaehong.kim
 */
@InterfaceStability.Unstable
public interface MatcherOperand {

    boolean isOperator();

    // for execution plan.
    int getExecutionCost();
    boolean isIndex();

    // this and operand
    MatcherOperand and(MatcherOperand operand);

    // this or operand
    MatcherOperand or(MatcherOperand operand);

    // not this
    MatcherOperand not();
}