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

import java.util.Collections;
import java.util.List;

/**
 * @author youngjin.kim2
 */
public class TransformCallbackParameters {

    private final List<TransformCallbackParameter> params;

    TransformCallbackParameters(List<TransformCallbackParameter> params) {
        this.params = Collections.unmodifiableList(params);
    }

    public Object[] getParamValues() {
        Object[] values = new Object[params.size()];
        for (int i = 0; i < params.size(); i++) {
            values[i] = params.get(i).getValue();
        }
        return values;
    }

    public Class<?>[] getParamTypes() {
        Class<?>[] types = new Class<?>[params.size()];
        for (int i = 0; i < params.size(); i++) {
            types[i] = params.get(i).getType().getJavaClass();
        }
        return types;
    }

}
