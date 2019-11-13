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

package com.navercorp.pinpoint.collector.config;

import com.navercorp.pinpoint.common.server.config.AnnotationVisitor;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.lang.reflect.Field;


/**
 * @author Woonduk Kang(emeroad)
 */
@TestPropertySource(locations = "classpath:test-pinpoint-collector.properties")
@ContextConfiguration(classes = GrpcSpanReceiverConfiguration.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class AnnotationVisitorTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final AnnotationVisitor annotationVisitor = new AnnotationVisitor(Value.class);

    @Autowired
    GrpcSpanReceiverConfiguration configuration;

    boolean touch;
    @Test
    public void test() {
//        GrpcSpanReceiverConfiguration configuration = new GrpcSpanReceiverConfiguration();
        final int grpcBindPort = configuration.getGrpcBindPort();

        this.annotationVisitor.visit(configuration, new AnnotationVisitor.FieldVisitor() {
            @Override
            public void visit(Field field, Object value) {
                if (field.getName().equals("grpcBindPort")) {
                    Assert.assertEquals(grpcBindPort , value);
                    touch = true;
                }
            }
        });
        Assert.assertTrue(touch);
    }

}