/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.web.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author emeroad
 */
public class SearchLevel {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final int limit;
    private final int level;

    public SearchLevel(int limit) {
        this(limit, 0);
    }

    private SearchLevel(int limit, int level) {
        if (limit < 0) {
            throw new IllegalArgumentException("negative limit " + limit);
        }
        if (level < 0) {
            throw new IllegalArgumentException("negative level" + level);
        }
        this.limit = limit;
        this.level = level;
    }

    public SearchLevel nextLevel() {
        final int nextLevel = next();
        return new SearchLevel(limit, nextLevel);
    }

    public int getLevel() {
        return level;
    }

    public boolean isLevelOverflow() {
        if (limit < level) {
            logger.debug("level overflow level:{}, limit:{}", level, limit);
            return true;
        }
        return false;
    }

    private int next() {
        return level + 1;
    }

}
