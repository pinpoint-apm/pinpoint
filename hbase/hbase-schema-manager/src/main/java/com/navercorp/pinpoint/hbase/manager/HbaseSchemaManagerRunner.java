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

import com.navercorp.pinpoint.common.hbase.HbaseSystemException;
import com.navercorp.pinpoint.hbase.manager.logging.Markers;
import com.navercorp.pinpoint.hbase.manager.task.HbaseSchemaManagerTask;
import com.navercorp.pinpoint.hbase.schema.reader.HbaseSchemaParseException;
import com.navercorp.pinpoint.hbase.schema.reader.InvalidHbaseSchemaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * @author HyunGil Jeong
 */
@Component
public class HbaseSchemaManagerRunner implements ApplicationRunner {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private HbaseSchemaManagerTaskFactory taskFactory;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        logger.info("Launched with arguments : {}", Arrays.asList(args.getSourceArgs()));
        ProgramCommand programCommand = ProgramCommand.parseArgs(args);
        ProgramOptions programOptions = ProgramOptions.parseArgs(args);
        HbaseSchemaManagerTask task = taskFactory.create(programCommand, programOptions);
        try {
            task.run(programCommand.getCommandArgs());
        } catch (HbaseSchemaParseException | InvalidHbaseSchemaException e) {
            logger.error(Markers.TERMINAL, "{}, cause : {}", e.getMessage(), e.getCause().getMessage());
            logger.error(Markers.APP_LOG, "Hbase schema error.", e);
        } catch (HbaseSystemException e) {
            logger.error(Markers.TERMINAL, "Error accessing hbase : {}", e.getMessage());
            logger.error(Markers.APP_LOG, "Hbase error.", e);
        } catch (Exception e) {
            logger.error(Markers.TERMINAL, "Error running '{}' : {}", programCommand.getCommand(), e.getMessage());
            logger.error(Markers.APP_LOG, "Error running '" + programCommand.getCommand() + "'.", e);
        }
    }
}
