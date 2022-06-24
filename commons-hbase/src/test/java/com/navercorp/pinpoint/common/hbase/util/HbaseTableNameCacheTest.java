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

package com.navercorp.pinpoint.common.hbase.util;

import org.apache.hadoop.hbase.NamespaceDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author HyunGil Jeong
 */
public class HbaseTableNameCacheTest {

    private final HbaseTableNameCache cache = new HbaseTableNameCache();

    @Test
    public void emptyNamespaceShouldReturnDefaultNamespace() {
        // Given
        final String nullNamespace = null;
        final String emptyNamespace = "";
        final String qualifier = "table";
        // When
        TableName tableName1 = cache.get(qualifier);
        TableName tableName2 = cache.get(nullNamespace, qualifier);
        TableName tableName3 = cache.get(emptyNamespace, qualifier);
        // Then
        Assertions.assertEquals(NamespaceDescriptor.DEFAULT_NAMESPACE_NAME_STR, tableName1.getNamespaceAsString());
        Assertions.assertEquals(qualifier, tableName1.getQualifierAsString());
        Assertions.assertEquals(NamespaceDescriptor.DEFAULT_NAMESPACE_NAME_STR, tableName2.getNamespaceAsString());
        Assertions.assertEquals(qualifier, tableName2.getQualifierAsString());
        Assertions.assertEquals(NamespaceDescriptor.DEFAULT_NAMESPACE_NAME_STR, tableName3.getNamespaceAsString());
        Assertions.assertEquals(qualifier, tableName3.getQualifierAsString());
    }

    @Test
    public void specifiedNamespace() {
        // Given
        final String namespace = "namespace";
        final String qualifier = "table";
        // When
        TableName tableName = cache.get(namespace, qualifier);
        // Then
        Assertions.assertEquals(namespace, tableName.getNamespaceAsString());
        Assertions.assertEquals(qualifier, tableName.getQualifierAsString());
    }

    @Test
    public void nullQualifierShouldThrowException() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            // Given
            final String nullQualifier = null;
            // When
            cache.get(nullQualifier);
            // Then
            Assertions.fail();
        });
    }
}
