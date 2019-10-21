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

package com.navercorp.pinpoint.hbase.manager.task;

import com.navercorp.pinpoint.hbase.schema.reader.HbaseSchemaReader;
import com.navercorp.pinpoint.hbase.schema.reader.core.ChangeSet;
import com.navercorp.pinpoint.hbase.schema.service.HbaseSchemaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
public class UpdateTask implements HbaseSchemaManagerTask {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final HbaseSchemaService hbaseSchemaService;
    private final HbaseSchemaReader hbaseSchemaReader;
    private final String namespace;
    private final String compression;

    public UpdateTask(HbaseSchemaService hbaseSchemaService, HbaseSchemaReader hbaseSchemaReader, String namespace, String compression) {
        this.hbaseSchemaService = Objects.requireNonNull(hbaseSchemaService, "hbaseSchemaService");
        this.hbaseSchemaReader = Objects.requireNonNull(hbaseSchemaReader, "hbaseSchemaReader");
        this.namespace = namespace;
        this.compression = compression;
    }

    @Override
    public void run(List<String> arguments) {
        logger.info("Running hbase schema manager update task.");
        logger.info("Namespace : {}, compression algorithm : {}", namespace, compression);

        List<ChangeSet> changeSets = loadChangeSets(arguments);

        boolean updated = hbaseSchemaService.update(namespace, compression, changeSets);
        if (updated) {
            logger.info("Hbase schema successfully updated.");
        } else {
            logger.info("No changes made.");
        }
    }

    private List<ChangeSet> loadChangeSets(List<String> arguments) {
        if (CollectionUtils.isEmpty(arguments)) {
            logger.info("Loading default change sets.");
            return hbaseSchemaReader.loadChangeSets();
        }
        String schemaPath = arguments.get(0);
        logger.info("Loading custom change sets from [{}].", schemaPath);
        return hbaseSchemaReader.loadChangeSets(schemaPath);
    }
}
