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

/**
 * @author emeroad
 */
public class SearchDepth {
    private final int limit;
    private final int depth;

    public SearchDepth(int limit) {
        this(limit, 0);
    }

    private SearchDepth(int limit, int depth) {
        if (limit < 0) {
            throw new IllegalArgumentException("negative limit " + limit);
        }
        if (depth < 0) {
            throw new IllegalArgumentException("negative depth" + depth);
        }
        this.limit = limit;
        this.depth = depth;
    }

    public SearchDepth nextDepth() {
        final int nextLevel = next();
        return new SearchDepth(limit, nextLevel);
    }

    public int getDepth() {
        return depth;
    }

    public int getLimit() {
        return limit;
    }


    public boolean isDepthOverflow() {
        if (limit <= depth) {
            return true;
        }
        return false;
    }

    private int next() {
        return depth + 1;
    }


}
