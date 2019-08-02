package com.navercorp.pinpoint.plugin.mongo;
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


import com.mongodb.WriteConcern;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Roy Kim
 */

public class MongoWriteConcernMapper {

    private final Map<WriteConcern, String> writeConcernMap;

    private static final String INVALID = "INVALID_WRITECONCERN";

    public MongoWriteConcernMapper() {
        writeConcernMap = buildWriteConcern();
    }

    private Map<WriteConcern, String> buildWriteConcern() {
        Map<WriteConcern, String> writeConcernMap = new HashMap<WriteConcern, String>();
        for (final Field f : WriteConcern.class.getFields()) {
            if (Modifier.isStatic(f.getModifiers())
                    && f.getType().equals(WriteConcern.class)
                    && f.getAnnotation(Deprecated.class) == null) {

                String value = f.getName();
                try {
                    WriteConcern key = (WriteConcern) f.get(null);
                    writeConcernMap.put(key, value);
                } catch (IllegalAccessException e) {
                    PLogger logger = PLoggerFactory.getLogger(this.getClass());
                    logger.warn("WriteConcern access error Caused by:" + e.getMessage(), e);
                }
            }
        }
        return writeConcernMap;
    }

    public String getName(WriteConcern writeConcern) {
        String ret = writeConcernMap.get(writeConcern);
        if (ret == null) {
            return INVALID;
        }
        return ret;
    }
}
