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
package com.navercorp.pinpoint.profiler.instrument.transformer;

import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.MatcherOperand;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operator.AndMatcherOperator;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operator.MatcherOperator;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operator.NotMatcherOperator;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operator.OrMatcherOperator;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jaehong.kim
 */
public class TransformerMatcherExecutionPlanner {

    public List<MatcherOperand> findIndex(final MatcherOperand operand) {
        final List<MatcherOperand> index = new ArrayList<MatcherOperand>();
        traversal(operand, index);

        return index;
    }

    // find indexed operands.
    private boolean traversal(final MatcherOperand operand, final List<MatcherOperand> index) {
        if (!operand.isOperator()) {
            if (operand.isIndex()) {
                index.add(operand);
                return true;
            }
            return false;
        }

        if (operand instanceof NotMatcherOperator) {
            // skip NOT operator.
            return false;
        }

        MatcherOperator operator = (MatcherOperator) operand;
        final MatcherOperand leftOperand = operator.getLeftOperand();
        if (leftOperand == null) {
            throw new IllegalArgumentException("invalid left operand - left operand must not be null. operator=" + operator);
        }

        final MatcherOperand rightOperand = operator.getRightOperand();
        if (rightOperand == null) {
            throw new IllegalArgumentException("invalid right operand - right operand must not be null. operator=" + operator);
        }

        if (operand instanceof AndMatcherOperator) {
            // if find any.
            final boolean indexed = traversal(leftOperand, index);
            if (indexed) {
                return true;
            }
            return traversal(rightOperand, index);
        } else if (operand instanceof OrMatcherOperator) {
            // find all.
            final boolean indexed = traversal(leftOperand, index);
            return traversal(rightOperand, index) || indexed;
        } else {
            throw new IllegalArgumentException("unknown operator. operator=" + operand);
        }
    }
}