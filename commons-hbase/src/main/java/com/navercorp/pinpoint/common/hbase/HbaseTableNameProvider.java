/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.common.hbase;

import com.navercorp.pinpoint.common.annotations.VisibleForTesting;
import com.navercorp.pinpoint.common.hbase.namespace.HbaseNamespaceValidator;
import com.navercorp.pinpoint.common.hbase.namespace.NamespaceValidator;
import com.navercorp.pinpoint.common.hbase.util.HbaseTableNameCache;
import com.navercorp.pinpoint.common.util.StringUtils;
import org.apache.hadoop.hbase.TableName;

import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
public class HbaseTableNameProvider implements TableNameProvider {

    private static final HbaseTableNameCache CACHE = new HbaseTableNameCache();

    private final String namespace;

    public HbaseTableNameProvider(String namespace) {
        this(namespace, HbaseNamespaceValidator.INSTANCE);
    }

    @VisibleForTesting
    HbaseTableNameProvider(String namespace, NamespaceValidator namespaceValidator) {
        Objects.requireNonNull(namespaceValidator, "namespaceValidator");
        this.namespace = requireValidation(namespace, namespaceValidator);
    }

    private String requireValidation(String namespace, NamespaceValidator namespaceValidator) {
        if (StringUtils.isEmpty(namespace)) {
            throw new IllegalArgumentException("Namespace must not be empty");
        }
        if (!namespaceValidator.validate(namespace)) {
            throw new IllegalArgumentException("Invalid namespace : " + namespace);
        }
        return namespace;
    }

    @Override
    public TableName getTableName(HbaseTable hBaseTable) {
        return getTableName(hBaseTable.getName());
    }

    @Override
    public TableName getTableName(String tableName) {
        return CACHE.get(namespace, tableName);
    }

    @Override
    public boolean hasDefaultNameSpace() {
        return true;
    }

}
