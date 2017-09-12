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

/**
 * @author Woonduk Kang(emeroad)
 */
public class StartTimeComparator {

    static final int CHANGE_TO_NEW_ELEMENT = 1;
    static final int KEEP_OLD_ELEMENT = -1;

    public static int compare(long before, long after) {
        final long diff = before - after;
        if (diff <= 0) {
            // Do not change it for the same value for performance.
            return KEEP_OLD_ELEMENT;
        }
        return CHANGE_TO_NEW_ELEMENT;
    }
}
