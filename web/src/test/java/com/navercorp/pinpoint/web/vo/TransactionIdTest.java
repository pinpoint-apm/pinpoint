/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.web.vo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.Assert;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.web.vo.TransactionId;

public class TransactionIdTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void sameAll() {
        TransactionId id1 = new TransactionId("A1", 1, 1);
        TransactionId id2 = new TransactionId("A1", 1, 1);
        Assert.assertEquals(0, id1.compareTo(id2));
    }

    @Test
    public void diffAgentStartTimeAsc() {
        TransactionId id1 = new TransactionId("A1", 1, 1);
        TransactionId id2 = new TransactionId("A1", 2, 1);
        Assert.assertEquals(-1, id1.compareTo(id2));
    }

    @Test
    public void diffAgentStartTimeDesc() {
        TransactionId id1 = new TransactionId("A1", 2, 1);
        TransactionId id2 = new TransactionId("A1", 1, 1);
        Assert.assertEquals(1, id1.compareTo(id2));
    }

    @Test
    public void diffSeqAsc() {
        TransactionId id1 = new TransactionId("A1", 1, 1);
        TransactionId id2 = new TransactionId("A1", 1, 2);
        Assert.assertEquals(-1, id1.compareTo(id2));
    }

    @Test
    public void diffSeqDesc() {
        TransactionId id1 = new TransactionId("A1", 1, 2);
        TransactionId id2 = new TransactionId("A1", 1, 1);
        Assert.assertEquals(1, id1.compareTo(id2));
    }

    @Test
    public void order() {
        List<Integer> numbers = new ArrayList<Integer>(10);
        for (int i = 0; i < 10; i++) {
            numbers.add(i);
        }
        Collections.shuffle(numbers);

        List<TransactionId> list = new ArrayList<TransactionId>();
        for (int i = 0; i < 10; i++) {
            list.add(new TransactionId("A", 1, numbers.get(i)));
        }
        logger.debug("{}", list);

        SortedSet<TransactionId> set = new TreeSet<TransactionId>(list);
        for (int i = 0; i < 10; i++) {
            set.add(list.get(i));
        }

        logger.debug("{}", set);
    }
}
