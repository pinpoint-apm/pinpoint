/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.thrift;

import com.navercorp.pinpoint.grpc.trace.PCmdEcho;
import com.navercorp.pinpoint.thrift.dto.command.TCommandEcho;

import com.google.protobuf.GeneratedMessageV3;

/**
 * @author Taejin Koo
 */
public class CommandThriftToGrpcMessageConverter implements MessageConverter<GeneratedMessageV3> {

    @Override
    public GeneratedMessageV3 toMessage(Object message) {
        if (message instanceof TCommandEcho) {
            return buildPCommandEcho((TCommandEcho) message);
        }
        return null;
    }

    private PCmdEcho buildPCommandEcho(TCommandEcho tCommandEcho) {
        PCmdEcho.Builder builder = PCmdEcho.newBuilder();
        builder.setMessage(tCommandEcho.getMessage());
        return builder.build();
    }

}
