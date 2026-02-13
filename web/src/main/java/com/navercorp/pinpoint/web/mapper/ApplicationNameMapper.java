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

import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.hbase.util.CellUtils;
import com.navercorp.pinpoint.web.component.ApplicationFactory;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.Service;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.client.Result;
import org.eclipse.collections.api.factory.primitive.IntSets;
import org.eclipse.collections.api.set.primitive.MutableIntSet;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 *
 */
@Component
public class ApplicationNameMapper implements RowMapper<List<Application>> {

    private final ApplicationFactory applicationFactory;

    public ApplicationNameMapper(ApplicationFactory applicationFactory) {
        this.applicationFactory = Objects.requireNonNull(applicationFactory, "applicationFactory");
    }

    @Override
    public List<Application> mapRow(Result result, int rowNum) throws Exception {
        if (result.isEmpty()) {
            return Collections.emptyList();
        }
        MutableIntSet uniqueTypeCodes = IntSets.mutable.of();
        String applicationName = CellUtils.rowToString(result);
        
        Cell[] rawCells = result.rawCells();
        for (Cell cell : rawCells) {
            int serviceTypeCode = CellUtils.valueToShort(cell);
            uniqueTypeCodes.add(serviceTypeCode);
        }

        List<Application> applicationList = new ArrayList<>();
        uniqueTypeCodes.forEach(serviceTypeCode -> {
            final Application application = applicationFactory.createApplication(Service.DEFAULT.getUid(), applicationName, serviceTypeCode);
            applicationList.add(application);
        });
        return applicationList;
    }
}
