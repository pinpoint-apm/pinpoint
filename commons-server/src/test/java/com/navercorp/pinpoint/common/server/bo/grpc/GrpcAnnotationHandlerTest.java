/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.common.server.bo.grpc;

import com.navercorp.pinpoint.common.server.bo.AnnotationFactory;
import com.navercorp.pinpoint.grpc.trace.PAnnotation;
import org.junit.Assert;
import org.junit.Test;


/**
 * @author Woonduk Kang(emeroad)
 */
public class GrpcAnnotationHandlerTest {

    @Test
    public void getValue() {
        AnnotationFactory.AnnotationTypeHandler<PAnnotation> handler = new GrpcAnnotationHandler();
        PAnnotation.Builder builder = PAnnotation.newBuilder();
        builder.getValueBuilder().setStringValue("testStringField");
        PAnnotation pAnnotation = builder.build();

        Object value = handler.getValue(pAnnotation);

        Assert.assertEquals("testStringField", value);

    }
}