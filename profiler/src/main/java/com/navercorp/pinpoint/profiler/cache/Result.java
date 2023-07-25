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

package com.navercorp.pinpoint.profiler.cache;

import java.util.Objects;

/**
 * @author emeroad
 */
public class Result<ID> {

    private final boolean newValue;
    private final ID id;

    public Result(boolean newValue, ID id) {
        this.newValue = newValue;
        this.id = Objects.requireNonNull(id, "id");
    }

    public boolean isNewValue() {
        return newValue;
    }

    public ID getId() {
        return id;
    }
}
