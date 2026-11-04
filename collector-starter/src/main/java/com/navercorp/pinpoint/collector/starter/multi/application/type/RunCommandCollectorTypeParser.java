/*
 * Copyright 2026 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.starter.multi.application.type;

import com.navercorp.pinpoint.common.server.util.ServerBootLogger;
import com.navercorp.pinpoint.common.util.StringUtils;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Parses the legacy {@code run [--types TYPE[,TYPE...]]} command line that used to be handled by Spring Shell.
 * <ul>
 *     <li>no arguments: {@link CollectorType#ALL}</li>
 *     <li>{@code run}: {@link CollectorType#ALL}</li>
 *     <li>{@code run --types BASIC,LOG}, {@code run --types=BASIC,LOG}, {@code run BASIC,LOG}</li>
 * </ul>
 * Any other command line yields an empty set, like the previous shell based parser did on an unknown command.
 */
public class RunCommandCollectorTypeParser implements CollectorTypeParser {

    private static final ServerBootLogger logger = ServerBootLogger.getLogger(RunCommandCollectorTypeParser.class);

    static final String RUN_COMMAND = "run";
    static final String TYPES_OPTION = "--types";

    @Override
    public CollectorTypeSet parse(String[] args) {
        if (args == null || args.length == 0) {
            return new CollectorTypeSet(Set.of(CollectorType.ALL));
        }
        if (!RUN_COMMAND.equals(args[0])) {
            logger.error("Unknown command: " + Arrays.toString(args) + ", expected: " + RUN_COMMAND + " [" + TYPES_OPTION + " TYPE,...]");
            return new CollectorTypeSet(Set.of());
        }
        try {
            return new CollectorTypeSet(parseTypes(Arrays.asList(args).subList(1, args.length)));
        } catch (IllegalArgumentException e) {
            logger.error("Failed to parse collector types " + Arrays.toString(args), e);
            return new CollectorTypeSet(Set.of());
        }
    }

    private Set<CollectorType> parseTypes(List<String> options) {
        if (options.isEmpty()) {
            return Set.of(CollectorType.ALL);
        }
        final String value;
        final String first = options.get(0);
        if (first.startsWith(TYPES_OPTION + "=")) {
            value = first.substring(TYPES_OPTION.length() + 1);
            if (options.size() > 1) {
                throw new IllegalArgumentException("unexpected arguments: " + options.subList(1, options.size()));
            }
        } else if (TYPES_OPTION.equals(first)) {
            if (options.size() != 2) {
                throw new IllegalArgumentException(TYPES_OPTION + " requires exactly one value: " + options);
            }
            value = options.get(1);
        } else {
            if (options.size() != 1) {
                throw new IllegalArgumentException("unexpected arguments: " + options);
            }
            value = first;
        }
        Set<CollectorType> types = new LinkedHashSet<>();
        for (String type : StringUtils.tokenizeToStringList(value, ",")) {
            types.add(CollectorType.valueOf(type.toUpperCase()));
        }
        if (types.isEmpty()) {
            throw new IllegalArgumentException("empty collector type: " + value);
        }
        return types;
    }
}
