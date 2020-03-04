/*
 * Copyright 2018 Naver Corp.
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

package com.navercorp.pinpoint.collector.mapper.thrift.stat;

import com.navercorp.pinpoint.collector.mapper.thrift.ThriftBoMapper;
import com.navercorp.pinpoint.common.server.bo.stat.FileDescriptorBo;
import com.navercorp.pinpoint.thrift.dto.TFileDescriptor;
import org.springframework.stereotype.Component;

/**
 * @author Roy Kim
 */
@Component
public class ThriftFileDescriptorBoMapper implements ThriftBoMapper<FileDescriptorBo, TFileDescriptor> {

    @Override
    public FileDescriptorBo map(TFileDescriptor tOpenFileDescriptor) {
        FileDescriptorBo fileDescriptorBo = new FileDescriptorBo();
        fileDescriptorBo.setOpenFileDescriptorCount(tOpenFileDescriptor.getOpenFileDescriptorCount());
        return fileDescriptorBo;
    }
}
