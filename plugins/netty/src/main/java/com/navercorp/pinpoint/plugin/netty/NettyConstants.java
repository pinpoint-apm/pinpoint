/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.netty;

import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.AnnotationKeyFactory;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;
import com.navercorp.pinpoint.common.trace.ServiceTypeProperty;

import static com.navercorp.pinpoint.common.trace.AnnotationKeyProperty.VIEW_IN_RECORD_SET;

/**
 * @author Taejin Koo
 */
public class NettyConstants {

    public static final ServiceType SERVICE_TYPE = ServiceTypeFactory.of(9150, "NETTY");
    public static final ServiceType SERVICE_TYPE_INTERNAL = ServiceTypeFactory.of(9151, "NETTY_INTERNAL");

    public static final ServiceType SERVICE_TYPE_CODEC_HTTP = ServiceTypeFactory.of(9152, "NETTY_HTTP", "NETTY_HTTP", ServiceTypeProperty.RECORD_STATISTICS);


    public static final AnnotationKey NETTY_ADDRESS = AnnotationKeyFactory.of(120, "netty.address", VIEW_IN_RECORD_SET);


    public static final String SCOPE = "NETTY_SCOPE";
    public static final String SCOPE_WRITE = "NETTY_WRITE_SCOPE";


    public static final String INTERCEPTOR_BASIC = "com.navercorp.pinpoint.bootstrap.interceptor.BasicMethodInterceptor";

    public static final String INTERCEPTOR_CHANNEL_PIPELINE_WRITE = "com.navercorp.pinpoint.plugin.netty.interceptor.ChannelPipelineWriteInterceptor";
    public static final String INTERCEPTOR_CHANNEL_PROMISE_ADD_LISTENER = "com.navercorp.pinpoint.plugin.netty.interceptor.ChannelPromiseAddListenerInterceptor";
    public static final String INTERCEPTOR_CHANNEL_PROMISE_NOTIFY = "com.navercorp.pinpoint.plugin.netty.interceptor.ChannelPromiseNotifyInterceptor";

    public static final String INTERCEPTOR_BOOTSTRAP_CONNECT = "com.navercorp.pinpoint.plugin.netty.interceptor.BootstrapConnectInterceptor";

    public static final String INTERCEPTOR_CODEC_HTTP_ENCODER = "com.navercorp.pinpoint.plugin.netty.interceptor.http.HttpEncoderInterceptor";


    public static final String UNKNOWN_ADDRESS = "Unknown";

}
