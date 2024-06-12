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

import com.navercorp.pinpoint.common.id.AgentId;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TransactionIdComparatorTest {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final Comparator<TransactionId> comparator = TransactionIdComparator.INSTANCE;

    @Test
    public void sameAll() {
        TransactionId id1 = new TransactionId(AgentId.of("A1"), 1, 1);
        TransactionId id2 = new TransactionId(AgentId.of("A1"), 1, 1);
        Assertions.assertEquals(0, comparator.compare(id1, id2));
    }

    @Test
    public void diffAgentStartTimeAsc() {
        TransactionId id1 = new TransactionId(AgentId.of("A1"), 1, 1);
        TransactionId id2 = new TransactionId(AgentId.of("A1"), 2, 1);
        Assertions.assertEquals(-1, comparator.compare(id1, id2));
    }

    @Test
    public void diffAgentStartTimeDesc() {
        TransactionId id1 = new TransactionId(AgentId.of("A1"), 2, 1);
        TransactionId id2 = new TransactionId(AgentId.of("A1"), 1, 1);

        Assertions.assertEquals(1, comparator.compare(id1, id2));
    }

    @Test
    public void diffSeqAsc() {
        TransactionId id1 = new TransactionId(AgentId.of("A1"), 1, 1);
        TransactionId id2 = new TransactionId(AgentId.of("A1"), 1, 2);
        Assertions.assertEquals(-1, comparator.compare(id1, id2));
    }

    @Test
    public void diffSeqDesc() {
        TransactionId id1 = new TransactionId(AgentId.of("A1"), 1, 2);
        TransactionId id2 = new TransactionId(AgentId.of("A1"), 1, 1);
        Assertions.assertEquals(1, comparator.compare(id1, id2));
    }

    @Test
    public void order() {
        List<Integer> numbers = new ArrayList<>(10);
        for (int i = 0; i < 10; i++) {
            numbers.add(i);
        }
        Collections.shuffle(numbers);

        List<TransactionId> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            list.add(new TransactionId(AgentId.of("A"), 1, numbers.get(i)));
        }
        logger.debug("{}", list);

        list.sort(comparator);
        int i = 0;
        for (TransactionId transactionId : list) {
            Assertions.assertEquals(i, transactionId.getTransactionSequence());
            i++;
        }
        logger.debug("{}", list);
    }
}
