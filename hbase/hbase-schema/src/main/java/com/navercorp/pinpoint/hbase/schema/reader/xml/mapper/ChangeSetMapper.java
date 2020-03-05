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

import com.navercorp.pinpoint.hbase.schema.reader.HbaseSchemaParseException;
import com.navercorp.pinpoint.hbase.schema.reader.InvalidHbaseSchemaException;
import com.navercorp.pinpoint.hbase.schema.reader.core.ChangeSet;
import com.navercorp.pinpoint.hbase.schema.reader.core.TableChange;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.validation.Schema;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
public class ChangeSetMapper {

    private final JAXBContext jaxbContext;
    private final Schema schema;
    private final TableChangeMapper tableChangeMapper;

    public ChangeSetMapper(JAXBContext jaxbContext, Schema schema) {
        this.jaxbContext = Objects.requireNonNull(jaxbContext, "jaxbContext");
        this.schema = schema;
        this.tableChangeMapper = new TableChangeMapper();
    }

    public ChangeSet mapChangeSet(com.navercorp.pinpoint.hbase.schema.definition.xml.ChangeSet changeSet) {
        try {
            StringWriter stringWriter = new StringWriter(); // no need to close StringWriter
            createMarshaller().marshal(changeSet, stringWriter);
            String value = stringWriter.toString();

            List<TableChange> tableChangeList = mapTableChanges(changeSet.getModifyTable(), changeSet.getCreateTable());
            return new ChangeSet(changeSet.getId(), value, tableChangeList);
        } catch (JAXBException e) {
            Throwable linkedException = e.getLinkedException();
            if (linkedException == null) {
                throw new IllegalStateException("JAXB error", e);
            }
            throw new HbaseSchemaParseException("Error computing md5 for change set id : " + changeSet.getId(), linkedException);
        }
    }

    private Marshaller createMarshaller() throws JAXBException {
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setSchema(schema);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
        return marshaller;
    }

    private List<TableChange> mapTableChanges(List<com.navercorp.pinpoint.hbase.schema.definition.xml.ChangeSet.ModifyTable> modifyTables,
                                              List<com.navercorp.pinpoint.hbase.schema.definition.xml.ChangeSet.CreateTable> createTables) {
        if (modifyTables.isEmpty() && createTables.isEmpty()) {
            return Collections.emptyList();
        }
        List<TableChange> tableChanges = new ArrayList<>(modifyTables.size() + createTables.size());
        for (com.navercorp.pinpoint.hbase.schema.definition.xml.ChangeSet.ModifyTable modifyTable : modifyTables) {
            try {
                tableChanges.add(tableChangeMapper.map(modifyTable));
            } catch (InvalidHbaseSchemaException e) {
                throw new InvalidHbaseSchemaException("modifyTable : " + modifyTable.getName() + ", " + e.getMessage(), e.getCause());
            }
        }
        for (com.navercorp.pinpoint.hbase.schema.definition.xml.ChangeSet.CreateTable createTable : createTables) {
            try {
                tableChanges.add(tableChangeMapper.map(createTable));
            } catch (InvalidHbaseSchemaException e) {
                throw new InvalidHbaseSchemaException("createTable : " + createTable.getName() + ", " + e.getMessage(), e.getCause());
            }
        }
        return tableChanges;
    }
}
