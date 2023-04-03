/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.web.filter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class FilterChainTest {

    @Test
    public void constructor_toArray() {
        Filter<List<String>> filter = new Filter<List<String>>() {
            @Override
            public boolean include(List<String> transaction) {
                return false;
            }
        };
        FilterChain<List<String>> filterChain = new FilterChain<>(List.of(filter));
        Assertions.assertFalse(filterChain.include(List.of("a")));

    }
}