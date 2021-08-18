/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.context;

import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.util.DataType;

/**
 * @author yjqg6666
 */
public interface AttributeRecorder {

    void recordAttribute(AnnotationKey key, String value);


    void recordAttribute(AnnotationKey key, int value);

    void recordAttribute(AnnotationKey key, Integer value);


    void recordAttribute(AnnotationKey key, long value);

    void recordAttribute(AnnotationKey key, Long value);


    void recordAttribute(AnnotationKey key, boolean value);

    void recordAttribute(AnnotationKey key, double value);

    void recordAttribute(AnnotationKey key, byte[] value);

    void recordAttribute(AnnotationKey key, DataType value);

    void recordAttribute(AnnotationKey key, Object value);

}
