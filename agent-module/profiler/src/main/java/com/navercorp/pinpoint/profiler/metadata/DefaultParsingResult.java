/*
 * Copyright 2017 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.metadata;


/**
 * @author emeroad
 */
public class DefaultParsingResult extends ParsingResultInternal<Integer> {
    private Integer id = null;

    public DefaultParsingResult(String originalSql) {
        super(originalSql);
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public boolean setId(Integer id) {
        // clear originalSql reference
        clearOriginalSql();

        if (this.id == null && id != null) {
            this.id = id;
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "DefaultParsingResult{" +
                "sql=" + getSql() +
                ", output=" + getOutput() +
                ", id=" + id +
                '}';
    }
}
