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
@TestPropertySource(properties = "test.port=111")
@ContextConfiguration(classes = AnnotationVisitorTest.TestConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class AnnotationVisitorTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final AnnotationVisitor<Value> annotationVisitor = new AnnotationVisitor<>(Value.class);

    @Autowired
    private TestConfig config;

    boolean touch;

    @Test
    public void test() {

        final int port = config.port;

        this.annotationVisitor.visit(config, new AnnotationVisitor.FieldVisitor() {
            @Override
            public void visit(Field field, Object value) {
                if (field.getName().equals("port")) {
                    Assert.assertEquals(port , value);
                    touch = true;
                }
            }
        });
        Assert.assertTrue(touch);
    }

    public static class TestConfig {
        @Value("${test.port}")
        private int port;
    }

}