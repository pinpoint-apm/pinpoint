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

package com.navercorp.pinpoint.collector.receiver.thrift;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.rpc.PipelineFactory;
import com.navercorp.pinpoint.rpc.server.ChannelFilter;
import com.navercorp.pinpoint.rpc.server.PinpointServerAcceptor;
import com.navercorp.pinpoint.rpc.server.ServerCodecPipelineFactory;
import com.navercorp.pinpoint.rpc.server.ServerOption;

/**
 * @author Taejin Koo
 */
public class PinpointServerAcceptorProvider {

    private ServerOption serverOption = ServerOption.getDefaultServerOption();
    private ChannelFilter channelFilter = ChannelFilter.BYPASS;
    private PipelineFactory pipelineFactory = new ServerCodecPipelineFactory();

    public PinpointServerAcceptor get() {
        return new PinpointServerAcceptor(serverOption, channelFilter, pipelineFactory);
    }

    public void setServerOption(ServerOption serverOption) {
        this.serverOption = Assert.requireNonNull(serverOption, "serverOption must not be null");
    }

    public void setChannelFilter(ChannelFilter channelFilter) {
        this.channelFilter = Assert.requireNonNull(channelFilter, "channelFilter must not be null");
    }

    public void setPipelineFactory(PipelineFactory pipelineFactory) {
        this.pipelineFactory = Assert.requireNonNull(pipelineFactory, "pipelineFactory must not be null");
    }

}
