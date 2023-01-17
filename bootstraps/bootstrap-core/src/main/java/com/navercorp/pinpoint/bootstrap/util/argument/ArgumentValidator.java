/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.bootstrap.util.argument;

import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ArgumentValidator implements Validator {
    private final Predicate[] predicates;

    private final int minArgsSize;

    public ArgumentValidator(List<Predicate> argumentList) {
        this(argumentList, 0);
    }

    public ArgumentValidator(List<Predicate> predicates, int minArgsSize) {
        this.predicates = predicates.toArray(new Predicate[0]);
        this.minArgsSize = minIndex(this.predicates, minArgsSize);
    }

    private static int minIndex(Predicate[] predicates, int minArgsSize) {
        if (minArgsSize != 0) {
            return minArgsSize;
        }
        int max = -1;
        for (Predicate predicate : predicates) {
            max = Math.max(max, predicate.index());
        }
        return max + 1;
    }

    @Override
    public boolean validate(Object[] args) {
        if (args == null) {
            return false;
        }
        if (args.length < minArgsSize) {
            return false;
        }

        for (Predicate predicate : this.predicates) {
            if (!predicate.test(args)) {
                return false;
            }
        }
        return true;
    }
}
