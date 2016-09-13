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

package com.navercorp.pinpoint.common.server.bo;


import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.OffsetFixedBuffer;
import com.navercorp.pinpoint.common.server.bo.PassiveSpanBo;
import com.navercorp.pinpoint.common.server.bo.RandomTSpan;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanFactory;
import com.navercorp.pinpoint.common.server.bo.serializer.trace.v1.SpanSerializer;
import com.navercorp.pinpoint.common.server.bo.serializer.trace.v2.SpanDecoder;
import com.navercorp.pinpoint.common.server.bo.serializer.trace.v2.SpanDecoderV0;
import com.navercorp.pinpoint.common.server.bo.serializer.trace.v2.SpanSerializerV2;
import com.navercorp.pinpoint.common.trace.LoggingInfo;
import com.navercorp.pinpoint.common.util.TransactionId;
import com.navercorp.pinpoint.common.util.TransactionIdUtils;
import com.navercorp.pinpoint.thrift.dto.TPassiveSpan;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.common.trace.ServiceType;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * @author Peter Chen
 */
public class PassiveSpanBoTest {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Random random = new Random();

    private final SpanSerializer spanSerializer = new SpanSerializer();
    private final SpanSerializerV2 spanSerializerV2 = new SpanSerializerV2();
    private final SpanDecoder spanDecoder = new SpanDecoderV0();

    private final RandomTSpan randomTSpan = new RandomTSpan();
    private final SpanFactory spanFactory = new SpanFactory();

    @Test
    public void testVersion() {
        PassiveSpanBo passiveSpanBo = new PassiveSpanBo();
        checkVersion(passiveSpanBo, 0);
        checkVersion(passiveSpanBo, 254);
        checkVersion(passiveSpanBo, 255);
        try {
            checkVersion(passiveSpanBo, 256);
            Assert.fail();
        } catch (Exception ignored) {
        }

        byte byteVersion = 2;
        passiveSpanBo.setVersion(byteVersion);
        Assert.assertTrue(passiveSpanBo.getRawVersion() == byteVersion);

    }

    private void checkVersion(PassiveSpanBo passiveSpanBo, int v) {
        passiveSpanBo.setVersion(v);
        int version = passiveSpanBo.getVersion();

        Assert.assertEquals(v, version);
    }


    @Test
    public void testGetterSetter() {
        PassiveSpanBo passiveSpanBo = new PassiveSpanBo();

        // passiveSpanId
        passiveSpanBo.setPassiveSpanId(100001);
        Assert.assertEquals(passiveSpanBo.getPassiveSpanId(), 100001);

        // parentSpanId
        passiveSpanBo.setParentSpanId(10);
        Assert.assertEquals(passiveSpanBo.getParentSpanId(), 10);

        // nextSpanId
        passiveSpanBo.setNextSpanId(100);
        Assert.assertEquals(passiveSpanBo.getNextSpanId(), 100);

        // remoteAddr
        passiveSpanBo.setRemoteAddr("remoteAddr");
        Assert.assertEquals(passiveSpanBo.getRemoteAddr(), "remoteAddr");

        // transactionId
        passiveSpanBo.setTransactionId(TransactionIdUtils.parseTransactionId("test-agent^1461811447435^1"));
        Assert.assertEquals(passiveSpanBo.getTransactionId(), TransactionIdUtils.parseTransactionId("test-agent^1461811447435^1"));

        // errCode
        passiveSpanBo.setErrCode(100);
        Assert.assertEquals(passiveSpanBo.getErrCode(), 100);

        // acceptorHost
        passiveSpanBo.setAcceptorHost("acceptorHost");
        Assert.assertEquals(passiveSpanBo.getAcceptorHost(), "acceptorHost");

        // agentStartTime
        passiveSpanBo.setAgentStartTime(12345);
        Assert.assertEquals(passiveSpanBo.getAgentStartTime(), 12345);

        passiveSpanBo.setApiId(10);
        Assert.assertEquals(passiveSpanBo.getApiId(), 10);

        passiveSpanBo.setApplicationServiceType((short)2);
        Assert.assertEquals(passiveSpanBo.getApplicationServiceType(), (short)2);

        passiveSpanBo.setCollectorAcceptTime(1000);
        Assert.assertEquals(passiveSpanBo.getCollectorAcceptTime(), 1000);

        passiveSpanBo.setElapsed(99);
        Assert.assertEquals(passiveSpanBo.getElapsed(), 99);

        passiveSpanBo.setEndPoint("endPoint");
        Assert.assertEquals(passiveSpanBo.getEndPoint(), "endPoint");

        passiveSpanBo.setRpc("rpc");
        Assert.assertEquals(passiveSpanBo.getRpc(), "rpc");

        // fake
        SpanBo fakeSpanBo = passiveSpanBo.createFakeSpanBo();
        Assert.assertEquals(passiveSpanBo.getParentSpanId(), fakeSpanBo.getParentSpanId());
        Assert.assertEquals(passiveSpanBo.getPassiveSpanId(), fakeSpanBo.getSpanId());
        Assert.assertEquals(fakeSpanBo.getRemoteAddr(), "remoteAddr");
        Assert.assertEquals(fakeSpanBo.getTransactionId(), TransactionIdUtils.parseTransactionId("test-agent^1461811447435^1"));
        Assert.assertEquals(fakeSpanBo.getErrCode(), 100);
        Assert.assertEquals(fakeSpanBo.getAcceptorHost(), "acceptorHost");
        Assert.assertEquals(fakeSpanBo.getAgentStartTime(), 12345);
        Assert.assertEquals(fakeSpanBo.getApiId(), 10);
        Assert.assertEquals(fakeSpanBo.getApplicationServiceType(), (short)2);
        Assert.assertEquals(fakeSpanBo.getCollectorAcceptTime(), 1000);
        Assert.assertEquals(fakeSpanBo.getElapsed(), 99);
        Assert.assertEquals(fakeSpanBo.getEndPoint(), "endPoint");
        Assert.assertEquals(fakeSpanBo.getRpc(), "rpc");
    }

    @Test
    public void testToString() {
        PassiveSpanBo passiveSpanBo = new PassiveSpanBo();
        passiveSpanBo.toString();

        passiveSpanBo = randomPassiveSpan();
        passiveSpanBo.toString();
    }

    @Test
    public void testMergePassiveSpan() throws Exception {
        // 1, first --> second  to  first --> fakespan --> second
        SpanBo first = new SpanBo();
        SpanBo second = new SpanBo();
        PassiveSpanBo passiveSpanBo = new PassiveSpanBo();

        SpanEventBo firstEvent1 = new SpanEventBo();
        SpanEventBo firstEvent2 = new SpanEventBo();
        firstEvent1.setNextSpanId(3);
        firstEvent2.setNextSpanId(1000);

        List<SpanEventBo> fistSpanEventBoList= new ArrayList<>();
        fistSpanEventBoList.add(firstEvent1);
        fistSpanEventBoList.add(firstEvent2);

        first.setSpanId(1);
        first.addSpanEventBoList(fistSpanEventBoList);
        second.setSpanId(3);

        SpanEventBo passiveEvent1 = new SpanEventBo();
        SpanEventBo passiveEvent2 = new SpanEventBo();
        passiveEvent1.setNextSpanId(3);
        passiveEvent2.setNextSpanId(1000);
        passiveSpanBo.setPassiveSpanId(2);
        passiveSpanBo.setParentSpanId(1);
        passiveSpanBo.setNextSpanId(3);
        passiveSpanBo.addPassiveSpanEventBo(passiveEvent1);
        passiveSpanBo.addPassiveSpanEventBo(passiveEvent2);
        passiveSpanBo.setStartTime(1);

        List<SpanBo> spanBoList = new ArrayList<>();
        spanBoList.add(first);
        spanBoList.add(second);

        List<PassiveSpanBo> passiveSpanBoList = new ArrayList<>();
        passiveSpanBoList.add(passiveSpanBo);

        PassiveSpanBo.mergePassiveSpan(spanBoList, passiveSpanBoList);

        Assert.assertEquals(spanBoList.size(), 3);

        first = spanBoList.get(0);
        second = spanBoList.get(1);
        SpanBo fake = spanBoList.get(2);

        Assert.assertEquals(first.getSpanEventBoList().get(0).getNextSpanId(), fake.getSpanId());
        Assert.assertNotEquals(first.getSpanEventBoList().get(1).getNextSpanId(), fake.getSpanId());

        Assert.assertEquals(second.getParentSpanId(), fake.getSpanId());
        Assert.assertEquals(fake.getSpanEventBoList().get(0).getNextSpanId(), second.getSpanId());
        Assert.assertNotEquals(fake.getSpanEventBoList().get(1).getNextSpanId(), second.getSpanId());
    }

    @Test
    public void testMergePassiveSpan_twoInOneChain() throws Exception {
        // 1, first --> second  to  first --> fakespan1 --> fakespan2 --> second
        SpanBo first = new SpanBo();
        SpanBo second = new SpanBo();
        PassiveSpanBo passiveSpanBo = new PassiveSpanBo();
        PassiveSpanBo passiveSpanBo2 = new PassiveSpanBo();

        SpanEventBo firstEvent1 = new SpanEventBo();
        SpanEventBo firstEvent2 = new SpanEventBo();
        firstEvent1.setNextSpanId(3);
        firstEvent2.setNextSpanId(1000);

        List<SpanEventBo> fistSpanEventBoList= new ArrayList<>();
        fistSpanEventBoList.add(firstEvent1);
        fistSpanEventBoList.add(firstEvent2);

        first.setSpanId(1);
        first.addSpanEventBoList(fistSpanEventBoList);
        second.setSpanId(3);

        SpanEventBo passiveEvent1 = new SpanEventBo();
        SpanEventBo passiveEvent2 = new SpanEventBo();
        passiveEvent1.setNextSpanId(3);
        passiveEvent2.setNextSpanId(1000);
        passiveSpanBo.setPassiveSpanId(2);
        passiveSpanBo.setParentSpanId(1);
        passiveSpanBo.setNextSpanId(3);
        passiveSpanBo.addPassiveSpanEventBo(passiveEvent1);
        passiveSpanBo.addPassiveSpanEventBo(passiveEvent2);
        passiveSpanBo.setStartTime(10);

        SpanEventBo passive2Event1 = new SpanEventBo();
        SpanEventBo passive2Event2 = new SpanEventBo();
        passive2Event1.setNextSpanId(3);
        passive2Event2.setNextSpanId(1000);
        passiveSpanBo2.setPassiveSpanId(4);
        passiveSpanBo2.setParentSpanId(1);
        passiveSpanBo2.setNextSpanId(3);
        passiveSpanBo2.addPassiveSpanEventBo(passive2Event1);
        passiveSpanBo2.addPassiveSpanEventBo(passive2Event2);
        passiveSpanBo2.setStartTime(1);

        List<SpanBo> spanBoList = new ArrayList<>();
        spanBoList.add(first);
        spanBoList.add(second);

        List<PassiveSpanBo> passiveSpanBoList = new ArrayList<>();
        passiveSpanBoList.add(passiveSpanBo);
        passiveSpanBoList.add(passiveSpanBo2);

        PassiveSpanBo.mergePassiveSpan(spanBoList, passiveSpanBoList);

        Assert.assertEquals(spanBoList.size(), 4);

        first = spanBoList.get(0);
        second = spanBoList.get(1);
        SpanBo fake2 = spanBoList.get(2);
        SpanBo fake = spanBoList.get(3);

        // 1,3,4,2
        Assert.assertEquals(first.getSpanId(), 1);
        Assert.assertEquals(second.getSpanId(), 3);
        Assert.assertEquals(fake2.getSpanId(), 4);
        Assert.assertEquals(fake.getSpanId(), 2);

        // sorted by startTime
        Assert.assertTrue(fake2.getStartTime() < fake.getStartTime());

        // first --> fake2 --> fake --> second
        Assert.assertEquals(first.getSpanEventBoList().get(0).getNextSpanId(), fake2.getSpanId());
        Assert.assertNotEquals(first.getSpanEventBoList().get(1).getNextSpanId(), fake2.getSpanId());
        Assert.assertEquals(fake2.getParentSpanId(), first.getSpanId());

        Assert.assertEquals(fake2.getSpanEventBoList().get(0).getNextSpanId(), fake.getSpanId());
        Assert.assertNotEquals(fake2.getSpanEventBoList().get(1).getNextSpanId(), fake.getSpanId());
        Assert.assertEquals(fake.getParentSpanId(), fake2.getSpanId());

        Assert.assertEquals(fake.getSpanEventBoList().get(0).getNextSpanId(), second.getSpanId());
        Assert.assertNotEquals(fake.getSpanEventBoList().get(1).getNextSpanId(), second.getSpanId());
        Assert.assertEquals(second.getParentSpanId(), fake.getSpanId());

    }

    @Test
    public void testMergePassiveSpan_random() throws Exception {

        for (int i = 0; i < 10; i++) {
            List<SpanBo> spanBoList = createRandomSpanBoList(RandomUtils.nextInt(3, 10));
            List<SpanBo> spanBoListBySeq = sortSpanBoListBySequence(spanBoList);
            List<PassiveSpanBo> passiveSpanBoList = createRandomPassiveSpanBoList(spanBoListBySeq);

            Collections.shuffle(spanBoListBySeq);
            Collections.shuffle(passiveSpanBoList);

            PassiveSpanBo.mergePassiveSpan(spanBoListBySeq, passiveSpanBoList);

            List<SpanBo> result = sortSpanBoListBySequence(spanBoListBySeq);

            // check
            for (int j = result.size() - 1; j >= 1; j--) {
                Assert.assertEquals(result.get(j).getParentSpanId(), result.get(j - 1).getSpanId());
            }

            Assert.assertEquals(result.get(0).getParentSpanId(), -1);

            // check startTime
            long prevStartTime = -1;
            for (SpanBo spanBo : result) {
                if (spanBo.getStartTime() == -1) {
                    // spanBo : pass
                    continue;
                }

                // fakeSpanBo
                Assert.assertTrue(spanBo.getStartTime() > prevStartTime);
                prevStartTime = spanBo.getStartTime();
            }
        }
    }

    // length <= 1000
    // spanId = [100, 1100]
    // we do't care spanId = [10000, 100000]
    // passiveSpanId = [5000, 6000]
    private List<SpanBo> createRandomSpanBoList(int length) {
        long spanIdIndex = 100;

        List<SpanBo> spanBoList = new ArrayList<>();

        for (int i = 0; i < length; i++) {
            final SpanBo spanBo = new SpanBo();

            spanBo.setSpanId(spanIdIndex);

            if (i == 0) {
                spanBo.setParentSpanId(-1);
            } else {
                spanBo.setParentSpanId(spanIdIndex - 1);
            }

            int eventNum = RandomUtils.nextInt(1, 11);
            int nextEventIndex = RandomUtils.nextInt(0, eventNum);
            for (int j = 0; j < eventNum; j++) {
                SpanEventBo spanEventBo = new SpanEventBo();
                if (j == nextEventIndex) {
                    spanEventBo.setNextSpanId(spanIdIndex + 1);
                } else {
                    spanEventBo.setNextSpanId(RandomUtils.nextInt(10000, 100000));
                }
                spanBo.addSpanEvent(spanEventBo);
            }

            spanIdIndex++;

            // we do't care spanBo's startTime
            spanBo.setStartTime(-1);

            spanBoList.add(spanBo);
        }

        return spanBoList;
    }

    private List<PassiveSpanBo> createRandomPassiveSpanBoList(List<SpanBo> spanBoListBySeq) {

        long passiveSpanId = 5000;
        List<PassiveSpanBo> passiveSpanBoList = new ArrayList<>();

        for (int i = 1; i < spanBoListBySeq.size(); i++) {
            int rand = RandomUtils.nextInt(1, 11);

            // p = 0.6, have passiveSpan
            if (rand <= 6) {
                int numInChain = RandomUtils.nextInt(1, 4);
                for (int j = 0; j < numInChain; j++) {
                    PassiveSpanBo passiveSpanBo = new PassiveSpanBo();
                    passiveSpanBo.setPassiveSpanId(passiveSpanId++);
                    passiveSpanBo.setParentSpanId(spanBoListBySeq.get(i - 1).getSpanId());
                    passiveSpanBo.setNextSpanId(spanBoListBySeq.get(i).getSpanId());

                    int eventNum = RandomUtils.nextInt(1, 11);
                    int nextEventIndex = RandomUtils.nextInt(0, eventNum);
                    for (int k = 0; k < eventNum; k++) {
                        SpanEventBo spanEventBo = new SpanEventBo();
                        if (k == nextEventIndex) {
                            spanEventBo.setNextSpanId(passiveSpanBo.getNextSpanId());
                        } else {
                            spanEventBo.setNextSpanId(RandomUtils.nextInt(10000, 100000));
                        }
                        passiveSpanBo.addPassiveSpanEventBo(spanEventBo);
                    }

                    passiveSpanBo.setStartTime(i * 10000 + j);

                    passiveSpanBoList.add(passiveSpanBo);
                }
            }
        }

        return passiveSpanBoList;
    }

    private List<SpanBo> sortSpanBoListBySequence(List<SpanBo> spanBoList) {
        Map<Long, SpanBo> spanBoMap = new HashMap<>();
        List<SpanBo> spanBoListBySequence = new ArrayList<>();

        for (SpanBo spanBo : spanBoList) {
            spanBoMap.put(spanBo.getSpanId(), spanBo);
        }

        SpanBo head = (SpanBo)CollectionUtils.find(spanBoList, new Predicate() {
            @Override
            public boolean evaluate(Object object) {
                SpanBo spanBo = (SpanBo)object;
                if (spanBo.getParentSpanId() == -1) {
                    return true;
                }
                return false;
            }
        });
        Assert.assertNotNull(head);
        spanBoMap.remove(head.getSpanId());
        spanBoListBySequence.add(head);

        SpanBo prev = head;
        while(!spanBoMap.isEmpty()) {
            long nextSpanId = -1;
            for(SpanEventBo eventBo: prev.getSpanEventBoList()) {
                // we do't care spanId = [10000, 100000]
                if (eventBo.getNextSpanId() < 10000) {
                    nextSpanId = eventBo.getNextSpanId();
                    break;
                }
            }
            // spanId = [100, 1100]
            // passiveSpanId = [5000, 6000]
            Assert.assertTrue((nextSpanId >= 100 && nextSpanId <= 1100) ||
                    (nextSpanId >= 5000 && nextSpanId <= 6000));

            SpanBo cur = spanBoMap.get(nextSpanId);
            Assert.assertNotNull(cur);
            spanBoMap.remove(nextSpanId);
            spanBoListBySequence.add(cur);

            prev = cur;
        }

        return spanBoListBySequence;

    }


//    @Test
//    public void testMergePassiveSpan_missing_first() throws Exception {
//        // 1, missing first
//        SpanBo first = new SpanBo();
//        SpanBo second = new SpanBo();
//        PassiveSpanBo passiveSpanBo = new PassiveSpanBo();
//
//        SpanEventBo event1 = new SpanEventBo();
//        SpanEventBo event2 = new SpanEventBo();
//        event1.setNextSpanId(3);
//        event2.setNextSpanId(1000);
//
//        List<SpanEventBo> spanEventBoList= new ArrayList<>();
//        spanEventBoList.add(event1);
//        spanEventBoList.add(event2);
//
//        first.setSpanId(1);
//        first.addSpanEventBoList(spanEventBoList);
//        second.setSpanId(3);
//        passiveSpanBo.setPassiveSpanId(new PassiveSpanId(1, 3));
//
//        List<SpanBo> spanBoList = new ArrayList<>();
//        // spanBoList.add(first);
//        spanBoList.add(second);
//
//        List<PassiveSpanBo> passiveSpanBoList = new ArrayList<>();
//        passiveSpanBoList.add(passiveSpanBo);
//
//        SpanServiceImpl.mergePassiveSpan(spanBoList, passiveSpanBoList);
//
//        Assert.assertEquals(spanBoList.size(), 1);
//
//    }
//
//    @Test
//    public void testMergePassiveSpan_missing_second() throws Exception {
//        // 1, missing second
//        SpanBo first = new SpanBo();
//        SpanBo second = new SpanBo();
//        PassiveSpanBo passiveSpanBo = new PassiveSpanBo();
//
//        SpanEventBo event1 = new SpanEventBo();
//        SpanEventBo event2 = new SpanEventBo();
//        event1.setNextSpanId(3);
//        event2.setNextSpanId(1000);
//
//        List<SpanEventBo> spanEventBoList= new ArrayList<>();
//        spanEventBoList.add(event1);
//        spanEventBoList.add(event2);
//
//        first.setSpanId(1);
//        first.addSpanEventBoList(spanEventBoList);
//        second.setSpanId(3);
//        passiveSpanBo.setPassiveSpanId(new PassiveSpanId(1, 3));
//
//        List<SpanBo> spanBoList = new ArrayList<>();
//        spanBoList.add(first);
//        //spanBoList.add(second);
//
//        List<PassiveSpanBo> passiveSpanBoList = new ArrayList<>();
//        passiveSpanBoList.add(passiveSpanBo);
//
//        SpanServiceImpl.mergePassiveSpan(spanBoList, passiveSpanBoList);
//
//        Assert.assertEquals(spanBoList.size(), 1);
//
//    }

    private PassiveSpanBo randomPassiveSpan() {
        TPassiveSpan tPassiveSpan = randomTSpan.randomTPassiveSpan();
        return spanFactory.buildPassiveSpanBo(tPassiveSpan);
    }

}
