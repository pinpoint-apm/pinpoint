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

package com.navercorp.pinpoint.hbase.manager;

import com.navercorp.pinpoint.hbase.manager.task.HbaseSchemaManagerTask;
import com.navercorp.pinpoint.hbase.manager.task.HelpTask;
import com.navercorp.pinpoint.hbase.manager.task.InitTask;
import com.navercorp.pinpoint.hbase.manager.task.PrintSchemaChangeLogTask;
import com.navercorp.pinpoint.hbase.manager.task.PrintSchemaChangeSummaryTask;
import com.navercorp.pinpoint.hbase.manager.task.ResetTask;
import com.navercorp.pinpoint.hbase.manager.task.UpdateTask;
import com.navercorp.pinpoint.hbase.schema.reader.HbaseSchemaReader;
import com.navercorp.pinpoint.hbase.schema.service.HbaseSchemaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author HyunGil Jeong
 */
@Component
public class HbaseSchemaManagerTaskFactory {

    @Autowired
    private HbaseSchemaReader hbaseSchemaReader;

    @Autowired
    private HbaseSchemaService hbaseSchemaService;

    @Autowired
    private XmlFormatter xmlFormatter;

    public HbaseSchemaManagerTask create(ProgramCommand programCommand, ProgramOptions programOptions) {
        if (programCommand == ProgramCommand.EMPTY) {
            return new HelpTask();
        }
        String commandString = programCommand.getCommand();
        Command command = Command.fromValue(commandString);
        if (command == null) {
            return new HelpTask();
        }

        switch (command) {
            case INIT: {
                String namespace = programOptions.getNamespace();
                return new InitTask(hbaseSchemaService, namespace);
            }
            case UPDATE: {
                String namespace = programOptions.getNamespace();
                String compression = programOptions.getCompression();
                return new UpdateTask(hbaseSchemaService, hbaseSchemaReader, namespace, compression);
            }
            case RESET: {
                String namespace = programOptions.getNamespace();
                return new ResetTask(hbaseSchemaService, namespace);
            }
            case SUMMARY: {
                String namespace = programOptions.getNamespace();
                return new PrintSchemaChangeSummaryTask(hbaseSchemaService, namespace);
            }
            case LOG: {
                String namespace = programOptions.getNamespace();
                return new PrintSchemaChangeLogTask(hbaseSchemaService, xmlFormatter, namespace);
            }
            case HELP:
                return new HelpTask();
            default:
                return new HelpTask();
        }
    }
}
