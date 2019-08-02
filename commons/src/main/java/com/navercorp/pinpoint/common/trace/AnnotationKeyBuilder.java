/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.common.trace;

/**
 * @author HyunGil Jeong
 */
public class AnnotationKeyBuilder {

    private final int code;
    private final String name;
    private boolean viewInRecordSet;
    private boolean errorApiMetadata;

    public AnnotationKeyBuilder(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public AnnotationKeyBuilder viewInRecordSet(boolean viewInRecordSet) {
        this.viewInRecordSet = viewInRecordSet;
        return this;
    }

    public AnnotationKeyBuilder errorApiMetadata(boolean errorApiMetadata) {
        this.errorApiMetadata = errorApiMetadata;
        return this;
    }

    int code() {
        return code;
    }

    String name() {
        return name;
    }

    boolean viewInRecordSet() {
        return viewInRecordSet;
    }

    boolean errorApiMetadata() {
        return errorApiMetadata;
    }

    public AnnotationKey build() {
        return new DefaultAnnotationKey(this);
    }
}
