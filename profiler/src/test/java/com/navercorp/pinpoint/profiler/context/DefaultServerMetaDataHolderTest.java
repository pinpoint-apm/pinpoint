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

package com.navercorp.pinpoint.profiler.context;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.navercorp.pinpoint.bootstrap.context.ServerMetaData;
import com.navercorp.pinpoint.bootstrap.context.ServerMetaDataHolder;

/**
 * @author hyungil.jeong
 */
public class DefaultServerMetaDataHolderTest {

    private static final int THREAD_COUNT = 500;

    private static final String SERVER_INFO = "testContainerInfo";
    private static final List<String> VM_ARGS = Arrays.asList("testVmArgs");

    private ExecutorService executorService;
    
    @Before
    public void setUp() {
        this.executorService = Executors.newFixedThreadPool(THREAD_COUNT);
    }

    @After
    public void cleanUp() {
        this.executorService.shutdown();
    }

    @Test
    public void testRaceConditionWhenAddingServiceInfo() throws InterruptedException {
        // Given
        final CountDownLatch initLatch = new CountDownLatch(THREAD_COUNT);
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch endLatch = new CountDownLatch(THREAD_COUNT);
        final Queue<Throwable> exceptions = new ConcurrentLinkedQueue<Throwable>();
        
        final String serviceName = "/test";
        final TestableServerMetaDataListener listener = new TestableServerMetaDataListener();
        final ServerMetaDataHolder metaDataContext = new DefaultServerMetaDataHolder(VM_ARGS);
        metaDataContext.addListener(listener);
        metaDataContext.setServerName(SERVER_INFO);
        // When
        for (int i = 0; i < THREAD_COUNT; ++i) {
            final List<String> serviceLibs = new ArrayList<String>();
            executorService.submit(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    initLatch.countDown();
                    try {
                        startLatch.await();
                        metaDataContext.addServiceInfo(serviceName, serviceLibs);
                        metaDataContext.notifyListeners();
                    } catch (final Throwable t) {
                        exceptions.add(t);
                    } finally {
                        endLatch.countDown();
                    }
                    return null;
                }
            });
        }
        initLatch.await();
        startLatch.countDown();
        endLatch.await();
        // Then
        assertTrue("Failed with exceptions : " + exceptions, exceptions.isEmpty());
        ServerMetaData metaData = listener.getServerMetaData();
        assertEquals(metaData.getServerInfo(), SERVER_INFO);
        assertEquals(metaData.getVmArgs(), VM_ARGS);
        assertEquals(metaData.getServiceInfos().size(), THREAD_COUNT);
    }

    @Test
    public void testRaceConditionWhenAddingAndInteratingServiceInfo() throws InterruptedException {
        // Given
        final CountDownLatch initLatch = new CountDownLatch(THREAD_COUNT);
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch endLatch = new CountDownLatch(THREAD_COUNT);
        final Queue<Throwable> exceptions = new ConcurrentLinkedQueue<Throwable>();
        
        final TestableServerMetaDataListener listener = new TestableServerMetaDataListener();
        final ServerMetaDataHolder metaDataContext = new DefaultServerMetaDataHolder(VM_ARGS);
        metaDataContext.addListener(listener);
        metaDataContext.setServerName(SERVER_INFO);
        // When
        final List<ServerMetaData> serverMetaDatas = new Vector<ServerMetaData>(THREAD_COUNT/2);
        for (int i = 0; i < THREAD_COUNT; ++i) {
            if (i % 2 == 0) {
                final String serviceName = "/name" + i;
                final List<String> serviceLibs = new ArrayList<String>();
                executorService.submit(new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        initLatch.countDown();
                        try {
                            startLatch.await();
                            metaDataContext.addServiceInfo(serviceName, serviceLibs);
                            metaDataContext.notifyListeners();
                        } catch (Throwable t) {
                            exceptions.add(t);
                        } finally {
                            endLatch.countDown();
                        }
                        return null;
                    }
                });
            } else {
                executorService.submit(new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        initLatch.countDown();
                        try {
                            startLatch.await();
                            ServerMetaData serverMetaData = listener.getServerMetaData();
                            serverMetaDatas.add(serverMetaData);
                        } catch (Throwable t) {
                            exceptions.add(t);
                        } finally {
                            endLatch.countDown();
                        }
                        return null;
                    }
                });
            }
        }
        initLatch.await();
        startLatch.countDown();
        endLatch.await();
        // Then
        assertTrue("Failed with exceptions : " + exceptions, exceptions.isEmpty());
        ServerMetaData metaData = listener.getServerMetaData();
        assertEquals(metaData.getServerInfo(), SERVER_INFO);
        assertEquals(metaData.getVmArgs(), VM_ARGS);
        assertEquals(metaData.getServiceInfos().size(), THREAD_COUNT/2);
        assertEquals(serverMetaDatas.size(), THREAD_COUNT/2);
    }
    
}
