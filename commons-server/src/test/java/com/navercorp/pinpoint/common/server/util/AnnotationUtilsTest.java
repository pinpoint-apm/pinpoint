/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.common.server.util;

import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;

public class AnnotationUtilsTest {

    @Test
    public void findApiAnnotation() {
        AnnotationBo annotationBo = AnnotationBo.of(AnnotationKey.API.getCode(), "a");
        String value = AnnotationUtils.findApiAnnotation(List.of(annotationBo));
        Assertions.assertEquals("a", value);

        List<AnnotationBo> list = new LinkedList<>();
        list.add(annotationBo);
        String value2 = AnnotationUtils.findApiAnnotation(list);
        Assertions.assertEquals("a", value2);
    }

    @Test
    public void findApiAnnotation_invalidType() {
        AnnotationBo annotationBo = AnnotationBo.of(AnnotationKey.API.getCode(), 1);
        String value = AnnotationUtils.findApiAnnotation(List.of(annotationBo));
        Assertions.assertNull(value);
    }

    @Test
    public void findAnnotation() {
        AnnotationBo annotationBo = AnnotationBo.of(AnnotationKey.API.getCode(), 1);
        AnnotationBo value = AnnotationUtils.findAnnotation(List.of(annotationBo), AnnotationKey.API);
        Assertions.assertEquals(annotationBo, value);
    }

    @Test
    public void findAnnotation_filter() {
        AnnotationBo annotationBo = AnnotationBo.of(AnnotationKey.API.getCode(), 1);
        List<AnnotationBo> values = AnnotationUtils.findAnnotations(List.of(annotationBo), e -> e.getKey() == AnnotationKey.API.getCode());
        Assertions.assertEquals(List.of(annotationBo), values);
    }
}