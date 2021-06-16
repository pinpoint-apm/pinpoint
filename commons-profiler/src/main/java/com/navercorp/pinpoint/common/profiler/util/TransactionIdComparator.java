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
    public int compare(TransactionId tid1, TransactionId tid2) {
        int agentIdComp = tid1.getAgentId().compareTo(tid2.getAgentId());
        if (agentIdComp != 0) {
            return agentIdComp;
        }

        int agentStartTimeComp = Long.compare(tid1.getAgentStartTime(), tid2.getAgentStartTime());
        if (agentStartTimeComp != 0) {
            return agentStartTimeComp;
        }

        return Long.compare(tid1.getTransactionSequence(), tid2.getTransactionSequence());
    }

}
