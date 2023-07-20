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

package com.navercorp.pinpoint.common.server.bo;

/**
 * @author emeroad
 */
public class AnnotationBo {

    private final int key;
    private final Object value;

    private final boolean isAuthorized;

    public static AnnotationBo of(int key, Object value) {
        return of(key, value, true);
    }

    public static AnnotationBo of(int key, Object value, boolean isAuthorized) {
        return new AnnotationBo(key, value, isAuthorized);
    }

    public static AnnotationBo unauthorized(int key, Object value) {
        return of(key, value, false);
    }

    AnnotationBo(int key, Object value, boolean isAuthorized) {
        this.key = key;
        this.value = value;
        this.isAuthorized = isAuthorized;
    }

    public int getKey() {
        return key;
    }


    public Object getValue() {
        return value;
    }


    public boolean isAuthorized() {
        return isAuthorized;
    }


    @Override
    public String toString() {
        return "AnnotationBo{" +
                "key=" + key +
                ", value=" + value +
                ", isAuthorized=" + isAuthorized +
                '}';
    }
}
