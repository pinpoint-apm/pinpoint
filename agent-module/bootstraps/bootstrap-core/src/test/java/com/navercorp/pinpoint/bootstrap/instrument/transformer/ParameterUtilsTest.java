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

package com.navercorp.pinpoint.bootstrap.instrument.transformer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Date;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ParameterUtilsTest {

    @Test
    public void toClass() {
    }

    @Test
    public void checkSupportType() {
        ParameterUtils.checkParameterType(new Class[]{String.class});

        ParameterUtils.checkParameterType(new Class[]{int.class});
        ParameterUtils.checkParameterType(new Class[]{Integer.class});
    }

    @Test
    public void checkSupportType_fail() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            ParameterUtils.checkParameterType(new Class[]{Date.class});
        });
    }


    @Test
    public void getComponentType() {
        Assertions.assertEquals(int.class, ParameterUtils.getRawComponentType(int[][].class));
    }
}