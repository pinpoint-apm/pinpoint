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

package com.navercorp.pinpoint.web.dao.hbase.stat.v2;

import com.navercorp.pinpoint.common.server.bo.codec.stat.FileDescriptorDecoder;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatType;
import com.navercorp.pinpoint.common.server.bo.stat.FileDescriptorBo;
import com.navercorp.pinpoint.web.dao.stat.FileDescriptorDao;
import com.navercorp.pinpoint.web.mapper.stat.AgentStatMapperV2;
import com.navercorp.pinpoint.web.vo.Range;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * @author Roy Kim
 */
@Repository("fileDescriptorDaoV2")
public class HbaseFileDescriptorDaoV2 implements FileDescriptorDao {

    private final FileDescriptorDecoder fileDescriptorDecoder;

    private final HbaseAgentStatDaoOperationsV2 operations;

    public HbaseFileDescriptorDaoV2(HbaseAgentStatDaoOperationsV2 operations, FileDescriptorDecoder fileDescriptorDecoder) {
        this.operations = Objects.requireNonNull(operations, "operations");
        this.fileDescriptorDecoder = Objects.requireNonNull(fileDescriptorDecoder, "fileDescriptorDecoder");
    }

    @Override
    public List<FileDescriptorBo> getAgentStatList(String agentId, Range range) {
        AgentStatMapperV2<FileDescriptorBo> mapper = operations.createRowMapper(fileDescriptorDecoder, range);
        return operations.getAgentStatList(AgentStatType.FILE_DESCRIPTOR, mapper, agentId, range);
    }

    @Override
    public boolean agentStatExists(String agentId, Range range) {
        AgentStatMapperV2<FileDescriptorBo> mapper = operations.createRowMapper(fileDescriptorDecoder, range);
        return operations.agentStatExists(AgentStatType.FILE_DESCRIPTOR, mapper, agentId, range);
    }

}
