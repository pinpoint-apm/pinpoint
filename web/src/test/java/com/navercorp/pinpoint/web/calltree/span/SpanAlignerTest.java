/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.web.calltree.span;

import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author jaehong.kim
 */
public class SpanAlignerTest {


    private ServiceTypeRegistryService serviceTypeRegistryService;

    @Before
    public void setUp() throws Exception {
        serviceTypeRegistryService = Mockito.mock(ServiceTypeRegistryService.class);
        Mockito.when(serviceTypeRegistryService.findServiceType(Mockito.anyShort())).thenReturn(ServiceType.UNKNOWN);
    }

    @Test
    public void singleSpan() throws Exception {
        List<String> expectResult = new ArrayList<>();
        expectResult.add("#");
        expectResult.add("##");
        expectResult.add("###");
        expectResult.add("####");

        List<SpanBo> list = new ArrayList<>();
        SpanBo span = new SpanBo();
        span.setParentSpanId(-1);
        span.setSpanId(1);

        span.addSpanEvent(makeSpanEvent(0, 1, -1));
        span.addSpanEvent(makeSpanEvent(1, 2, -1));
        span.addSpanEvent(makeSpanEvent(2, 3, -1));
        list.add(span);

        SpanAligner spanAligner = new SpanAligner(list, 1, serviceTypeRegistryService);
        final CallTree callTree = spanAligner.align();
        CallTreeAssert.assertDepth("single", callTree, expectResult);
    }

    @Test
    public void nextSpan() throws Exception {
        List<String> expectResult = new ArrayList<>();
        expectResult.add("#");
        expectResult.add("##");
        expectResult.add("###");
        expectResult.add("####");
        expectResult.add("#####"); // nextSpan
        expectResult.add("######");
        expectResult.add("#######");
        expectResult.add("########");

        List<SpanBo> list = new ArrayList<>();
        SpanBo span = new SpanBo();
        span.setParentSpanId(-1);
        span.setSpanId(1);

        span.addSpanEvent(makeSpanEvent(0, 1, -1));
        span.addSpanEvent(makeSpanEvent(1, 2, -1));
        span.addSpanEvent(makeSpanEvent(2, 3, 100));
        list.add(span);

        SpanBo nextSpan = new SpanBo();
        nextSpan.setParentSpanId(1);
        nextSpan.setSpanId(100);
        nextSpan.addSpanEvent(makeSpanEvent(0, 1, -1));
        nextSpan.addSpanEvent(makeSpanEvent(1, 2, -1));
        nextSpan.addSpanEvent(makeSpanEvent(2, 3, -1));
        list.add(nextSpan);

        SpanAligner spanAligner = new SpanAligner(list, 1, serviceTypeRegistryService);
        final CallTree callTree = spanAligner.align();
        CallTreeAssert.assertDepth("link", callTree, expectResult);
    }

    @Test
    public void notFoundNextSpan() throws Exception {
        List<String> expectResult = new ArrayList<>();
        expectResult.add("#");
        expectResult.add("##");
        expectResult.add("###");
        expectResult.add("####");

        List<SpanBo> list = new ArrayList<>();
        SpanBo span = new SpanBo();
        span.setParentSpanId(-1);
        span.setSpanId(1);

        span.addSpanEvent(makeSpanEvent(0, 1, -1));
        span.addSpanEvent(makeSpanEvent(1, 2, -1));
        span.addSpanEvent(makeSpanEvent(2, 3, 100));
        list.add(span);

        SpanAligner spanAligner = new SpanAligner(list, 1, serviceTypeRegistryService);
        final CallTree callTree = spanAligner.align();
        CallTreeAssert.assertDepth("notFoundNextSpan", callTree, expectResult);
    }

    @Test
    public void notFoundRoot() throws Exception {
        List<String> expectResult = new ArrayList<>();
        expectResult.add("#"); // unknown
        expectResult.add("##");
        expectResult.add("###");
        expectResult.add("####");

        List<SpanBo> list = new ArrayList<>();
        SpanBo span = new SpanBo();
        span.setParentSpanId(1);
        span.setSpanId(2);

        span.addSpanEvent(makeSpanEvent(0, 1, -1));
        span.addSpanEvent(makeSpanEvent(1, 2, -1));
        list.add(span);

        SpanAligner spanAligner = new SpanAligner(list, 1, serviceTypeRegistryService);
        final CallTree callTree = spanAligner.align();
        CallTreeAssert.assertDepth("notFoundRoot", callTree, expectResult);
    }

    @Test
    public void duplicatedRoot() throws Exception {
        List<String> expectResult = new ArrayList<>();
        expectResult.add("#");
        expectResult.add("##");
        expectResult.add("###");

        List<SpanBo> list = new ArrayList<>();
        SpanBo rootSpan1 = new SpanBo();
        rootSpan1.setParentSpanId(-1);
        rootSpan1.setSpanId(2);

        rootSpan1.addSpanEvent(makeSpanEvent(0, 1, -1));
        rootSpan1.addSpanEvent(makeSpanEvent(1, 2, -1));
        list.add(rootSpan1);

        SpanBo rootSpan2 = new SpanBo();
        rootSpan2.setParentSpanId(-1);
        rootSpan2.setSpanId(3);

        rootSpan2.addSpanEvent(makeSpanEvent(0, 1, -1));
        rootSpan2.addSpanEvent(makeSpanEvent(1, 1, -1));
        list.add(rootSpan2);

        SpanAligner spanAligner = new SpanAligner(list, 1, serviceTypeRegistryService);
        final CallTree callTree = spanAligner.align();
        CallTreeAssert.assertDepth("duplicatedRoot", callTree, expectResult);
    }

    @Test
    public void fill() throws Exception {
        List<String> expectResult = new ArrayList<>();
        expectResult.add("#"); // unknown - not found root
        expectResult.add("##");
        expectResult.add("###");
        expectResult.add("####");
        expectResult.add("#####"); // unknown - fill link
        expectResult.add("######");
        expectResult.add("#######");
        expectResult.add("########");

        List<SpanBo> list = new ArrayList<>();
        SpanBo span = new SpanBo();
        span.setParentSpanId(0);
        span.setSpanId(1);
        span.setElapsed(2);

        span.addSpanEvent(makeSpanEvent(0, 1, -1));
        span.addSpanEvent(makeSpanEvent(1, 2, 100, 2));
        list.add(span);

        // missing middle span
        // parentSpanId = 1, spanId = 100

        SpanBo nextSpan = new SpanBo();
        nextSpan.setParentSpanId(100);
        nextSpan.setSpanId(200);
        nextSpan.setElapsed(1);

        nextSpan.addSpanEvent(makeSpanEvent(0, 1, -1));
        nextSpan.addSpanEvent(makeSpanEvent(1, 2, -1));

        list.add(nextSpan);

        SpanAligner spanAligner = new SpanAligner(list, 1, serviceTypeRegistryService);
        final CallTree callTree = spanAligner.align();
        CallTreeAssert.assertDepth("fill", callTree, expectResult);
    }

    @Test
    public void notFoundFill() throws Exception {
        List<String> expectResult = new ArrayList<>();
        expectResult.add("#"); // unknown - not found root
        expectResult.add("##");
        expectResult.add("###");
        expectResult.add("####");

        List<SpanBo> list = new ArrayList<>();
        SpanBo span = new SpanBo();
        span.setParentSpanId(0);
        span.setSpanId(1);
        span.setStartTime(1);

        span.addSpanEvent(makeSpanEvent(0, 1, -1));
        span.addSpanEvent(makeSpanEvent(1, 2, 100, 2));
        list.add(span);

        // missing middle span
        // parentSpanId = 1, spanId = 100

        SpanBo nextSpan = new SpanBo();
        nextSpan.setParentSpanId(100);
        nextSpan.setSpanId(200);
        nextSpan.setStartTime(0);  // too fast

        nextSpan.addSpanEvent(makeSpanEvent(0, 1, -1));
        nextSpan.addSpanEvent(makeSpanEvent(1, 2, -1));

        list.add(nextSpan);

        SpanAligner spanAligner = new SpanAligner(list, 1, serviceTypeRegistryService);
        final CallTree callTree = spanAligner.align();
        CallTreeAssert.assertDepth("fill", callTree, expectResult);
    }

    @Test
    public void duplicatedSpan() throws Exception {
        List<String> expectResult = new ArrayList<>();
        expectResult.add("#"); // unknown - not found root
        expectResult.add("##");
        expectResult.add("###");
        expectResult.add("####");
        expectResult.add("#####");

        List<SpanBo> list = new ArrayList<>();
        SpanBo span = new SpanBo();
        span.setParentSpanId(0);
        span.setSpanId(1);

        span.addSpanEvent(makeSpanEvent(0, 1, -1));
        span.addSpanEvent(makeSpanEvent(1, 2, -1));
        span.addSpanEvent(makeSpanEvent(2, 3, 100, 3));
        list.add(span);

        SpanBo duplicatedSpan = new SpanBo();
        duplicatedSpan.setParentSpanId(0);
        duplicatedSpan.setSpanId(1);
        duplicatedSpan.setElapsed(2);

        duplicatedSpan.addSpanEvent(makeSpanEvent(0, 1, -1));
        duplicatedSpan.addSpanEvent(makeSpanEvent(1, 2, -1));
        duplicatedSpan.addSpanEvent(makeSpanEvent(2, 3, 200));
        list.add(duplicatedSpan);

        SpanBo nextSpan = new SpanBo();
        nextSpan.setParentSpanId(1);
        nextSpan.setSpanId(200);
        nextSpan.setElapsed(1);

        nextSpan.addSpanEvent(makeSpanEvent(0, 1, -1));
        nextSpan.addSpanEvent(makeSpanEvent(1, 1, -1));
        nextSpan.addSpanEvent(makeSpanEvent(2, 2, -1));
        list.add(nextSpan);

        SpanAligner spanAligner = new SpanAligner(list, 1, serviceTypeRegistryService);
        final CallTree callTree = spanAligner.align();
        CallTreeAssert.assertDepth("duplicatedSpan", callTree, expectResult);
    }

    @Test
    public void multipleSpanNotFoundRoot() throws Exception {
        List<String> expectResult = new ArrayList<>();
        expectResult.add("#"); // unknown - not found root
        expectResult.add("##");
        expectResult.add("###");
        expectResult.add("####");
        expectResult.add("#####");
        expectResult.add("##");
        expectResult.add("###");
        expectResult.add("####");
        expectResult.add("#####");

        List<SpanBo> list = new ArrayList<>();
        SpanBo span = new SpanBo();
        span.setParentSpanId(0);
        span.setSpanId(1);

        span.addSpanEvent(makeSpanEvent(0, 1, -1));
        span.addSpanEvent(makeSpanEvent(1, 2, -1));
        span.addSpanEvent(makeSpanEvent(2, 3, 100, 3));
        list.add(span);

        SpanBo secondSpan = new SpanBo();
        secondSpan.setParentSpanId(0);
        secondSpan.setSpanId(100);

        secondSpan.addSpanEvent(makeSpanEvent(0, 1, -1));
        secondSpan.addSpanEvent(makeSpanEvent(1, 2, -1));
        secondSpan.addSpanEvent(makeSpanEvent(2, 3, -1));
        list.add(secondSpan);

        SpanAligner spanAligner = new SpanAligner(list, 1, serviceTypeRegistryService);
        final CallTree callTree = spanAligner.align();
        CallTreeAssert.assertDepth("multipleSpanNotFoundRoot", callTree, expectResult);
    }

    @Test
    public void corrupted() throws Exception {
        List<String> expectResult = new ArrayList<>();
        expectResult.add("#");
        expectResult.add("##");
        expectResult.add("###"); // corrupted

        List<SpanBo> list = new ArrayList<>();
        SpanBo span = new SpanBo();
        span.setParentSpanId(-1);
        span.setSpanId(1);

        span.addSpanEvent(makeSpanEvent(0, 1, -1));
        // missing span event
        span.addSpanEvent(makeSpanEvent(2, 3, 100, 3));
        list.add(span);

        SpanAligner spanAligner = new SpanAligner(list, 1, serviceTypeRegistryService);
        final CallTree callTree = spanAligner.align();
        CallTreeAssert.assertDepth("corrupted", callTree, expectResult);
    }

    @Test
    public void corruptedNextSpan() throws Exception {
        List<String> expectResult = new ArrayList<>();
        expectResult.add("#");
        expectResult.add("##");
        expectResult.add("###"); // corrupted
        expectResult.add("####"); // nextSpan
        expectResult.add("#####");

        List<SpanBo> list = new ArrayList<>();
        SpanBo span = new SpanBo();
        span.setParentSpanId(-1);
        span.setSpanId(1);

        span.addSpanEvent(makeSpanEvent(0, 1, -1));
        // missing span event
        span.addSpanEvent(makeSpanEvent(2, 3, 100, 3));
        list.add(span);

        SpanBo nextSpan = new SpanBo();
        nextSpan.setParentSpanId(1);
        nextSpan.setSpanId(100);

        nextSpan.addSpanEvent(makeSpanEvent(0, 1, -1));
        list.add(nextSpan);

        SpanAligner spanAligner = new SpanAligner(list, 1, serviceTypeRegistryService);
        final CallTree callTree = spanAligner.align();
        CallTreeAssert.assertDepth("corruptedNextSpan", callTree, expectResult);
    }

    @Test
    public void emptySpanList() throws Exception {
        List<String> expectResult = new ArrayList<>();
        expectResult.add("#"); // unknown

        List<SpanBo> list = new ArrayList<>();
        SpanAligner spanAligner = new SpanAligner(list, 1, serviceTypeRegistryService);
        final CallTree callTree = spanAligner.align();
        CallTreeAssert.assertDepth("emptySpanList", callTree, expectResult);
    }

    @Test
    public void loopSpanList() throws Exception {
        List<String> expectResult = new ArrayList<>();
        expectResult.add("#"); // unknown

        List<SpanBo> list = new ArrayList<>();
        SpanBo span = new SpanBo();
        span.setParentSpanId(100);
        span.setSpanId(1);

        span.addSpanEvent(makeSpanEvent(0, 1, -1));
        span.addSpanEvent(makeSpanEvent(1, 2, 100));
        list.add(span);

        SpanBo nextSpan = new SpanBo();
        nextSpan.setParentSpanId(1);
        nextSpan.setSpanId(100);

        nextSpan.addSpanEvent(makeSpanEvent(0, 1, -1));
        nextSpan.addSpanEvent(makeSpanEvent(0, 2, 1));
        list.add(nextSpan);

        SpanAligner spanAligner = new SpanAligner(list, 1, serviceTypeRegistryService);
        final CallTree callTree = spanAligner.align();
        CallTreeAssert.assertDepth("loopSpanList", callTree, expectResult);
    }

    private SpanEventBo makeSpanEvent(int sequence, int depth, int nextSpanId) {
        return makeSpanEvent(sequence, depth, nextSpanId, 0);
    }

    private SpanEventBo makeSpanEvent(int sequence, int depth, int nextSpanId, int endElapsed) {
        SpanEventBo event = new SpanEventBo();
        event.setSequence((short) sequence);
        event.setDepth(depth);
        event.setNextSpanId(nextSpanId);
        event.setEndElapsed(endElapsed);
        return event;
    }
}