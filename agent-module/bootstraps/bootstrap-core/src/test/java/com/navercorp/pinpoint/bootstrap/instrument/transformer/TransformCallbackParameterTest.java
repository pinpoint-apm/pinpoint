/*
 * Copyright 2024 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.bootstrap.instrument.transformer;

import com.navercorp.pinpoint.common.trace.ServiceType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author youngjin.kim2
 */
public class TransformCallbackParameterTest {

    @Test
    public void testParametersSerde()  {
        TransformCallbackParameters parameters = TransformCallbackParametersBuilder.newBuilder()
                .addBoolean(Boolean.TRUE)
                .addServiceType(ServiceType.STAND_ALONE)
                .addLong(1L)
                .addDouble(1.0D)
                .addString("test")
                .addStringArray(new String[]{"test1", "test2"})
                .addStringArrayArray(new String[][]{{"test1", "test2"}, {"test3", "test4"}})
                .toParameters();

        Object[] values = parameters.getParamValues();
        assertThat(values).hasSize(7);
        assertThat(values[0]).isInstanceOf(Boolean.class).isEqualTo(Boolean.TRUE);
        assertThat(values[1]).isInstanceOf(ServiceType.class).isEqualTo(ServiceType.STAND_ALONE);
        assertThat(values[2]).isInstanceOf(Long.class).isEqualTo(1L);
        assertThat(values[3]).isInstanceOf(Double.class).isEqualTo(1.0D);
        assertThat(values[4]).isInstanceOf(String.class).isEqualTo("test");
        assertThat(values[5]).isInstanceOf(String[].class).isEqualTo(new String[]{"test1", "test2"});
        assertThat(values[6]).isInstanceOf(String[][].class).isEqualTo(new String[][]{{"test1", "test2"}, {"test3", "test4"}});

        Class<?>[] types = parameters.getParamTypes();
        assertThat(types[0]).isEqualTo(Boolean.class);
        assertThat(types[1]).isEqualTo(ServiceType.class);
        assertThat(types[2]).isEqualTo(Long.class);
        assertThat(types[3]).isEqualTo(Double.class);
        assertThat(types[4]).isEqualTo(String.class);
        assertThat(types[5]).isEqualTo(String[].class);
        assertThat(types[6]).isEqualTo(String[][].class);
    }
}
