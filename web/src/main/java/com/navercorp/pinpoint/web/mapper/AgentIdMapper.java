/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.web.mapper;

import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.stereotype.Component;

import com.navercorp.pinpoint.common.hbase.RowMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 */
@Component
public class AgentIdMapper implements RowMapper<List<String>> {

    @Override
    public List<String> mapRow(Result result, int rowNum) throws Exception {
        if (result.isEmpty()) {
            return Collections.emptyList();
        }
        final Cell[] rawCells = result.rawCells();
        final List<String> agentIdList = new ArrayList<>(rawCells.length);

        for (Cell cell : rawCells) {
            final String agentId = Bytes.toString(CellUtil.cloneQualifier(cell));
            agentIdList.add(agentId);
        }

        return agentIdList;
    }
}
