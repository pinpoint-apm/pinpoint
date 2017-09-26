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

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.List;


/**
 * @author Woonduk Kang(emeroad)
 */
public class ThreadDumpRequestTest {


    @Test
    public void getLimit() {
        final int maxThreadDumpLimit = ThreadDumpRequest.MAX_THREAD_DUMP_LIMIT;
        Assert.assertEquals(ThreadDumpRequest.getLimit(-1), maxThreadDumpLimit);
        Assert.assertEquals(ThreadDumpRequest.getLimit(0), maxThreadDumpLimit);
        Assert.assertEquals(ThreadDumpRequest.getLimit(1000), 1000);
        Assert.assertEquals(ThreadDumpRequest.getLimit(maxThreadDumpLimit +  100), maxThreadDumpLimit);
    }


    @Test
    public void enableTransactionId() {

        List<Long> localTransactionIdList = Lists.newArrayList(1L);
        List<String> threadNameList = Collections.emptyList();
        ThreadDumpRequest request = new ThreadDumpRequest(StackTrace.DUMP, 10, localTransactionIdList, threadNameList);
        Assert.assertTrue(request.isEnableFilter());
        Assert.assertTrue(request.isEnableLocalTransactionIdFilter());
        Assert.assertFalse(request.isEnableThreadNameFilter());

        Assert.assertTrue(request.findLocalTransactionId(1));
        Assert.assertFalse(request.findLocalTransactionId(2));

        Assert.assertFalse(request.findThreadName("a"));
        Assert.assertFalse(request.findThreadName("b"));
    }

    @Test
    public void enableThreadDump() {

        List<Long> localTransactionIdList = Collections.emptyList();
        List<String> threadNameList = Lists.newArrayList("a");

        ThreadDumpRequest request = new ThreadDumpRequest(StackTrace.DUMP, 10, localTransactionIdList, threadNameList);
        Assert.assertTrue(request.isEnableFilter());
        Assert.assertFalse(request.isEnableLocalTransactionIdFilter());
        Assert.assertTrue(request.isEnableThreadNameFilter());

        Assert.assertFalse(request.findLocalTransactionId(1));
        Assert.assertFalse(request.findLocalTransactionId(2));

        Assert.assertTrue(request.findThreadName("a"));
        Assert.assertFalse(request.findThreadName("b"));
    }


    @Test
    public void filter() {

        List<Long> localTransactionIdList = Lists.newArrayList(1L);
        List<String> threadNameList = Lists.newArrayList("a");
        ThreadDumpRequest request = new ThreadDumpRequest(StackTrace.DUMP, 10, localTransactionIdList, threadNameList);
        Assert.assertTrue(request.isEnableFilter());
        Assert.assertTrue(request.isEnableLocalTransactionIdFilter());
        Assert.assertTrue(request.isEnableThreadNameFilter());

        Assert.assertTrue(request.findLocalTransactionId(1));
        Assert.assertFalse(request.findLocalTransactionId(2));

        Assert.assertTrue(request.findThreadName("a"));
        Assert.assertFalse(request.findThreadName("b"));
    }

    @Test
    public void all() {

        List<Long> localTransactionIdList = Collections.emptyList();
        List<String> threadNameList = Collections.emptyList();

        ThreadDumpRequest request = new ThreadDumpRequest(StackTrace.DUMP, 10, localTransactionIdList, threadNameList);
        Assert.assertFalse(request.isEnableFilter());
        Assert.assertFalse(request.isEnableLocalTransactionIdFilter());
        Assert.assertFalse(request.isEnableThreadNameFilter());

        Assert.assertFalse(request.findLocalTransactionId(1));
        Assert.assertFalse(request.findLocalTransactionId(2));

        Assert.assertFalse(request.findThreadName("a"));
        Assert.assertFalse(request.findThreadName("b"));
    }
}