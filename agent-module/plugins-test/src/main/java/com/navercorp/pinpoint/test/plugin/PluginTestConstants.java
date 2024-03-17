/*
 * Copyright 2014 NAVER Corp.
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
package com.navercorp.pinpoint.test.plugin;


import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

/**
 * @author Jongho Moon
 *
 */
public final class PluginTestConstants {
    private PluginTestConstants() {
    }

    public static final String JUNIT_OUTPUT_DELIMITER = "#####";
    public static final String JUNIT_OUTPUT_DELIMITER_REGEXP = Pattern.quote(JUNIT_OUTPUT_DELIMITER);
    public static final String PINPOINT_TEST_ID = "pinpoint.test.id";
    public static final String CHILD_CLASS_PATH_PREFIX = "-child=";

    public static final String CAUSED_DELIMITER = "$CAUSE$";

    public static final String TAG = "PLUGIN-TEST";

    // AgentParameter
    public static final String AGENT_PARAMETER_IMPORT_PLUGIN = "IMPORT_PLUGIN";
    // AgentParser
    public static final String AGENT_PARSER_DELIMITER = ",";

    public static final Charset UTF_8 = StandardCharsets.UTF_8;
    public static final String UTF_8_NAME = UTF_8.name();
}
