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

import java.util.List;

/**
 *
 * @author netspider
 * @author emeroad
 */
public interface Filter<T> {
    boolean ACCEPT = true;
    boolean REJECT = false;

    Filter NONE = new Filter() {
        @Override
        public boolean include(List transaction) {
            return ACCEPT;
        }
    };

    static <T> Filter<T> acceptAllFilter() {
        @SuppressWarnings("unchecked")
        final Filter<T> none = NONE;
        return none;
    }

    boolean include(List<T> transaction);
}
