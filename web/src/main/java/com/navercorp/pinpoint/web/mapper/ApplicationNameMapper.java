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

import com.navercorp.pinpoint.common.ServiceType;
import com.navercorp.pinpoint.web.service.ServiceTypeRegistryService;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Component;

import com.navercorp.pinpoint.web.vo.Application;

/**
 *
 */
@Component
public class ApplicationNameMapper implements RowMapper<Application> {

    @Autowired
    private ServiceTypeRegistryService registry;

    @Override
    public Application mapRow(Result result, int rowNum) throws Exception {
        if (result.isEmpty()) {
            return null;
        }
        String applicationName = Bytes.toString(result.getRow());
        short serviceTypeCode = Bytes.toShort(result.value());

        ServiceType serviceType = registry.findServiceType(serviceTypeCode);
        return new Application(applicationName, serviceType);
    }
}
