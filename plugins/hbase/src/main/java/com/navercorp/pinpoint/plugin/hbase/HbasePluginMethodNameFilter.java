/*
 *  Copyright 2018 NAVER Corp.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.navercorp.pinpoint.plugin.hbase;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilter;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The type Hbase plugin method name filter.
 *
 * @author Victor.Zxy
 * @version 1.8.1
 * @since 2018/10/12
 */
public class HbasePluginMethodNameFilter implements MethodFilter {

    /**
     * The enum Method name type.
     */
    public enum MethodNameType {

        ADMIN, TABLE
    }

    private final List<String> tableMethodNames = Arrays.asList("append", "increment", "exists", "existsAll", "get", "getScanner", "put", "checkAndPut", "delete", "checkAndDelete", "mutateRow", "checkAndMutate");

    private final List<String> adminMethodNames = Arrays.asList("tableExists", "listTables", "listTableNames", "getTableDescriptor", "createTable", "deleteTable", "modifyTable", "truncateTable", "enableTable", "enableTableAsync", "enableTables", "disableTableAsync", "disableTable", "disableTables", "getAlterStatus", "addColumn", "deleteColumn", "modifyColumn", "compact", "majorCompact", "split", "getTableRegions", "snapshot", "restoreSnapshot", "cloneSnapshot", "listSnapshots", "deleteSnapshot");

    private final Set<String> includeMethodNames = new HashSet<String>();

    /**
     * Instantiates a new Hbase plugin method name filter.
     *
     * @param type the type
     */
    public HbasePluginMethodNameFilter(MethodNameType type) {

        switch (type) {
            case ADMIN:
                this.includeMethodNames.addAll(adminMethodNames);
                break;
            case TABLE:
                this.includeMethodNames.addAll(tableMethodNames);
                break;
            default:
                break;
        }
    }

    @Override
    public boolean accept(InstrumentMethod method) {

        if (method != null) {

            final int modifiers = method.getModifiers();
            // only public.
            if (!Modifier.isPublic(modifiers) || Modifier.isStatic(modifiers) || Modifier.isAbstract(modifiers) || Modifier.isNative(modifiers)) {
                return false;
            }

            final String name = method.getName();
            // include method.
            if (this.includeMethodNames.contains(name)) {
                return true;
            }
        }
        return false;
    }
}
