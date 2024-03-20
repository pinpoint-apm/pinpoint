/*
 * Copyright 2024 NAVER Corp.
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
package com.navercorp.pinpoint.common.server.dao.hbase.mapper;

import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.id.ApplicationId;
import com.navercorp.pinpoint.common.util.UuidUtils;
import org.apache.hadoop.hbase.client.Result;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.Set;
import java.util.UUID;

/**
 * @author youngjin.kim2
 */
@Component
public class ServiceIndexMapper implements RowMapper<List<ApplicationId>> {

    private static final HbaseColumnFamily.ServiceApplicationIndex DESCRIPTOR = HbaseColumnFamily.SERVICE_APPLICATION_INDEX;

    @Override
    public List<ApplicationId> mapRow(Result result, int rowNum) throws Exception {
        NavigableMap<byte[], byte[]> familyMap = result.getFamilyMap(DESCRIPTOR.getName());
        if (familyMap == null || familyMap.isEmpty()) {
            return List.of();
        }

        Set<byte[]> qualifierSet = familyMap.keySet();
        List<ApplicationId> applicationIds = new ArrayList<>(qualifierSet.size());
        for (byte[] qualifier : qualifierSet) {
            applicationIds.add(parseQualifier(qualifier));
        }
        return applicationIds;
    }

    private static ApplicationId parseQualifier(byte[] qualifier) {
        UUID value = UuidUtils.fromBytes(qualifier);
        return ApplicationId.of(value);
    }

}
