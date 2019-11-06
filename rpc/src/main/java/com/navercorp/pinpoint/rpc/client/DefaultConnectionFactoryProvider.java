/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.rpc.client;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.rpc.PipelineFactory;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.util.Timer;

/**
 * @author Taejin Koo
 */
public class DefaultConnectionFactoryProvider implements ConnectionFactoryProvider {

    private final PipelineFactory pipelineFactory;

    public DefaultConnectionFactoryProvider(PipelineFactory pipelineFactory) {
        this.pipelineFactory = Assert.requireNonNull(pipelineFactory, "pipelineFactory");
    }

    @Override
    public ConnectionFactory get(Timer connectTimer, Closed closed, ChannelFactory channelFactory, SocketOption socketOption, ClientOption clientOption, ClientHandlerFactory clientHandlerFactory) {
        return new ConnectionFactory(connectTimer, closed, channelFactory, socketOption, clientOption, clientHandlerFactory, pipelineFactory);
    }

}
