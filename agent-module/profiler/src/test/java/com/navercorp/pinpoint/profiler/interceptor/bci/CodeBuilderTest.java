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

package com.navercorp.pinpoint.profiler.interceptor.bci;

import com.navercorp.pinpoint.profiler.instrument.interceptor.CodeBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author emeroad
 */
public class CodeBuilderTest {
    @Test
    public void testCodeBuilder() {
        CodeBuilder builder = new CodeBuilder();
        builder.begin();
        builder.format("1");
        builder.end();
        Assertions.assertEquals("{1}", builder.toString());
    }

    @Test
    public void testFormat() {
        CodeBuilder builder = new CodeBuilder();
        builder.begin();
        builder.format("1");
        builder.format("2");
        builder.end();
        Assertions.assertEquals("{12}", builder.toString());
    }

    @Test
    public void testFormatAppend() {
        CodeBuilder builder = new CodeBuilder();
        builder.begin();
        builder.format("1");
        builder.append("2");
        builder.end();
        Assertions.assertEquals("{12}", builder.toString());
    }


}
