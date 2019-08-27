/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.hbase.schema.reader.xml.mapper;

import com.navercorp.pinpoint.hbase.schema.definition.xml.ChangeSet;
import com.navercorp.pinpoint.hbase.schema.definition.xml.Durability;
import com.navercorp.pinpoint.hbase.schema.reader.core.TableConfiguration;

/**
 * @author HyunGil Jeong
 */
public class TableConfigurationMapper {

    public TableConfiguration mapConfiguration(ChangeSet.CreateTable.Configuration configuration) {
        if (configuration == null) {
            return TableConfiguration.EMPTY_CONFIGURATION;
        }
        return new TableConfiguration.Builder()
                .maxFileSize(configuration.getMaxFilesize())
                .readOnly(configuration.isReadonly())
                .compactionEnabled(configuration.isCompactionEnabled())
                .memstoreFlushSize(configuration.getMemstoreFlushsize())
                .durability(mapDurability(configuration.getDurability()))
                .build();
    }

    private TableConfiguration.Durability mapDurability(Durability durability) {
        if (durability == null) {
            return null;
        }
        return TableConfiguration.Durability.valueOf(durability.value());
    }
}
