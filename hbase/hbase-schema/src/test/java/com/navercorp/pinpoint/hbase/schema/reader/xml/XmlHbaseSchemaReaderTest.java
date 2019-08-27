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
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author HyunGil Jeong
 */
public class XmlHbaseSchemaReaderTest {

    private final XmlHbaseSchemaReader reader = new XmlHbaseSchemaReader();

    @Test
    public void loadChangeSets() {
        final String schemaFilePath = "classpath:hbase-schema/test-hbase-schema.xml";

        String expectedIncludeChangeSetId = "include-1";
        List<TableChange> expectedIncludeChangeSetTableChanges = Arrays.asList(
                new CreateTableChange(
                        "IncludeTable1",
                        TableConfiguration.EMPTY_CONFIGURATION,
                        Arrays.asList(new CreateColumnFamilyChange("CF1", new ColumnFamilyConfiguration.Builder().timeToLive(5184000).build())),
                        new CreateTableChange.SplitOption.Manual(Arrays.asList("\\x01", "\\x02", "\\x03"))));

        String expectedChangeSetId1 = "id-1";
        List<TableChange> expectedChangeSet1TableChanges = Arrays.asList(
                new CreateTableChange(
                        "Table1",
                        new TableConfiguration.Builder().durability(TableConfiguration.Durability.ASYNC_WAL).build(),
                        Arrays.asList(
                                new CreateColumnFamilyChange("CF1", new ColumnFamilyConfiguration.Builder().timeToLive(86400).dataBlockEncoding(ColumnFamilyConfiguration.DataBlockEncoding.NONE).build()),
                                new CreateColumnFamilyChange("CF2", ColumnFamilyConfiguration.EMPTY_CONFIGURATION)),
                        new CreateTableChange.SplitOption.Auto(16)));

        String expectedChangeSetId2 = "id-2";
        List<TableChange> expectedChangeSet2TableChanges = Arrays.asList(
                new ModifyTableChange(
                        "Table1",
                        TableConfiguration.EMPTY_CONFIGURATION,
                        Arrays.asList(new CreateColumnFamilyChange("CF3", ColumnFamilyConfiguration.EMPTY_CONFIGURATION))));

        List<ChangeSet> changeSets = reader.loadChangeSets(schemaFilePath);
        assertThat(changeSets.size(), is(3));

        ChangeSet includeChangeSet = changeSets.get(0);
        assertThat(includeChangeSet, matches(expectedIncludeChangeSetId, expectedIncludeChangeSetTableChanges));
        ChangeSet changeSet1 = changeSets.get(1);
        assertThat(changeSet1, matches(expectedChangeSetId1, expectedChangeSet1TableChanges));
        ChangeSet changeSet2 = changeSets.get(2);
        assertThat(changeSet2, matches(expectedChangeSetId2, expectedChangeSet2TableChanges));

    }

    private static Matcher<ChangeSet> matches(String expectedId, List<TableChange> expectedTableChanges) {
        return new BaseMatcher<ChangeSet>() {
            @Override
            public boolean matches(Object item) {
                if (item instanceof ChangeSet) {
                    ChangeSet actualChangeSet = (ChangeSet) item;
                    return Objects.equals(expectedId, actualChangeSet.getId()) &&
                            Objects.equals(expectedTableChanges, actualChangeSet.getTableChanges());
                }
                return false;
            }

            @Override
            public void describeTo(Description description) {
                description.appendValue("ChangeSet with id : " + expectedId + ", and tableChanges : " + expectedTableChanges);
            }
        };
    }

}
