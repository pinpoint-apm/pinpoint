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

package com.navercorp.pinpoint.profiler.sender;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.navercorp.pinpoint.profiler.util.ObjectPool;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializer;

/**
 * @author Taejin Koo
 */
public class StandbySpanStreamDataStorageTest {

    private static SpanStreamSendDataFactory factory;

    @BeforeClass
    public static void setUp() {
        HeaderTBaseSerializerPoolFactory serializerFactory = new HeaderTBaseSerializerPoolFactory(true, 1000, true);
        ObjectPool<HeaderTBaseSerializer> objectPool = new ObjectPool<HeaderTBaseSerializer>(serializerFactory, 16);

        factory = new SpanStreamSendDataFactory(objectPool);
    }

    @Test
    public void storageTest1() throws InterruptedException {
        long blockTime = 1000;
        long sleepTime = 500;

        StandbySpanStreamDataStorage storage = new StandbySpanStreamDataStorage(10, blockTime);

        SpanStreamSendData oldValue = createSpanStreamSendData();
        SpanStreamSendData newValue = createSpanStreamSendData("a".getBytes());
        Thread.sleep(sleepTime);

        oldValue.addBuffer("test".getBytes());
        Thread.sleep(1);

        storage.addStandbySpanStreamData(oldValue);
        storage.addStandbySpanStreamData(newValue);

        Assert.assertTrue(storage.getLeftWaitTime(1000) < blockTime - sleepTime);
    }

    @Test
    public void storageTest2() throws InterruptedException {
        StandbySpanStreamDataStorage storage = new StandbySpanStreamDataStorage();

        SpanStreamSendData oldValue = createSpanStreamSendData();
        SpanStreamSendData newValue = createSpanStreamSendData("a".getBytes());
        oldValue.addBuffer("test".getBytes());

        storage.addStandbySpanStreamData(oldValue);
        storage.addStandbySpanStreamData(newValue);

        // more available capacity buffer
        Assert.assertEquals(newValue, storage.getStandbySpanStreamSendData());
    }

    @Test
    public void storageTest3() throws InterruptedException {
        StandbySpanStreamDataStorage storage = new StandbySpanStreamDataStorage(3, 1000);

        boolean isAdded = storage.addStandbySpanStreamData(createSpanStreamSendData("a".getBytes()));
        Assert.assertTrue(isAdded);

        isAdded = storage.addStandbySpanStreamData(createSpanStreamSendData("b".getBytes()));
        Assert.assertTrue(isAdded);

        isAdded = storage.addStandbySpanStreamData(createSpanStreamSendData("c".getBytes()));
        Assert.assertTrue(isAdded);

        isAdded = storage.addStandbySpanStreamData(createSpanStreamSendData("d".getBytes()));
        Assert.assertFalse(isAdded);
    }

    @Test
    public void storageTest4() throws InterruptedException {
        long blockTime = 1000;
        long sleepTime = 300;

        StandbySpanStreamDataStorage storage = new StandbySpanStreamDataStorage(10, blockTime);

        SpanStreamSendData spanStreamSendData = createSpanStreamSendData("a".getBytes());
        storage.addStandbySpanStreamData(spanStreamSendData);
        Thread.sleep(sleepTime);

        spanStreamSendData = createSpanStreamSendData("a".getBytes());
        storage.addStandbySpanStreamData(spanStreamSendData);
        Thread.sleep(sleepTime);

        spanStreamSendData = createSpanStreamSendData("a".getBytes());
        storage.addStandbySpanStreamData(spanStreamSendData);
        Thread.sleep(sleepTime);

        spanStreamSendData = createSpanStreamSendData("a".getBytes());
        storage.addStandbySpanStreamData(spanStreamSendData);
        Thread.sleep(sleepTime);

        spanStreamSendData = createSpanStreamSendData("a".getBytes());
        storage.addStandbySpanStreamData(spanStreamSendData);
        Thread.sleep(sleepTime);

        Assert.assertEquals(2, storage.getForceFlushSpanStreamDataList().size());
    }

    private SpanStreamSendData createSpanStreamSendData(byte[] initValue) {
        SpanStreamSendData spanStreamSendData = createSpanStreamSendData();
        spanStreamSendData.addBuffer(initValue);
        return spanStreamSendData;
    }

    private SpanStreamSendData createSpanStreamSendData() {
        SpanStreamSendData spanStreamSendData = factory.create();
        return spanStreamSendData;
    }

}
