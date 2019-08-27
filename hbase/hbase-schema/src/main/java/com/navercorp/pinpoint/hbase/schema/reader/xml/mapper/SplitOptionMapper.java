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

import com.navercorp.pinpoint.hbase.schema.definition.xml.ChangeSet;
import com.navercorp.pinpoint.hbase.schema.reader.core.CreateTableChange;

/**
 * @author HyunGil Jeong
 */
public class SplitOptionMapper {

    public CreateTableChange.SplitOption mapSplitOption(ChangeSet.CreateTable.Split split) {
        if (split == null) {
            return CreateTableChange.SplitOption.NONE;
        }
        ChangeSet.CreateTable.Split.SplitKeys splitKeys = split.getSplitKeys();
        if (splitKeys != null) {
            return new CreateTableChange.SplitOption.Manual(splitKeys.getSplitKey());
        }
        ChangeSet.CreateTable.Split.Auto auto = split.getAuto();
        if (auto != null) {
            int numRegions = auto.getNumRegions();
            return new CreateTableChange.SplitOption.Auto(numRegions);
        }
        return CreateTableChange.SplitOption.NONE;
    }
}
