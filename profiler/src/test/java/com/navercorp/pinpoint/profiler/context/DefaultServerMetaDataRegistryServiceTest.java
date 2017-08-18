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
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.navercorp.pinpoint.bootstrap.context.ServerMetaData;
import com.navercorp.pinpoint.bootstrap.context.ServiceInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author hyungil.jeong
 */
public class DefaultServerMetaDataRegistryServiceTest {

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
    public void testRaceConditionWhenAddingConnectors() throws InterruptedException {
        // Given
        final CountDownLatch initLatch = new CountDownLatch(THREAD_COUNT);
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch endLatch = new CountDownLatch(THREAD_COUNT);
        final Queue<Exception> exceptions = new ConcurrentLinkedQueue<Exception>();

        final String protocolPrefix = "protocol-";
        final ServerMetaDataRegistryService serverMetaDataRegistryService = new DefaultServerMetaDataRegistryService(VM_ARGS);
        serverMetaDataRegistryService.setServerName(SERVER_INFO);
        // When
        for (int i = 0; i < THREAD_COUNT; ++i) {
            final int port = i;
            final String protocol = protocolPrefix + i;
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    initLatch.countDown();
                    try {
                        startLatch.await();
                        serverMetaDataRegistryService.addConnector(protocol, port);
                    } catch (final Exception e) {
                        exceptions.add(e);
                    } finally {
                        endLatch.countDown();
                    }
                }
            });
        }
        // Then
        initLatch.await();
        startLatch.countDown();
        endLatch.await();
        // Then
        assertTrue("Failed with errors : " + exceptions, exceptions.isEmpty());
        ServerMetaData serverMetaData = serverMetaDataRegistryService.getServerMetaData();
        assertEquals(SERVER_INFO, serverMetaData.getServerInfo());
        assertEquals(VM_ARGS, serverMetaData.getVmArgs());
        Map<Integer, String> connectors = serverMetaData.getConnectors();
        assertEquals(THREAD_COUNT, connectors.size());
        for (Integer port : connectors.keySet()) {
            assertTrue(connectors.containsKey(port));
            final String expectedProtocol = protocolPrefix + port;
            assertEquals(expectedProtocol, connectors.get(port));
        }
    }

    @Test
    public void testRaceConditionWhenAddingServiceInfo() throws InterruptedException {
        // Given
        final CountDownLatch initLatch = new CountDownLatch(THREAD_COUNT);
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch endLatch = new CountDownLatch(THREAD_COUNT);
        final Queue<Exception> exceptions = new ConcurrentLinkedQueue<Exception>();
        
        final String serviceName = "/test";
        final ServerMetaDataRegistryService serverMetaDataRegistryService = new DefaultServerMetaDataRegistryService(VM_ARGS);
        serverMetaDataRegistryService.setServerName(SERVER_INFO);
        // When
        for (int i = 0; i < THREAD_COUNT; ++i) {
            final List<String> serviceLibs = new ArrayList<String>();
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    initLatch.countDown();
                    try {
                        startLatch.await();
                        ServiceInfo serviceInfo = new DefaultServiceInfo(serviceName, serviceLibs);
                        serverMetaDataRegistryService.addServiceInfo(serviceInfo);
                    } catch (final Exception e) {
                        exceptions.add(e);
                    } finally {
                        endLatch.countDown();
                    }
                }
            });
        }
        initLatch.await();
        startLatch.countDown();
        endLatch.await();
        // Then
        assertTrue("Failed with exceptions : " + exceptions, exceptions.isEmpty());
        ServerMetaData serverMetaData = serverMetaDataRegistryService.getServerMetaData();
        assertEquals(SERVER_INFO, serverMetaData.getServerInfo());
        assertEquals(VM_ARGS, serverMetaData.getVmArgs());
        assertEquals(THREAD_COUNT, serverMetaData.getServiceInfos().size());
    }
}
