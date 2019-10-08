/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.common.profiler.util;

import java.util.Comparator;

/**
 * @author Woonduk Kang(emeroad)
 */
public class TransactionIdComparator implements Comparator<TransactionId> {

    public static final TransactionIdComparator INSTANCE = new TransactionIdComparator();

    @Override
    public int compare(TransactionId o1, TransactionId o2) {
        int r1 = o1.getAgentId().compareTo(o2.getAgentId());
        if (r1 == 0) {
            if (o1.getAgentStartTime() > o2.getAgentStartTime()) {
                return 1;
            } else if (o1.getAgentStartTime() < o2.getAgentStartTime()) {
                return -1;
            } else {
                if (o1.getTransactionSequence() > o2.getTransactionSequence()) {
                    return 1;
                } else if (o1.getTransactionSequence() < o2.getTransactionSequence()) {
                    return -1;
                } else {
                    return 0;
                }
            }
        } else {
            return r1;
        }
    }

}
