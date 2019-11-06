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

import com.navercorp.pinpoint.hbase.manager.logging.Markers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author HyunGil Jeong
 */
public class HelpTask implements HbaseSchemaManagerTask {

    private static final String LINE_BREAK = System.lineSeparator();

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void run(List<String> arguments) {
        logger.info(Markers.TERMINAL, createHelpMessage());
    }

    private String createHelpMessage() {
        StringBuilder sb = new StringBuilder("usage:").append(LINE_BREAK);
        sb.append("java -jar <jarfile> [--namespace=<namespace>] [--compression=<algorithm>] [--dry]").append(LINE_BREAK);
        sb.append("                    [--hbase.host=<hostname>] [--hbase.port=<port>] [--hbase.znodeParent=<parent>]").append(LINE_BREAK);
        sb.append("                    <command> [<args>]").append(LINE_BREAK);
        sb.append(LINE_BREAK);
        sb.append("commands:").append(LINE_BREAK);
        sb.append(LINE_BREAK);
        sb.append("    init     Enable and initialize hbase schema management.").append(LINE_BREAK);
        sb.append("             usage   : java -jar <jarfile> [<options>] init").append(LINE_BREAK);
        sb.append("             options : namespace, dry, hbase.host, hbase.port, hbase.znodeParent").append(LINE_BREAK);
        sb.append(LINE_BREAK);
        sb.append("    update   Update current hbase schema, optionally with <schema-path> given as argument.").append(LINE_BREAK);
        sb.append("             Uses pinpoint's default change sets if no argument is given.").append(LINE_BREAK);
        sb.append("             usage   : java -jar <jarfile> [<options>] update [<schema-path>]").append(LINE_BREAK);
        sb.append("             options : namespace, compression, dry, hbase.host, hbase.port, hbase.znodeParent").append(LINE_BREAK);
        sb.append(LINE_BREAK);
        sb.append("    reset    Reset current hbase schema management, deleting all records of executed change sets.").append(LINE_BREAK);
        sb.append("             This DOES NOT delete any existing tables, it simply returns the hbase schema management").append(LINE_BREAK);
        sb.append("             to a clean state as if the 'init' command has been run for the first time.").append(LINE_BREAK);
        sb.append("             usage   : java -jar <jarfile> [<options>] reset").append(LINE_BREAK);
        sb.append("             options : namespace, dry, hbase.host, hbase.port, hbase.znodeParent").append(LINE_BREAK);
        sb.append(LINE_BREAK);
        sb.append("    summary  Print the summary of all executed change sets.").append(LINE_BREAK);
        sb.append("             usage   : java -jar <jarfile> [<options>] summary").append(LINE_BREAK);
        sb.append("             options : namespace, hbase.host, hbase.port, hbase.znodeParent").append(LINE_BREAK);
        sb.append(LINE_BREAK);
        sb.append("    log      Print all (or specified) executed change sets.").append(LINE_BREAK);
        sb.append("             usage   : java -jar <jarfile> [<options>] log [<changeSet-id>]").append(LINE_BREAK);
        sb.append("             options : namespace, hbase.host, hbase.port, hbase.znodeParent").append(LINE_BREAK);
        sb.append(LINE_BREAK);
        sb.append("options:").append(LINE_BREAK);
        sb.append(LINE_BREAK);
        sb.append("    --namespace          Hbase namespace to use.").append(LINE_BREAK);
        sb.append("                         Defaults to 'default' namespace if option is not present.").append(LINE_BREAK);
        sb.append(LINE_BREAK);
        sb.append("    --compression        Hbase compression algorithm to use (eg SNAPPY).").append(LINE_BREAK);
        sb.append("                         Defaults to 'none' if option is not present.").append(LINE_BREAK);
        sb.append(LINE_BREAK);
        sb.append("    --dry                Executes a dry run, without making changes to the current hbase schema.").append(LINE_BREAK);
        sb.append("                         Defaults to non-dry run if option is not present.").append(LINE_BREAK);
        sb.append(LINE_BREAK);
        sb.append("    --hbase.host         Hbase host to connect to, analogous to 'hbase.zookeeper.quorum' hbase property.").append(LINE_BREAK);
        sb.append("                         Defaults to value set in 'hbase.properties' inside the <jarfile> if option is not present.").append(LINE_BREAK);
        sb.append(LINE_BREAK);
        sb.append("    --hbase.port         Hbase port to connect to, analogous to 'hbase.zookeeper.property.clientPort' hbase property.").append(LINE_BREAK);
        sb.append("                         Defaults to value set in 'hbase.properties' inside the <jarfile> if option is not present.").append(LINE_BREAK);
        sb.append(LINE_BREAK);
        sb.append("    --hbase.znodeParent  Parent ZK node, analogous to 'zookeeper.znode.parent' hbase property.").append(LINE_BREAK);
        sb.append("                         Defaults to value set in 'hbase.properties' inside the <jarfile> if option is not present.").append(LINE_BREAK);
        return sb.toString();
    }
}
