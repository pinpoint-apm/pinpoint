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

package com.navercorp.pinpoint.hbase.schema.reader.xml;

import com.navercorp.pinpoint.hbase.schema.reader.core.ChangeSet;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
public class XmlHbaseSchemaParseResult {

    private final Collection<String> includeFiles;
    private final Collection<ChangeSet> changeSets;

    public XmlHbaseSchemaParseResult(Collection<String> includeFiles, Collection<ChangeSet> changeSets) {
        this.includeFiles = Objects.requireNonNull(includeFiles, "includeFiles");
        this.changeSets = Objects.requireNonNull(changeSets, "changeSets");
    }

    public Collection<String> getIncludeFiles() {
        return Collections.unmodifiableCollection(includeFiles);
    }

    public Collection<ChangeSet> getChangeSets() {
        return Collections.unmodifiableCollection(changeSets);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("XmlHbaseSchemaParseResult{");
        sb.append("includeFiles=").append(includeFiles);
        sb.append(", changeSets=").append(changeSets);
        sb.append('}');
        return sb.toString();
    }
}
