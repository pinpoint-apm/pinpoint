/*
 * Copyright 2026 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.batch.config;

import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Objects;

@Configuration
public class MetaDataBatchConfiguration extends DefaultBatchConfiguration {

    private final DataSource metaDataDataSource;
    private final PlatformTransactionManager metaDataTransactionManager;

    public MetaDataBatchConfiguration(@Qualifier("metaDataDataSource") DataSource metaDataDataSource,
                                      @Qualifier("metaDataTransactionManager") PlatformTransactionManager metaDataTransactionManager) {
        this.metaDataDataSource = Objects.requireNonNull(metaDataDataSource, "metaDataDataSource");
        this.metaDataTransactionManager = Objects.requireNonNull(metaDataTransactionManager, "metaDataTransactionManager");
    }

    @Override
    protected DataSource getDataSource() {
        return metaDataDataSource;
    }

    @Override
    protected PlatformTransactionManager getTransactionManager() {
        return metaDataTransactionManager;
    }
}
