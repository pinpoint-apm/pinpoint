/*
 * Copyright 2023 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.mybatis;

import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;

public class DefaultMyBatisConfigurationCustomizer implements MyBatisConfigurationCustomizer {

    @Override
    public void customize(Configuration config) {
        config.setCacheEnabled(true);

        // lazy loading
        config.setLazyLoadingEnabled(true);
        config.setAggressiveLazyLoading(true);

        config.setUseGeneratedKeys(true);

        // don't need "REUSE" because preparedStatements are cached at dbcp
        config.setDefaultExecutorType(ExecutorType.SIMPLE);

        // defaultQueryTimeout. unit is second
        config.setDefaultStatementTimeout(5);

        // underscore mapping of DB table
        config.setMapUnderscoreToCamelCase(true);
    }
}
