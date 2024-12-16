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

import com.navercorp.pinpoint.bootstrap.plugin.jdbc.JdbcAutoCommitConfig;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.JdbcConfig;
import com.navercorp.pinpoint.common.trace.ServiceType;

import java.util.ArrayList;
import java.util.List;

/**
 * @author youngjin.kim2
 */
public class TransformCallbackParametersBuilder {

    private final List<TransformCallbackParameter> params = new ArrayList<>();

    private TransformCallbackParametersBuilder() {
    }

    public static TransformCallbackParametersBuilder newBuilder() {
        return new TransformCallbackParametersBuilder();
    }

    private TransformCallbackParametersBuilder addParameter(TransformCallbackParameter param) {
        this.params.add(param);
        return this;
    }

    public TransformCallbackParametersBuilder addBoolean(Boolean param) {
        return this.addParameter(TransformCallbackParameter.of(param));
    }

    public TransformCallbackParametersBuilder addServiceType(ServiceType param) {
        return this.addParameter(TransformCallbackParameter.of(param));
    }

    public TransformCallbackParametersBuilder addLong(Long param) {
        return this.addParameter(TransformCallbackParameter.of(param));
    }

    public TransformCallbackParametersBuilder addDouble(Double param) {
        return this.addParameter(TransformCallbackParameter.of(param));
    }

    public TransformCallbackParametersBuilder addString(String param) {
        return this.addParameter(TransformCallbackParameter.of(param));
    }

    public TransformCallbackParametersBuilder addStringArray(String[] param) {
        return this.addParameter(TransformCallbackParameter.of(param));
    }

    public TransformCallbackParametersBuilder addStringArrayArray(String[][] param) {
        return this.addParameter(TransformCallbackParameter.of(param));
    }

    public TransformCallbackParametersBuilder addJdbcConfig(JdbcConfig param) {
        return this.addParameter(TransformCallbackParameter.of(param));
    }

    public TransformCallbackParametersBuilder addJdbcConfig(JdbcAutoCommitConfig param) {
        return this.addParameter(TransformCallbackParameter.of(param));
    }

    /**
     * @deprecated use {@link #build()} instead
     */
    public TransformCallbackParameters toParameters() {
        return build();
    }

    public TransformCallbackParameters build() {
        return new TransformCallbackParameters(this.params);
    }

}
