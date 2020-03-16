/*
 * Copyright 2016 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.bootstrap;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ArgsParser {
    public static final String DELIMITER = ",";
    private static final String DELIMITER_PATTERN = "\\s*" + DELIMITER + "\\s*";

    public Map<String, String> parse(String args) {
        if (isEmpty(args)) {
            return Collections.emptyMap();
        }

        final Map<String, String> map = new HashMap<String, String>();

        Scanner scanner = new Scanner(args);
        scanner.useDelimiter(DELIMITER_PATTERN);

        while (scanner.hasNext()) {
            String token = scanner.next();
            int assign = token.indexOf('=');

            if (assign == -1) {
                map.put(token, "");
            } else {
                String key = token.substring(0, assign);
                String value = token.substring(assign + 1);
                map.put(key, value);
            }
        }
        scanner.close();
        return Collections.unmodifiableMap(map);
    }

    private boolean isEmpty(String args) {
        return args == null || args.isEmpty();
    }

}
