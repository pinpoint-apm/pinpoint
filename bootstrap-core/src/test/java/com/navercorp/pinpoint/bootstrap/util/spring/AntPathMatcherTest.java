/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.util.spring;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertThat;

/**
 * copy from spring-framework
 * https://github.com/spring-projects/spring-framework/blob/master/spring-core/src/test/java/org/springframework/util/AntPathMatcherTests.java
 * https://github.com/naver/pinpoint/issues/5890
 * potential bug fix
 * This bug does not affect the pinpoint
 * Pinpoint does not use templateVariable(always null)
 * @author Woonduk Kang(emeroad)
 */
public class AntPathMatcherTest {
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    /**
     * SPR-7787
     */
    @Test
    public void extractUriTemplateVarsRegexQualifiers() {
        Map<String, String> result = pathMatcher.extractUriTemplateVariables(
                "{symbolicName:[\\p{L}\\.]+}-sources-{version:[\\p{N}\\.]+}.jar",
                "com.example-sources-1.0.0.jar");
        // skip
        // no assertj dependency.
    }

}
