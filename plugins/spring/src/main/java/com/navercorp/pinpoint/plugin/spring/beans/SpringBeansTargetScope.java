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

/**
 * @author jaehong.kim
 */
public enum SpringBeansTargetScope {
    COMPONENT_SCAN, POST_PROCESSOR;

    public static SpringBeansTargetScope get(String name) {
        if (name != null && name.toLowerCase().equals("post-processor")) {
            return POST_PROCESSOR;
        }

        return COMPONENT_SCAN;
    }
}