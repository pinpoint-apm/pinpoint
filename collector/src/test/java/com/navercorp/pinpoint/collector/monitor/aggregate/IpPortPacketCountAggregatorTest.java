/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.collector.monitor.aggregate;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author HyunGil Jeong
 */
public class IpPortPacketCountAggregatorTest {

    private final Random random = new Random();
    private final IpPortPacketCountAggregator aggregator = new IpPortPacketCountAggregator();

    private final ExecutorService executorService = Executors.newFixedThreadPool(4);

    @Test
    public void multipleGetAndResets() throws InterruptedException {
        final int numIpPorts = 5000;
        final int numPacketsPerIpPort = 100;
        final int totalNumPackets = numIpPorts * numPacketsPerIpPort;
        List<IpPortTestPacket> testPackets = new ArrayList<>(totalNumPackets);
        for (int i = 0; i < numIpPorts; i++) {
            String ip = UUID.randomUUID().toString();
            int port = random.nextInt(55000) + 10000;
            testPackets.addAll(createTestPackets(ip, port, numPacketsPerIpPort));
        }
        final int numReports = totalNumPackets / 100 + 1;
        final int numTasks = totalNumPackets + numReports;
        final CountDownLatch taskCompleteLatch = new CountDownLatch(numTasks);
        final List<Runnable> tasks = new ArrayList<>(numTasks);
        for (IpPortTestPacket testPacket : testPackets) {
            tasks.add(() -> {
                aggregator.increment(testPacket.ip, testPacket.port);
                taskCompleteLatch.countDown();
            });
        }

        final AtomicLong packetCounter = new AtomicLong();
        for (int i = 0; i < numReports; i++) {
            tasks.add(() -> {
                aggregator.getAndReset().values().forEach(packetCounter::addAndGet);
                taskCompleteLatch.countDown();
            });
        }
        Collections.shuffle(tasks);
        for (Runnable task : tasks) {
            executorService.submit(task);
        }
        taskCompleteLatch.await(10, TimeUnit.SECONDS);
        aggregator.getAndReset().values().forEach(packetCounter::addAndGet);

        long actualNumPackets = packetCounter.get();
        Assert.assertEquals(totalNumPackets, actualNumPackets);
    }

    private static List<IpPortTestPacket> createTestPackets(String ip, int port, int numPackets) {
        return IntStream.range(0, numPackets)
                .mapToObj(i -> new IpPortTestPacket(ip, port))
                .collect(Collectors.toList());
    }

    private static class IpPortTestPacket {
        private final String ip;
        private final int port;

        private IpPortTestPacket(String ip, int port) {
            this.ip = ip;
            this.port = port;
        }
    }
}
