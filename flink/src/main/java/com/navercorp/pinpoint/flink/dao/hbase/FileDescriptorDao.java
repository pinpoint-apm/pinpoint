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
package com.navercorp.pinpoint.flink.dao.hbase;

import com.navercorp.pinpoint.common.hbase.HbaseTable;
import com.navercorp.pinpoint.common.hbase.HbaseTemplate2;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.ApplicationStatHbaseOperationFactory;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.join.ApplicationStatSerializer;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinApplicationStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinFileDescriptorBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.StatType;

/**
 * @author Roy Kim
 */
public class FileDescriptorDao extends DefaultApplicationMetricDao<JoinFileDescriptorBo> {

    public FileDescriptorDao(ApplicationStatSerializer<JoinFileDescriptorBo> serializer,
                             HbaseTemplate2 hbaseTemplate2,
                             ApplicationStatHbaseOperationFactory operations,
                             TableNameProvider tableNameProvider) {
        super(StatType.APP_FILE_DESCRIPTOR, JoinApplicationStatBo::getJoinFileDescriptorBoList, serializer, HbaseTable.APPLICATION_STAT_AGGRE,
                hbaseTemplate2, operations, tableNameProvider);
    }

}
