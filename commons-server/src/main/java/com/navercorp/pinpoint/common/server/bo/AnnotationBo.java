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

    private int key;
    private Object value;

    private boolean isAuthorized = true;

    public AnnotationBo() {
    }

    public AnnotationBo(int key, Object value) {
        this.key = key;
        this.value = value;
    }

    public int getKey() {
        return key;
    }


    public void setKey(int key) {
        this.key = key;
    }


    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public boolean isAuthorized() {
        return isAuthorized;
    }

    public void setAuthorized(boolean isAuthorized) {
        this.isAuthorized = isAuthorized;
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
