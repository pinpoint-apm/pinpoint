/*
 * Copyright 2016 NAVER Corp.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.plugin.spring.beans;

import com.navercorp.pinpoint.bootstrap.util.PathMatcher;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * @author jaehong.kim
 */
public class SpringBeansTargetTest {

    @Test
    public void split() {
        SpringBeansTarget target = new SpringBeansTarget();
        assertThat(target.split("1, 2, 3, 4, 5")).hasSize(5);
        assertThat(target.split("1,2,3,4,5")).hasSize(5);
        assertThat(target.split("1, , 2, , , 3, ")).hasSize(3);
        assertThat(target.split(", , , ,   ,    , ")).hasSize(0);
    }

    @Test
    public void compilePattern() {
        SpringBeansTarget target = new SpringBeansTarget();
        List<PathMatcher> list = target.compilePattern(Arrays.asList("1", "regex:2", "antstyle:3"), ".");
        assertThat(list).hasSize(3);
        list = target.compilePattern(Arrays.asList("1", "regex: 2", "antstyle:  3"), ".");
        assertThat(list).hasSize(3);
        list = target.compilePattern(Arrays.asList("1", "regex:", "antstyle:"), ".");
        assertThat(list).hasSize(1);
        list = target.compilePattern(Arrays.asList("1", "regex: 1", "antstyle: 2"), ".");
        assertThat(list).hasSize(3);
    }
}