/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.rocketmq.interceptor;

import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.plugin.rocketmq.field.accessor.ChannelTablesAccessor;
import com.navercorp.pinpoint.plugin.rocketmq.field.accessor.ChannelTablesGetter;
import com.navercorp.pinpoint.plugin.rocketmq.field.accessor.MQClientInstanceGetter;
import org.apache.rocketmq.client.consumer.listener.MessageListener;
import org.apache.rocketmq.client.impl.consumer.DefaultMQPushConsumerImpl;
import org.apache.rocketmq.client.impl.factory.MQClientInstance;
import org.apache.rocketmq.remoting.RemotingClient;
import org.apache.rocketmq.remoting.netty.NettyRemotingClient;

/**
 * @author messi-gao
 */
public class DefaultMQPushConsumerImplStartInterceptor implements AroundInterceptor {

    @Override
    public void before(Object target, Object[] args) {

    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        DefaultMQPushConsumerImpl consumerImpl = (DefaultMQPushConsumerImpl) target;
        MessageListener messageListener =
                consumerImpl.getDefaultMQPushConsumer().getMessageListener();
        MQClientInstanceGetter mqClientInstanceGetter = (MQClientInstanceGetter) target;
        MQClientInstance mqClientInstance = mqClientInstanceGetter._$PINPOINT$_getMQClientInstance();
        RemotingClient remotingClient = mqClientInstance.getMQClientAPIImpl().getRemotingClient();
        if (remotingClient instanceof NettyRemotingClient) {
            ChannelTablesGetter nettyRemotingClient = (ChannelTablesGetter) remotingClient;
            ChannelTablesAccessor channelTablesAccessor = (ChannelTablesAccessor) messageListener;
            channelTablesAccessor._$PINPOINT$_setChannelTables(
                    nettyRemotingClient._$PINPOINT$_getChannelTables());
        }
    }
}
