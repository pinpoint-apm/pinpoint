/*
 * Copyright 2016 NAVER Corp.
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

package com.navercorp.pinpoint.common.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Taejin Koo
 */
public class FixedMaxSizeTreeSetTest {

    private final AtomicInteger idGenerator = new AtomicInteger();

    @Before
    public void setup() {
        idGenerator.set(0);
    }

    @Test
    public void basicFunctionTest1() throws Exception {
        int treeSetMaxSize = 3;
        int addEntrySize = treeSetMaxSize + 5;

        FixedMaxSizeTreeSet<SimpleEntry> fixedMaxSizeTreeSet = new FixedMaxSizeTreeSet<SimpleEntry>(treeSetMaxSize, new KeepOldElementComparator());

        long currentTimeMillis = System.currentTimeMillis();
        long diff = 100;

        for (int i = 0; i < addEntrySize; i++) {
            SimpleEntry simpleEntry = createSimpleEntry(currentTimeMillis + (diff * i));
            fixedMaxSizeTreeSet.add(simpleEntry);
        }

        List<SimpleEntry> list = fixedMaxSizeTreeSet.getList();
        Assert.assertEquals(treeSetMaxSize, list.size());
        for (int i = 0; i < list.size(); i++) {
            Assert.assertTrue(assertContainsId(i + 1, list));
        }
    }

    @Test
    public void basicFunctionTest2() throws Exception {
        int treeSetMaxSize = 3;
        int addEntrySize = treeSetMaxSize + 5;

        FixedMaxSizeTreeSet<SimpleEntry> fixedMaxSizeTreeSet = new FixedMaxSizeTreeSet<SimpleEntry>(treeSetMaxSize, new KeepOldElementComparator());

        long currentTimeMillis = System.currentTimeMillis();
        long diff = 100;

        for (int i = 0; i < addEntrySize; i++) {
            SimpleEntry simpleEntry = createSimpleEntry(currentTimeMillis - (diff * i));
            fixedMaxSizeTreeSet.add(simpleEntry);
        }

        List<SimpleEntry> list = fixedMaxSizeTreeSet.getList();
        Assert.assertEquals(treeSetMaxSize, list.size());
        for (int i = 0; i < list.size(); i++) {
            Assert.assertTrue(assertContainsId(addEntrySize - i, list));
        }
    }

    private SimpleEntry createSimpleEntry(long startTime) {
        return new SimpleEntry(idGenerator.incrementAndGet(), startTime);
    }

    private boolean assertContainsId(int id, List<SimpleEntry> simpleEntryList) {
        for (SimpleEntry entry : simpleEntryList) {
            if (id == entry.getId()) {
                return true;
            }
        }
        return false;
    }

    private static class SimpleEntry {

        private final int id;
        private final long startTime;

        public SimpleEntry(int id, long startTime) {
            this.id = id;
            this.startTime = startTime;
        }

        public int getId() {
            return id;
        }

        public long getStartTime() {
            return startTime;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("SimpleEntry{");
            sb.append("id=").append(id);
            sb.append(", startTime=").append(startTime);
            sb.append('}');
            return sb.toString();
        }

    }

    private static class KeepOldElementComparator implements Comparator<SimpleEntry> {

        private static final int CHANGE_TO_NEW_ELEMENT = 1;
        private static final int KEEP_OLD_ELEMENT = -1;

        @Override
        public int compare(SimpleEntry oldElement, SimpleEntry newElement) {
            long diff = oldElement.getStartTime() - newElement.getStartTime();
            if (diff <= 0) {
                return KEEP_OLD_ELEMENT;
            }

            return CHANGE_TO_NEW_ELEMENT;
        }

    }

}
