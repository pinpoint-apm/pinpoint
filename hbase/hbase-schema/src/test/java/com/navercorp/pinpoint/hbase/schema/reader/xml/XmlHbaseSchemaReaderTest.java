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
import com.navercorp.pinpoint.hbase.schema.reader.core.ColumnFamilyConfiguration;
import com.navercorp.pinpoint.hbase.schema.reader.core.CreateColumnFamilyChange;
import com.navercorp.pinpoint.hbase.schema.reader.core.CreateTableChange;
import com.navercorp.pinpoint.hbase.schema.reader.core.ModifyTableChange;
import com.navercorp.pinpoint.hbase.schema.reader.core.TableChange;
import com.navercorp.pinpoint.hbase.schema.reader.core.TableConfiguration;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author HyunGil Jeong
 */
public class XmlHbaseSchemaReaderTest {

    private final XmlHbaseSchemaReader reader = new XmlHbaseSchemaReader();

    @Test
    public void loadChangeSets() {
        final String schemaFilePath = "classpath:hbase-schema/test-hbase-schema.xml";

        String expectedIncludeChangeSetId = "include-1";
        List<TableChange> expectedIncludeChangeSetTableChanges = List.of(
                new CreateTableChange(
                        "IncludeTable1",
                        TableConfiguration.EMPTY_CONFIGURATION,
                        List.of(new CreateColumnFamilyChange("CF1", new ColumnFamilyConfiguration.Builder().timeToLive(5184000).build())),
                        new CreateTableChange.SplitOption.Manual(List.of("\\x01", "\\x02", "\\x03"))));

        String expectedChangeSetId1 = "id-1";
        List<TableChange> expectedChangeSet1TableChanges = List.of(
                new CreateTableChange(
                        "Table1",
                        new TableConfiguration.Builder().durability(TableConfiguration.Durability.ASYNC_WAL).build(),
                        List.of(
                                new CreateColumnFamilyChange("CF1", new ColumnFamilyConfiguration.Builder().timeToLive(86400).dataBlockEncoding(ColumnFamilyConfiguration.DataBlockEncoding.NONE).build()),
                                new CreateColumnFamilyChange("CF2", ColumnFamilyConfiguration.EMPTY_CONFIGURATION)),
                        new CreateTableChange.SplitOption.Auto(16)));

        String expectedChangeSetId2 = "id-2";
        List<TableChange> expectedChangeSet2TableChanges = List.of(
                new ModifyTableChange(
                        "Table1",
                        TableConfiguration.EMPTY_CONFIGURATION,
                        List.of(new CreateColumnFamilyChange("CF3", ColumnFamilyConfiguration.EMPTY_CONFIGURATION))));

        List<ChangeSet> changeSets = reader.loadChangeSets(schemaFilePath);
        assertThat(changeSets).hasSize(3);

        ChangeSet includeChangeSet = changeSets.get(0);
        assertMatches(includeChangeSet, expectedIncludeChangeSetId, expectedIncludeChangeSetTableChanges);
        ChangeSet changeSet1 = changeSets.get(1);
        assertMatches(changeSet1, expectedChangeSetId1, expectedChangeSet1TableChanges);
        ChangeSet changeSet2 = changeSets.get(2);
        assertMatches(changeSet2, expectedChangeSetId2, expectedChangeSet2TableChanges);
    }

    private static void assertMatches(ChangeSet cs, String expectedId, List<TableChange> expectedTableChanges) {
        assertThat(cs).matches(item -> {
            if (item != null) {
                return Objects.equals(expectedId, item.getId()) &&
                        Objects.equals(expectedTableChanges, item.getTableChanges());
            }
            return false;
        }, "ChangeSet with id : " + expectedId + ", and tableChanges : " + expectedTableChanges);
    }

}
