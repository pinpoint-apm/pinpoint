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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.web.service.ApplicationFactory;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.navercorp.pinpoint.web.vo.Application;

/**
 *
 */
@Component
public class ApplicationNameMapper implements RowMapper<List<Application>> {

    @Autowired
    private ApplicationFactory applicationFactory;

    @Override
    public List<Application> mapRow(Result result, int rowNum) throws Exception {
        if (result.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Short> uniqueTypeCodes = new HashSet<>();
        String applicationName = Bytes.toString(result.getRow());
        
        Cell[] rawCells = result.rawCells();
        for (Cell cell : rawCells) {
            short serviceTypeCode = Bytes.toShort(CellUtil.cloneValue(cell));
            uniqueTypeCodes.add(serviceTypeCode);
        }
        List<Application> applicationList = new ArrayList<>();
        for (short serviceTypeCode : uniqueTypeCodes) {
            final Application application = applicationFactory.createApplication(applicationName, serviceTypeCode);
            applicationList.add(application);
        }
        return applicationList;
    }
}
