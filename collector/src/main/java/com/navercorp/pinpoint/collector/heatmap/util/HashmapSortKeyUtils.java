/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.collector.heatmap.util;

/**
 * @author minwoo-jung
 */
public class HashmapSortKeyUtils {

    private static String generateKeySuccessKey(String applicationName) {
        return applicationName + "#suc";
    }

    private static String generateFailKey(String applicationName) {
        return applicationName + "#fal";
    }

    public static String generateKey(String applicationName, boolean isSuccess) {
        if (isSuccess) {
            return generateKeySuccessKey(applicationName);
        } else {
            return generateFailKey(applicationName);
        }
    }
}
