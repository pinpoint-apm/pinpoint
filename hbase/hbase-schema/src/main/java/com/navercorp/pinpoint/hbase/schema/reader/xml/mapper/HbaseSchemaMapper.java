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

package com.navercorp.pinpoint.hbase.schema.reader.xml.mapper;

import com.navercorp.pinpoint.hbase.schema.definition.xml.HbaseSchema;
import com.navercorp.pinpoint.hbase.schema.reader.InvalidHbaseSchemaException;
import com.navercorp.pinpoint.hbase.schema.reader.core.ChangeSet;
import com.navercorp.pinpoint.hbase.schema.reader.xml.XmlHbaseSchemaParseResult;
import org.springframework.util.CollectionUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.validation.Schema;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author HyunGil Jeong
 */
public class HbaseSchemaMapper {

    private final ChangeSetMapper changeSetMapper;

    public HbaseSchemaMapper(JAXBContext jaxbContext, Schema schema) {
        this.changeSetMapper = new ChangeSetMapper(jaxbContext, schema);
    }

    public XmlHbaseSchemaParseResult map(HbaseSchema hbaseSchema) {
        Set<String> includeFiles = mapIncludeFiles(hbaseSchema.getInclude());
        Collection<ChangeSet> changeSets = mapChangeSets(hbaseSchema.getChangeSet());
        return new XmlHbaseSchemaParseResult(includeFiles, changeSets);
    }

    private Set<String> mapIncludeFiles(List<HbaseSchema.Include> schemaIncludes) {
        if (CollectionUtils.isEmpty(schemaIncludes)) {
            return Collections.emptySet();
        }
        Set<String> includeFiles = new LinkedHashSet<>();
        for (HbaseSchema.Include schemaInclude : schemaIncludes) {
            String includeFile = schemaInclude.getFile();
            if (includeFiles.contains(includeFile)) {
                throw new InvalidHbaseSchemaException("Duplicate include file : " + includeFile);
            }
            includeFiles.add(includeFile);
        }
        return includeFiles;
    }

    private Collection<ChangeSet> mapChangeSets(List<com.navercorp.pinpoint.hbase.schema.definition.xml.ChangeSet> schemaChangeSets) {
        if (CollectionUtils.isEmpty(schemaChangeSets)) {
            return Collections.emptySet();
        }
        Map<String, ChangeSet> changeSets = new LinkedHashMap<>();
        for (com.navercorp.pinpoint.hbase.schema.definition.xml.ChangeSet schemaChangeSet : schemaChangeSets) {
            String changeSetId = schemaChangeSet.getId();
            if (changeSets.containsKey(changeSetId)) {
                throw new InvalidHbaseSchemaException("Duplicate changeSet id : " + changeSetId);
            }
            ChangeSet changeSet = changeSetMapper.mapChangeSet(schemaChangeSet);
            changeSets.put(changeSetId, changeSet);
        }
        return changeSets.values();
    }
}
