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

package com.navercorp.pinpoint.profiler.util;

import com.navercorp.pinpoint.common.util.FixedMaxSizeTreeSet;
import com.navercorp.pinpoint.thrift.dto.command.TActiveThreadDump;
import com.navercorp.pinpoint.thrift.dto.command.TActiveThreadLightDump;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Taejin Koo
 */
public class ActiveThreadDumpUtilsTest {

    private final AtomicInteger idGenerator = new AtomicInteger();

    @Before
    public void setup() {
        idGenerator.set(0);
    }

    @Test
    public void tActiveThreadDumpTest1() throws Exception {
        int treeSetMaxSize = 3;
        int addEntrySize = treeSetMaxSize + 5;

        FixedMaxSizeTreeSet<TActiveThreadDump> tActiveThreadDumpFixedMaxSizeTreeSet = new FixedMaxSizeTreeSet<TActiveThreadDump>(treeSetMaxSize, ActiveThreadDumpUtils.getDumpComparator());

        long currentTimeMillis = System.currentTimeMillis();
        long diff = 100;

        for (int i = 0; i < addEntrySize; i++) {
            TActiveThreadDump activeThreadDump = createTActiveThreadDump(currentTimeMillis + (diff * i));
            tActiveThreadDumpFixedMaxSizeTreeSet.add(activeThreadDump);
        }

        List<TActiveThreadDump> list = tActiveThreadDumpFixedMaxSizeTreeSet.getList();
        Assert.assertEquals(treeSetMaxSize, list.size());
        for (int i = 0; i < list.size(); i++) {
            Assert.assertTrue(assertContainsActiveThreadDumpId(i + 1, list));
        }
    }

    @Test
    public void tActiveThreadDumpTest2() throws Exception {
        int treeSetMaxSize = 3;
        int addEntrySize = treeSetMaxSize + 5;

        FixedMaxSizeTreeSet<TActiveThreadDump> tActiveThreadDumpFixedMaxSizeTreeSet = new FixedMaxSizeTreeSet<TActiveThreadDump>(treeSetMaxSize, ActiveThreadDumpUtils.getDumpComparator());

        long currentTimeMillis = System.currentTimeMillis();
        long diff = 100;

        for (int i = 0; i < addEntrySize; i++) {
            TActiveThreadDump activeThreadDump = createTActiveThreadDump(currentTimeMillis - (diff * i));
            tActiveThreadDumpFixedMaxSizeTreeSet.add(activeThreadDump);
        }

        List<TActiveThreadDump> list = tActiveThreadDumpFixedMaxSizeTreeSet.getList();
        Assert.assertEquals(treeSetMaxSize, list.size());
        for (int i = 0; i < list.size(); i++) {
            Assert.assertTrue(assertContainsActiveThreadDumpId(addEntrySize - i, list));
        }
    }

    @Test
    public void tActiveThreadDumpTest3() throws Exception {
        int treeSetMaxSize = 3;
        int addEntrySize = treeSetMaxSize + 5;

        FixedMaxSizeTreeSet<TActiveThreadDump> tActiveThreadDumpFixedMaxSizeTreeSet = new FixedMaxSizeTreeSet<TActiveThreadDump>(treeSetMaxSize, ActiveThreadDumpUtils.getDumpComparator());

        long currentTimeMillis = System.currentTimeMillis();

        for (int i = 0; i < addEntrySize; i++) {
            TActiveThreadDump activeThreadDump = createTActiveThreadDump(currentTimeMillis);
            tActiveThreadDumpFixedMaxSizeTreeSet.add(activeThreadDump);
        }

        List<TActiveThreadDump> list = tActiveThreadDumpFixedMaxSizeTreeSet.getList();

        Assert.assertEquals(treeSetMaxSize, list.size());
        for (int i = 0; i < list.size(); i++) {
            Assert.assertTrue(assertContainsActiveThreadDumpId(i + 1, list));
        }
    }

    private TActiveThreadDump createTActiveThreadDump(long startTime) {
        TActiveThreadDump tActiveThreadDump = new TActiveThreadDump();
        tActiveThreadDump.setStartTime(startTime);
        tActiveThreadDump.setLocalTraceId(idGenerator.incrementAndGet());
        return tActiveThreadDump;
    }

    private boolean assertContainsActiveThreadDumpId(int id, List<TActiveThreadDump> activeThreadDumpList) {
        for (TActiveThreadDump activeThreadDump : activeThreadDumpList) {
            if (id == activeThreadDump.getLocalTraceId()) {
                return true;
            }
        }
        return false;
    }

    @Test
    public void tActiveThreadLightDumpTest1() throws Exception {
        int treeSetMaxSize = 3;
        int addEntrySize = treeSetMaxSize + 5;

        FixedMaxSizeTreeSet<TActiveThreadLightDump> tActiveThreadDumpFixedMaxSizeTreeSet = new FixedMaxSizeTreeSet<TActiveThreadLightDump>(treeSetMaxSize, ActiveThreadDumpUtils.getLightDumpComparator());

        long currentTimeMillis = System.currentTimeMillis();
        long diff = 100;

        for (int i = 0; i < addEntrySize; i++) {
            TActiveThreadLightDump activeThreadDump = createTActiveThreadLightDump(currentTimeMillis + (diff * i));
            tActiveThreadDumpFixedMaxSizeTreeSet.add(activeThreadDump);
        }

        List<TActiveThreadLightDump> list = tActiveThreadDumpFixedMaxSizeTreeSet.getList();
        Assert.assertEquals(treeSetMaxSize, list.size());
        for (int i = 0; i < list.size(); i++) {
            Assert.assertTrue(assertContainsActiveThreadLightDumpId(i + 1, list));
        }
    }

    @Test
    public void tActiveThreadLightDumpTest2() throws Exception {
        int treeSetMaxSize = 3;
        int addEntrySize = treeSetMaxSize + 5;

        FixedMaxSizeTreeSet<TActiveThreadLightDump> tActiveThreadDumpFixedMaxSizeTreeSet = new FixedMaxSizeTreeSet<TActiveThreadLightDump>(treeSetMaxSize, ActiveThreadDumpUtils.getLightDumpComparator());

        long currentTimeMillis = System.currentTimeMillis();
        long diff = 100;

        for (int i = 0; i < addEntrySize; i++) {
            TActiveThreadLightDump activeThreadDump = createTActiveThreadLightDump(currentTimeMillis - (diff * i));
            tActiveThreadDumpFixedMaxSizeTreeSet.add(activeThreadDump);
        }

        List<TActiveThreadLightDump> list = tActiveThreadDumpFixedMaxSizeTreeSet.getList();
        Assert.assertEquals(treeSetMaxSize, list.size());
        for (int i = 0; i < list.size(); i++) {
            Assert.assertTrue(assertContainsActiveThreadLightDumpId(addEntrySize - i, list));
        }
    }

    @Test
    public void tActiveThreadLightDumpTest3() throws Exception {
        int treeSetMaxSize = 3;
        int addEntrySize = treeSetMaxSize + 5;

        FixedMaxSizeTreeSet<TActiveThreadLightDump> tActiveThreadDumpFixedMaxSizeTreeSet = new FixedMaxSizeTreeSet<TActiveThreadLightDump>(treeSetMaxSize, ActiveThreadDumpUtils.getLightDumpComparator());

        long currentTimeMillis = System.currentTimeMillis();

        for (int i = 0; i < addEntrySize; i++) {
            TActiveThreadLightDump activeThreadDump = createTActiveThreadLightDump(currentTimeMillis);
            tActiveThreadDumpFixedMaxSizeTreeSet.add(activeThreadDump);
        }

        List<TActiveThreadLightDump> list = tActiveThreadDumpFixedMaxSizeTreeSet.getList();

        Assert.assertEquals(treeSetMaxSize, list.size());
        for (int i = 0; i < list.size(); i++) {
            Assert.assertTrue(assertContainsActiveThreadLightDumpId(i + 1, list));
        }
    }

    private TActiveThreadLightDump createTActiveThreadLightDump(long startTime) {
        TActiveThreadLightDump tActiveThreadDump = new TActiveThreadLightDump();
        tActiveThreadDump.setStartTime(startTime);
        tActiveThreadDump.setLocalTraceId(idGenerator.incrementAndGet());
        return tActiveThreadDump;
    }

    private boolean assertContainsActiveThreadLightDumpId(int id, List<TActiveThreadLightDump> activeThreadDumpList) {
        for (TActiveThreadLightDump activeThreadDump : activeThreadDumpList) {
            if (id == activeThreadDump.getLocalTraceId()) {
                return true;
            }
        }
        return false;
    }

}
