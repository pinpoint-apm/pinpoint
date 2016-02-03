/*
 *  Copyright 2016 NAVER Corp.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.navercorp.pinpoint.web.scatter;

import com.navercorp.pinpoint.web.vo.TransactionId;
import com.navercorp.pinpoint.web.vo.scatter.Dot;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @Author Taejin Koo
 */
public class ScatterDataTest {

    @Test
    public void addDotTest() throws Exception {
        int count = 100;

        long from = 1000;
        int xGroupUnit = 100;
        int yGroupUnit = 100;

        ScatterData scatterData = new ScatterData(from, xGroupUnit, yGroupUnit);
        List<Dot> dotList = createDotList("agent", "transactionAgent", count, from);
        for (Dot dot : dotList) {
            scatterData.addDot(dot);
        }

        List<Dot> dots = extractDotList(scatterData);
        Assert.assertEquals(count, dots.size());
    }

    @Test
    public void mergeTest() throws Exception {
        int count = 100;

        long from = 1000;
        int xGroupUnit = 100;
        int yGroupUnit = 100;

        ScatterData scatterData = new ScatterData(from, xGroupUnit, yGroupUnit);
        List<Dot> dotList = createDotList("agent", "transactionAgent", count, from);
        for (Dot dot : dotList) {
            ScatterData newScatterData = new ScatterData(from, xGroupUnit, yGroupUnit);
            newScatterData.addDot(dot);
            scatterData.merge(newScatterData);
        }

        List<Dot> dots = extractDotList(scatterData);
        Assert.assertEquals(count, dots.size());
    }

    private List<Dot> createDotList(String agentId, String transactionAgentId, int createSize, long from) {
        long currentTime = System.currentTimeMillis();

        List<TransactionId> transactionIdList = new ArrayList<>(createSize);
        for (int i = 0; i<createSize; i++) {
            transactionIdList.add(new TransactionId(transactionAgentId, currentTime, i));
        }

        long acceptedTime = Math.max(Math.abs(ThreadLocalRandom.current().nextLong(Long.MAX_VALUE)), from);
        int executionTime = (int) Math.abs(ThreadLocalRandom.current().nextLong(60 * 1000));

        List<Dot> dotList = new ArrayList<>(createSize);
        for (int i = 0; i<createSize; i++) {
            dotList.add(new Dot(transactionIdList.get(i), acceptedTime, executionTime, Dot.EXCEPTION_NONE, agentId));
        }

        return dotList;
    }

    private List<Dot> extractDotList(ScatterData scatterData) {
        List<Dot> dotList = new ArrayList<>();
        for (AgentScatterData eachAgentScatterData : scatterData.getAgentScatterDataMap().values()) {
            dotList.addAll(extractDotList(eachAgentScatterData));
        }

        return dotList;
    }

    private List<Dot> extractDotList(AgentScatterData agentScatterData) {
        List<Dot> dotList = new ArrayList<>();
        for (TransactionAgentScatterData transactionAgentScatterData : agentScatterData.getTransactionAgentScatterDataMap().values()) {
            dotList.addAll(extractDotList(transactionAgentScatterData));
        }

        return dotList;
    }

    private List<Dot> extractDotList(TransactionAgentScatterData transactionAgentScatterData) {
        List<Dot> dotList = new ArrayList<>();
        for (DotGroup dotGroup : transactionAgentScatterData.getDotGroupMap().values()) {
            dotList.addAll(extractDotList(dotGroup));
        }

        return dotList;
    }

    private List<Dot> extractDotList(DotGroup dotGroup) {
        List<Dot> dotList = dotGroup.getDotList();
        return dotList;
    }

}
