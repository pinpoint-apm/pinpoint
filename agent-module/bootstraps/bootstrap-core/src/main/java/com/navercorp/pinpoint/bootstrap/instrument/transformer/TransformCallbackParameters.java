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

import java.util.List;
import java.util.Objects;

/**
 * @author youngjin.kim2
 */
public class TransformCallbackParameters {

    private final TransformCallbackParameter[] params;

    TransformCallbackParameters(List<TransformCallbackParameter> params) {
        Objects.requireNonNull(params, "params");
        this.params = params.toArray(new TransformCallbackParameter[0]);
    }

    public Object[] values() {
        final int size = params.length;
        Object[] values = new Object[size];
        for (int i = 0; i < size; i++) {
            values[i] = params[i].getValue();
        }
        return values;
    }

    public Class<?>[] types() {
        final int size = params.length;
        Class<?>[] types = new Class<?>[size];
        for (int i = 0; i < size; i++) {
            types[i] = params[i].getType();
        }
        return types;
    }

}
