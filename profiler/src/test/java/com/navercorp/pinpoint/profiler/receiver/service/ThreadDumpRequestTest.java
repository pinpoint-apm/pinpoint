/*
 * Copyright 2017 NAVER Corp.
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
 */

package com.navercorp.pinpoint.profiler.receiver.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ThreadDumpRequestTest {


    @Test
    public void getLimit() {
        final int maxThreadDumpLimit = ThreadDumpRequest.MAX_THREAD_DUMP_LIMIT;
        Assertions.assertEquals(ThreadDumpRequest.getLimit(-1), maxThreadDumpLimit);
        Assertions.assertEquals(ThreadDumpRequest.getLimit(0), maxThreadDumpLimit);
        Assertions.assertEquals(ThreadDumpRequest.getLimit(1000), 1000);
        Assertions.assertEquals(ThreadDumpRequest.getLimit(maxThreadDumpLimit + 100), maxThreadDumpLimit);
    }


    @Test
    public void enableTransactionId() {

        List<Long> localTransactionIdList = Collections.singletonList(1L);
        List<String> threadNameList = Collections.emptyList();
        ThreadDumpRequest request = new ThreadDumpRequest(StackTrace.DUMP, 10, localTransactionIdList, threadNameList);
        Assertions.assertTrue(request.isEnableFilter());
        Assertions.assertTrue(request.isEnableLocalTransactionIdFilter());
        Assertions.assertFalse(request.isEnableThreadNameFilter());

        Assertions.assertTrue(request.findLocalTransactionId(1));
        Assertions.assertFalse(request.findLocalTransactionId(2));

        Assertions.assertFalse(request.findThreadName("a"));
        Assertions.assertFalse(request.findThreadName("b"));
    }

    @Test
    public void enableThreadDump() {

        List<Long> localTransactionIdList = Collections.emptyList();
        List<String> threadNameList = Collections.singletonList("a");

        ThreadDumpRequest request = new ThreadDumpRequest(StackTrace.DUMP, 10, localTransactionIdList, threadNameList);
        Assertions.assertTrue(request.isEnableFilter());
        Assertions.assertFalse(request.isEnableLocalTransactionIdFilter());
        Assertions.assertTrue(request.isEnableThreadNameFilter());

        Assertions.assertFalse(request.findLocalTransactionId(1));
        Assertions.assertFalse(request.findLocalTransactionId(2));

        Assertions.assertTrue(request.findThreadName("a"));
        Assertions.assertFalse(request.findThreadName("b"));
    }


    @Test
    public void filter() {

        List<Long> localTransactionIdList = Collections.singletonList(1L);
        List<String> threadNameList = Collections.singletonList("a");
        ThreadDumpRequest request = new ThreadDumpRequest(StackTrace.DUMP, 10, localTransactionIdList, threadNameList);
        Assertions.assertTrue(request.isEnableFilter());
        Assertions.assertTrue(request.isEnableLocalTransactionIdFilter());
        Assertions.assertTrue(request.isEnableThreadNameFilter());

        Assertions.assertTrue(request.findLocalTransactionId(1));
        Assertions.assertFalse(request.findLocalTransactionId(2));

        Assertions.assertTrue(request.findThreadName("a"));
        Assertions.assertFalse(request.findThreadName("b"));
    }

    @Test
    public void all() {

        List<Long> localTransactionIdList = Collections.emptyList();
        List<String> threadNameList = Collections.emptyList();

        ThreadDumpRequest request = new ThreadDumpRequest(StackTrace.DUMP, 10, localTransactionIdList, threadNameList);
        Assertions.assertFalse(request.isEnableFilter());
        Assertions.assertFalse(request.isEnableLocalTransactionIdFilter());
        Assertions.assertFalse(request.isEnableThreadNameFilter());

        Assertions.assertFalse(request.findLocalTransactionId(1));
        Assertions.assertFalse(request.findLocalTransactionId(2));

        Assertions.assertFalse(request.findThreadName("a"));
        Assertions.assertFalse(request.findThreadName("b"));
    }
}