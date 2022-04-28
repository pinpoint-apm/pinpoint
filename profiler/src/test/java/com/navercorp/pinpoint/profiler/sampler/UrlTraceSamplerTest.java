/*
 * Copyright 2022 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.sampler;

import com.navercorp.pinpoint.bootstrap.sampler.Sampler;
import com.navercorp.pinpoint.bootstrap.sampler.TraceSampler;
import com.navercorp.pinpoint.profiler.context.id.AtomicIdGenerator;
import com.navercorp.pinpoint.profiler.context.id.IdGenerator;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class UrlTraceSamplerTest {
    static final String PATTERN1 = "/foo/*";
    static final String PATTERN2 = "/**/bar???";
    static final String PATTERN3 = "/baz/**";

    final TraceSampler defaultTraceSampler = newTraceSampler(FalseSampler.INSTANCE);
    final TraceSampler fooTraceSampler = newTraceSampler(TrueSampler.INSTANCE);
    final TraceSampler barTraceSampler = newTraceSampler(TrueSampler.INSTANCE);
    final TraceSampler bazTraceSampler = newTraceSampler(TrueSampler.INSTANCE);

    private TraceSampler newTraceSampler(Sampler sampler) {
        IdGenerator atomicIdGenerator = new AtomicIdGenerator();
        return new BasicTraceSampler(atomicIdGenerator, sampler);
    }

    @Test
    public void isNewSampled() {
        final UrlTraceSampler urlTraceSampler = getUrlTraceSampler();
        assertTrue(urlTraceSampler.isNewSampled("/foo/bar").isSampled());
        assertTrue(urlTraceSampler.isNewSampled("/foo/foo").isSampled());
        assertTrue(urlTraceSampler.isNewSampled("/bar/bar000").isSampled());
        assertTrue(urlTraceSampler.isNewSampled("/baz").isSampled());
        assertTrue(urlTraceSampler.isNewSampled("/baz/foo").isSampled());
        assertTrue(urlTraceSampler.isNewSampled("/baz/foo/bar").isSampled());

        // not found
        assertFalse(urlTraceSampler.isNewSampled("/AAA").isSampled());
        assertFalse(urlTraceSampler.isNewSampled("/foo").isSampled());
        assertFalse(urlTraceSampler.isNewSampled("/bar").isSampled());
        // default
        assertFalse(urlTraceSampler.isNewSampled().isSampled());
    }

    @Test
    public void getSampler() {
        // pattern1
        final UrlTraceSampler urlTraceSampler = getUrlTraceSampler();
        TraceSampler traceSampler = urlTraceSampler.getSampler("/foo/foo");
        assertEquals(fooTraceSampler, traceSampler);
        traceSampler = urlTraceSampler.getSampler("/foo/bar");
        assertEquals(fooTraceSampler, traceSampler);
        // pattern2
        traceSampler = urlTraceSampler.getSampler("/AAA/bar001");
        assertEquals(barTraceSampler, traceSampler);
        traceSampler = urlTraceSampler.getSampler("/BBB/bar002");
        assertEquals(barTraceSampler, traceSampler);
        // other
        traceSampler = urlTraceSampler.getSampler("/foo-bar");
        assertEquals(defaultTraceSampler, traceSampler);
        traceSampler = urlTraceSampler.getSampler("/CCC/bar/DDD");
        assertEquals(defaultTraceSampler, traceSampler);

        // default
        traceSampler = urlTraceSampler.getSampler("/AAA");
        assertEquals(defaultTraceSampler, traceSampler);
    }

    private UrlTraceSampler getUrlTraceSampler() {
        Map<String, TraceSampler> urlMap = new LinkedHashMap<>();
        urlMap.put(PATTERN1, fooTraceSampler);
        urlMap.put(PATTERN2, barTraceSampler);
        urlMap.put(PATTERN3, bazTraceSampler);

        return new UrlTraceSampler(urlMap, defaultTraceSampler);
    }
}