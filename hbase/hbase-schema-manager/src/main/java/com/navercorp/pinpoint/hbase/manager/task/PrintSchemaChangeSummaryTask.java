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

import com.navercorp.pinpoint.hbase.schema.domain.SchemaChangeLog;
import com.navercorp.pinpoint.hbase.schema.service.HbaseSchemaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
public class PrintSchemaChangeSummaryTask implements HbaseSchemaManagerTask {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final HbaseSchemaService hbaseSchemaService;
    private final String namespace;

    public PrintSchemaChangeSummaryTask(HbaseSchemaService hbaseSchemaService, String namespace) {
        this.hbaseSchemaService = Objects.requireNonNull(hbaseSchemaService, "hbaseSchemaService");
        this.namespace = namespace;
    }

    @Override
    public void run(List<String> arguments) {
        List<SchemaChangeLog> schemaChangeLogs = hbaseSchemaService.getChangeLogs(namespace);
        printSchemaChangeLogs(schemaChangeLogs);
    }

    private void printSchemaChangeLogs(List<SchemaChangeLog> schemaChangeLogs) {
        if (CollectionUtils.isEmpty(schemaChangeLogs)) {
            logger.info("No schema change logs found.");
            return;
        }
        logger.info("Schema change logs : ");
        for (SchemaChangeLog schemaChangeLog : schemaChangeLogs) {
            logger.info("id : {}, order : {}, timestamp : {}",
                    schemaChangeLog.getId(), schemaChangeLog.getExecOrder(), schemaChangeLog.getExecTimestamp());
        }
    }
}
