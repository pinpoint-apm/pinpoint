/*
 * Copyright 2019 NAVER Corp.
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
import com.navercorp.pinpoint.hbase.schema.reader.core.ColumnFamilyChange;
import com.navercorp.pinpoint.hbase.schema.reader.core.ColumnFamilyConfiguration;
import com.navercorp.pinpoint.hbase.schema.reader.core.CreateColumnFamilyChange;
import com.navercorp.pinpoint.hbase.schema.reader.core.CreateTableChange;
import com.navercorp.pinpoint.hbase.schema.reader.core.ModifyTableChange;
import com.navercorp.pinpoint.hbase.schema.reader.core.TableChange;
import com.navercorp.pinpoint.hbase.schema.reader.core.TableConfiguration;
import org.junit.Test;
import org.xml.sax.InputSource;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author HyunGil Jeong
 */
public class XmlHbaseSchemaParserTest {

    private static final XmlHbaseSchemaParser parser = new XmlHbaseSchemaParser();

    @Test
    public void parseSchema() {
        final String schemaFile = "hbase-schema/test-hbase-schema.xml";

        TableConfiguration expectedChangeSet1_tableConfiguration = new TableConfiguration.Builder().durability(TableConfiguration.Durability.ASYNC_WAL).build();
        List<ColumnFamilyChange> expectedChangeSet1_columnFamilies = Arrays.asList(
                new CreateColumnFamilyChange("CF1", new ColumnFamilyConfiguration.Builder().timeToLive(86400).dataBlockEncoding(ColumnFamilyConfiguration.DataBlockEncoding.NONE).build()),
                new CreateColumnFamilyChange("CF2", ColumnFamilyConfiguration.EMPTY_CONFIGURATION));
        CreateTableChange.SplitOption expectedChangeSet1_tableSplitOption = new CreateTableChange.SplitOption.Auto(16);
        TableChange expectedChangeSet1_tableChange = new CreateTableChange(
                "Table1",
                expectedChangeSet1_tableConfiguration,
                expectedChangeSet1_columnFamilies,
                expectedChangeSet1_tableSplitOption);

        List<ColumnFamilyChange> expectedChangeSet2_columnFamilies = Arrays.asList(
                new CreateColumnFamilyChange("CF3", ColumnFamilyConfiguration.EMPTY_CONFIGURATION));
        TableChange expectedChangeSet2_tableChange = new ModifyTableChange(
                "Table1",
                TableConfiguration.EMPTY_CONFIGURATION,
                expectedChangeSet2_columnFamilies);

        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(schemaFile);
        XmlHbaseSchemaParseResult parseResult = parser.parseSchema(new InputSource(inputStream));

        List<String> includeFiles = new ArrayList<>(parseResult.getIncludeFiles());
        assertThat(includeFiles.size(), is(1));
        String includeFile = includeFiles.get(0);
        assertThat(includeFile, is("test-hbase-schema-include.xml"));

        List<ChangeSet> changeSets = new ArrayList<>(parseResult.getChangeSets());
        assertThat(changeSets.size(), is(2));

        ChangeSet changeSet1 = changeSets.get(0);
        assertThat(changeSet1.getId(), is("id-1"));
        assertThat(changeSet1.getTableChanges(), is(Arrays.asList(expectedChangeSet1_tableChange)));
        ChangeSet changeSet2 = changeSets.get(1);
        assertThat(changeSet2.getId(), is("id-2"));
        assertThat(changeSet2.getTableChanges(), is(Arrays.asList(expectedChangeSet2_tableChange)));
    }
}
