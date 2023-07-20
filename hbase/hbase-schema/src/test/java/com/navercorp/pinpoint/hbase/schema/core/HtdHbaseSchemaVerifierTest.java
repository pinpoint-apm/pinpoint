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

package com.navercorp.pinpoint.hbase.schema.core;

import com.navercorp.pinpoint.common.util.CollectionUtils;
import org.apache.hadoop.hbase.NamespaceDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.ColumnFamilyDescriptorBuilder;
import org.apache.hadoop.hbase.client.TableDescriptor;
import org.apache.hadoop.hbase.client.TableDescriptorBuilder;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author HyunGil Jeong
 */
public class HtdHbaseSchemaVerifierTest {

    private final HbaseSchemaVerifier<TableDescriptor> verifier = new HtdHbaseSchemaVerifier();

    @Test
    public void emptyExpectedSchemas_shouldReturnTrue() {
        List<TableDescriptor> emptyExpectedSchemas = Collections.emptyList();
        List<TableDescriptor> actualSchemas = List.of(createHtd("table1", "table1_1"));
        assertThat(verifier.verifySchemas(null, actualSchemas)).isTrue();
        assertThat(verifier.verifySchemas(emptyExpectedSchemas, actualSchemas)).isTrue();
    }

    @Test
    public void emptyActualSchemas_shouldReturnFalse() {
        List<TableDescriptor> emptyActualSchemas = Collections.emptyList();
        List<TableDescriptor> expectedSchemas = List.of(createHtd("table1", "table1_1"));
        assertThat(verifier.verifySchemas(expectedSchemas, null)).isFalse();
        assertThat(verifier.verifySchemas(expectedSchemas, emptyActualSchemas)).isFalse();
    }

    @Test
    public void exactMatch_shouldReturnTrue() {
        List<TableDescriptor> expectedSchemas = List.of(
                createHtd("table1", "table1_1"),
                createHtd("table2", "table2_1", "table2_2", "table2_3"),
                createHtd("table3"));
        List<TableDescriptor> actualSchemas = copySchema(expectedSchemas);
        assertThat(verifier.verifySchemas(expectedSchemas, actualSchemas)).isTrue();
    }

    @Test
    public void excessiveTableNameMatch_shouldReturnTrue() {
        List<TableDescriptor> expectedSchemas = List.of(
                createHtd("table1", "table1_1"),
                createHtd("table2", "table2_1", "table2_2", "table2_3"),
                createHtd("table3"));
        List<TableDescriptor> actualSchemas = copySchema(expectedSchemas);
        actualSchemas.add(createHtd("table4", "table4_1"));
        assertThat(verifier.verifySchemas(expectedSchemas, actualSchemas)).isTrue();
    }

    @Test
    public void excessiveColumnFamilyMatch_shouldReturnTrue() {
        List<TableDescriptor> expectedSchemas = List.of(
                createHtd("table1", "table1_1"),
                createHtd("table2", "table2_1", "table2_2", "table2_3"),
                createHtd("table3"));
        List<TableDescriptorBuilder> actualSchemas = copyToBuilder(expectedSchemas);
        for (TableDescriptorBuilder htd : actualSchemas) {
            htd.setColumnFamily(ColumnFamilyDescriptorBuilder.of("newCF"));
        }
        assertThat(verifier.verifySchemas(expectedSchemas, build(actualSchemas))).isTrue();
    }

    @Test
    public void partialTableNameMatch_shouldReturnFalse() {
        List<TableDescriptor> actualSchemas = List.of(
                createHtd("table1", "table1_1"),
                createHtd("table2", "table2_1", "table2_2", "table2_3"),
                createHtd("table3"));
        List<TableDescriptorBuilder> expectedSchemas = copyToBuilder(actualSchemas);
        expectedSchemas.add(TableDescriptorBuilder.newBuilder(createHtd("table4", "table4_1")));
        assertThat(verifier.verifySchemas(build(expectedSchemas), actualSchemas)).isFalse();
    }

    @Test
    public void partialColumnFamilyMatch_shouldReturnFalse() {
        List<TableDescriptor> actualSchemas = List.of(
                createHtd("table1", "table1_1"),
                createHtd("table2", "table2_1", "table2_2", "table2_3"),
                createHtd("table3"));
        List<TableDescriptorBuilder> expectedSchemas = copyToBuilder(actualSchemas);
        for (TableDescriptorBuilder htd : expectedSchemas) {
            htd.setColumnFamily(ColumnFamilyDescriptorBuilder.of("newCF"));
        }
        assertThat(verifier.verifySchemas(build(expectedSchemas), actualSchemas)).isFalse();
    }

    @Test
    public void tableNameMismatch_shouldReturnFalse() {
        List<TableDescriptor> expectedSchemas = List.of(createHtd("table1", "CF1"));
        List<TableDescriptor> actualSchemas = List.of(createHtd("table2", "CF1"));
        assertThat(verifier.verifySchemas(expectedSchemas, actualSchemas)).isFalse();
    }

    @Test
    public void columnFamilyMismatch_shouldReturnFalse() {
        List<TableDescriptor> expectedSchemas = List.of(createHtd("table1", "CF1"));
        List<TableDescriptor> actualSchemas = List.of(createHtd("table1", "CF2"));
        assertThat(verifier.verifySchemas(expectedSchemas, actualSchemas)).isFalse();
    }

    private TableDescriptor createHtd(String tableQualifier, String... columnFamilyNames) {
        TableName tableName = TableName.valueOf(NamespaceDescriptor.DEFAULT_NAMESPACE_NAME_STR, tableQualifier);

        TableDescriptorBuilder builder = TableDescriptorBuilder.newBuilder(tableName);
        for (String columnFamilyName : columnFamilyNames) {
            builder.setColumnFamily(ColumnFamilyDescriptorBuilder.of(columnFamilyName));
        }
        return builder.build();
    }

    private List<TableDescriptor> copySchema(List<TableDescriptor> htds) {
        if (CollectionUtils.isEmpty(htds)) {
            return Collections.emptyList();
        }
        return new ArrayList<>(htds);
    }

    private List<TableDescriptorBuilder> copyToBuilder(List<TableDescriptor> htds) {
        if (CollectionUtils.isEmpty(htds)) {
            return Collections.emptyList();
        }
        return htds.stream()
                .map(TableDescriptorBuilder::newBuilder)
                .collect(Collectors.toList());
    }

    private List<TableDescriptor> build(List<TableDescriptorBuilder> htds) {
        if (CollectionUtils.isEmpty(htds)) {
            return Collections.emptyList();
        }
        return htds.stream()
                .map(TableDescriptorBuilder::build)
                .collect(Collectors.toList());
    }
}
