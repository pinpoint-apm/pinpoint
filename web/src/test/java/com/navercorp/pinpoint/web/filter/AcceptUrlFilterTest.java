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

package com.navercorp.pinpoint.web.filter;

import com.navercorp.pinpoint.common.server.bo.SpanBo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author emeroad
 */
public class AcceptUrlFilterTest {
    private static final Charset UTF8 = StandardCharsets.UTF_8;

    @Test
    public void acceptTest_1() {

        AcceptUrlFilter filter = new AcceptUrlFilter("/**/*");
        SpanBo spanBo = new SpanBo();
        spanBo.setRpc("/test");
        Assertions.assertTrue(filter.accept(List.of(spanBo)));

    }

    @Test
    public void acceptTest_2() {

        AcceptUrlFilter filter = new AcceptUrlFilter("/abc/*");
        SpanBo spanBo = new SpanBo();
        spanBo.setRpc("/test");
        Assertions.assertFalse(filter.accept(List.of(spanBo)));

    }

}