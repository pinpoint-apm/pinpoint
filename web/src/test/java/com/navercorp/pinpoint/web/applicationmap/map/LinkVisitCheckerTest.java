/*
 * Copyright 2023 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.web.applicationmap.map;

import com.google.common.util.concurrent.MoreExecutors;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.vo.Application;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author emeroad
 * @author HyunGil Jeong
 */
public class LinkVisitCheckerTest {

    @Test
    public void testVisitCaller() {
        LinkVisitChecker checker = new LinkVisitChecker();

        Application testApplication = new Application("test", ServiceType.STAND_ALONE);
        Assertions.assertFalse(checker.visitOut(testApplication));
        Assertions.assertTrue(checker.visitOut(testApplication));

        Application newApp = new Application("newApp", ServiceType.STAND_ALONE);
        Assertions.assertFalse(checker.visitOut(newApp));
        Assertions.assertTrue(checker.visitOut(newApp));
    }

    @Test
    public void testVisitCallee() {
        LinkVisitChecker checker = new LinkVisitChecker();

        Application testApplication = new Application("test", ServiceType.STAND_ALONE);
        Assertions.assertFalse(checker.visitIn(testApplication));
        Assertions.assertTrue(checker.visitIn(testApplication));

        Application newApp = new Application("newApp", ServiceType.STAND_ALONE);
        Assertions.assertFalse(checker.visitIn(newApp));
        Assertions.assertTrue(checker.visitIn(newApp));
    }

    @Test
    public void testVisitCallerParallel() throws Exception {
        final int concurrency = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(concurrency);

        LinkVisitChecker checker = new LinkVisitChecker();
        CountDownLatch visitedLatch = new CountDownLatch(concurrency);
        CountDownLatch submitLatch = new CountDownLatch(1);
        AtomicInteger firstVisitCount = new AtomicInteger();
        AtomicInteger alreadyVisitedCount = new AtomicInteger();

        LinkVisitor linkVisitor = new LinkCallerVisitor(checker);
        VisitJobContext jobContext = new VisitJobContext(linkVisitor, visitedLatch, submitLatch, firstVisitCount, alreadyVisitedCount);
        final Application testApplication = new Application("test1", ServiceType.STAND_ALONE);

        for (int i = 0; i < concurrency; i++) {
            executorService.submit(new VisitJob(jobContext, testApplication));
        }
        submitLatch.countDown();
        visitedLatch.await();
        Assertions.assertEquals(1, firstVisitCount.get());
        Assertions.assertEquals(concurrency - 1, alreadyVisitedCount.get());

        MoreExecutors.shutdownAndAwaitTermination(executorService, Duration.ofSeconds(3));
    }

    @Test
    public void testVisitCallerParallelMultipleApplications() throws Exception {
        final int concurrency = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(concurrency);

        LinkVisitChecker checker = new LinkVisitChecker();
        CountDownLatch visitedLatch = new CountDownLatch(concurrency);
        CountDownLatch submitLatch = new CountDownLatch(1);
        AtomicInteger firstVisitCount = new AtomicInteger();
        AtomicInteger alreadyVisitedCount = new AtomicInteger();

        LinkVisitor linkVisitor = new LinkCallerVisitor(checker);
        VisitJobContext jobContext = new VisitJobContext(linkVisitor, visitedLatch, submitLatch, firstVisitCount, alreadyVisitedCount);
        final Application testApplication1 = new Application("test1", ServiceType.STAND_ALONE);
        final Application testApplication2 = new Application("test2", ServiceType.STAND_ALONE);
        final Application testApplication3 = new Application("test3", ServiceType.STAND_ALONE);

        for (int i = 0; i < concurrency; i++) {
            if (i % 3 == 0) {
                executorService.submit(new VisitJob(jobContext, testApplication1));
            } else if (i % 3 == 1) {
                executorService.submit(new VisitJob(jobContext, testApplication2));
            } else {
                executorService.submit(new VisitJob(jobContext, testApplication3));
            }
        }
        submitLatch.countDown();
        visitedLatch.await();
        Assertions.assertEquals(3, firstVisitCount.get());
        Assertions.assertEquals(concurrency - 3, alreadyVisitedCount.get());

        MoreExecutors.shutdownAndAwaitTermination(executorService, Duration.ofSeconds(3));
    }

    private static class VisitJobContext {
        private final LinkVisitor linkVisitor;
        private final CountDownLatch visitLatch;
        private final CountDownLatch submitLatch;
        private final AtomicInteger firstVisitCount;
        private final AtomicInteger alreadyVisistedCount;

        private VisitJobContext(
                LinkVisitor linkVisitor,
                CountDownLatch visitLatch,
                CountDownLatch submitLatch,
                AtomicInteger firstVisitCount,
                AtomicInteger alreadyVisistedCount) {
            this.linkVisitor = linkVisitor;
            this.visitLatch = visitLatch;
            this.submitLatch = submitLatch;
            this.firstVisitCount = firstVisitCount;
            this.alreadyVisistedCount = alreadyVisistedCount;
        }

        private void visit(Application application) {
            boolean alreadyVisited = linkVisitor.visit(application);
            if (alreadyVisited) {
                alreadyVisistedCount.getAndIncrement();
            } else {
                firstVisitCount.getAndIncrement();
            }
        }

        private void waitForInitialization() throws InterruptedException {
            submitLatch.await(1000L, TimeUnit.MILLISECONDS);
        }

        private void markDone() {
            visitLatch.countDown();
        }
    }

    private static class VisitJob implements Runnable {

        private final VisitJobContext jobContext;
        private final Application application;

        private VisitJob(VisitJobContext jobContext, Application application) {
            this.jobContext = jobContext;
            this.application = application;
        }

        @Override
        public void run() {
            try {
                jobContext.waitForInitialization();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            jobContext.visit(application);
            jobContext.markDone();
        }
    }

    private interface LinkVisitor {
        boolean visit(Application application);
    }

    private static class LinkCallerVisitor implements LinkVisitor {

        private final LinkVisitChecker linkVisitChecker;

        private LinkCallerVisitor(LinkVisitChecker linkVisitChecker) {
            this.linkVisitChecker = linkVisitChecker;
        }

        @Override
        public boolean visit(Application application) {
            return linkVisitChecker.visitOut(application);
        }
    }

    private static class LinkCalleeVisitor implements LinkVisitor {

        private final LinkVisitChecker linkVisitChecker;

        private LinkCalleeVisitor(LinkVisitChecker linkVisitChecker) {
            this.linkVisitChecker = linkVisitChecker;
        }

        @Override
        public boolean visit(Application application) {
            return linkVisitChecker.visitIn(application);
        }
    }
}
