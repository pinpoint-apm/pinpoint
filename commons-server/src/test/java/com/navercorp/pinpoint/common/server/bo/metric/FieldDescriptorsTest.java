/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.common.server.bo.metric;

import com.navercorp.pinpoint.bootstrap.plugin.monitor.metric.CustomMetric;

import org.junit.Test;

/**
 * @author Taejin Koo
 */
public class FieldDescriptorsTest {

    @Test
    public void createTest() {
        FieldDescriptors.Builder builder = new FieldDescriptors.Builder();

        builder.add(new FieldDescriptor(0, "name", CustomMetric.class));
        builder.add(new FieldDescriptor(3, "name3", CustomMetric.class));
        builder.add(new FieldDescriptor(1, "name2", CustomMetric.class));
        builder.add(new FieldDescriptor(2, "name4", CustomMetric.class));

        builder.build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void createFailTest1() {
        FieldDescriptors.Builder builder = new FieldDescriptors.Builder();

        builder.add(new FieldDescriptor(0, "name", CustomMetric.class));
        builder.add(new FieldDescriptor(5, "name3", CustomMetric.class));
        builder.add(new FieldDescriptor(1, "name2", CustomMetric.class));
        builder.add(new FieldDescriptor(2, "name4", CustomMetric.class));

        builder.build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void createFailTest2() {
        FieldDescriptors.Builder builder = new FieldDescriptors.Builder();

        builder.add(new FieldDescriptor(0, "name", CustomMetric.class));
        builder.add(new FieldDescriptor(3, "name3", CustomMetric.class));
        builder.add(new FieldDescriptor(1, "name", CustomMetric.class));
        builder.add(new FieldDescriptor(2, "name4", CustomMetric.class));

        builder.build();
    }

}
