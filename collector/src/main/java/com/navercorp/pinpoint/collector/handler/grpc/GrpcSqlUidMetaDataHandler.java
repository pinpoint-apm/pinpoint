/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.handler.grpc;

import com.navercorp.pinpoint.collector.service.SqlMetaDataService;
import com.navercorp.pinpoint.collector.service.SqlUidMetaDataService;
import com.navercorp.pinpoint.thrift.io.DefaultTBaseLocator;
import org.springframework.stereotype.Service;

/**
 * @author emeroad
 */
@Service
public class GrpcSqlUidMetaDataHandler extends GrpcSqlMetaDataHandler {

    public GrpcSqlUidMetaDataHandler(SqlMetaDataService[] sqlMetaDataServices, SqlUidMetaDataService[] sqlUidMetaDataServices) {
        super(sqlMetaDataServices, sqlUidMetaDataServices);
    }

    @Override
    public int type() {
        return DefaultTBaseLocator.SQLUIDMETADATA;
    }
}