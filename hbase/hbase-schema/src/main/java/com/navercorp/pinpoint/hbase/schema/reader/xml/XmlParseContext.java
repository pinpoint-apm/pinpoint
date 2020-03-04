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
import org.springframework.core.io.Resource;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author HyunGil Jeong
 */
class XmlParseContext {

    private final Map<String, ChangeSet> changeSetMap = new LinkedHashMap<>();

    private final Set<Resource> visitedResources = new HashSet<>();

    private Resource resource;

    XmlParseContext(Resource resource) {
        this.resource = Objects.requireNonNull(resource, "resource");
    }

    Resource getResource() {
        return resource;
    }

    void setResource(Resource resource) {
        this.resource = Objects.requireNonNull(resource, "resource");
        if (visitedResources.contains(resource)) {
            throw new IllegalStateException("Cyclic include detected for : " + resource);
        }
        visitedResources.add(resource);
    }

    void addChangeSets(Collection<ChangeSet> changeSets) {
        if (CollectionUtils.isEmpty(changeSets)) {
            return;
        }
        for (ChangeSet changeSet : changeSets) {
            String changeSetId = changeSet.getId();
            ChangeSet previous = changeSetMap.put(changeSetId, changeSet);
            if (previous != null) {
                throw new IllegalStateException("Duplicate changeSet found. Id: " + changeSetId);
            }
        }
    }

    List<ChangeSet> getChangeSets() {
        return new ArrayList<>(changeSetMap.values());
    }
}
