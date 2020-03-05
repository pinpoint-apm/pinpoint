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

import com.navercorp.pinpoint.hbase.manager.logging.Markers;
import com.navercorp.pinpoint.hbase.manager.task.HelpTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Collections;

/**
 * @author HyunGil Jeong
 */
@SpringBootApplication(scanBasePackages = {
        "com.navercorp.pinpoint.hbase.manager",
        "com.navercorp.pinpoint.hbase.schema"})
public class HbaseSchemaManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(HbaseSchemaManager.class);

    public static void main(String[] args) {
        ProgramCommand programCommand = ProgramCommand.parseArgs(args);
        if (programCommand == ProgramCommand.EMPTY) {
            printHelp();
            return;
        }
        Command command = Command.fromValue(programCommand.getCommand());
        if (command == null) {
            LOGGER.info(Markers.TERMINAL, "'{}' is not a valid command.", programCommand.getCommand());
            printHelp();
            return;
        }
        if (command == Command.HELP) {
            printHelp();
            return;
        }
        SpringApplication application = new SpringApplication(HbaseSchemaManager.class);
        application.setBannerMode(Banner.Mode.OFF);
        application.run(args);
    }

    private static void printHelp() {
        HelpTask helpTask = new HelpTask();
        helpTask.run(Collections.emptyList());
    }
}
