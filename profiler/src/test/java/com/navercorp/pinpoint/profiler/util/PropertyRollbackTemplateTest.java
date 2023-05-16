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

package com.navercorp.pinpoint.profiler.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Properties;

/**
 * @author Woonduk Kang(emeroad)
 */
public class PropertyRollbackTemplateTest {

    @Test
    public void execute() {
        Properties properties = new Properties();

        PropertyRollbackTemplate template = new PropertyRollbackTemplate(properties);
        template.addKey("a", "1");
        template.execute(new Runnable() {
            @Override
            public void run() {

            }
        });

        Assertions.assertTrue(properties.isEmpty());
    }

    @Test
    public void execute1() {
        Properties properties = new Properties();
        properties.put("a", "A");

        PropertyRollbackTemplate template = new PropertyRollbackTemplate(properties);
        template.addKey("a", "1");
        template.execute(new Runnable() {
            @Override
            public void run() {

            }
        });

        Assertions.assertEquals(properties.get("a"), "A");
    }

    @Test
    public void execute2() {
        Properties properties = new Properties();

        PropertyRollbackTemplate template = new PropertyRollbackTemplate(properties);
        template.addKey("a", "1");
        template.execute(new Runnable() {
            @Override
            public void run() {

            }
        });

        Assertions.assertNull(properties.get("a"));
    }
}