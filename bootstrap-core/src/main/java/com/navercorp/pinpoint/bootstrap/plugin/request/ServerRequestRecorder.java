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

package com.navercorp.pinpoint.bootstrap.plugin.request;

import com.navercorp.pinpoint.bootstrap.context.Header;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.util.NumberUtils;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.Assert;

/**
 * @author jaehong.kim
 */
public class ServerRequestRecorder<T> {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();
    private final RequestAdaptor<T> requestAdaptor;

    public ServerRequestRecorder(RequestAdaptor<T> requestAdaptor) {
        this.requestAdaptor = Assert.requireNonNull(requestAdaptor, "requestAdaptor");
    }

    // Records the server's request information.
    public void record(final SpanRecorder recorder, final T request) {
        if (recorder == null || request == null) {
            return;
        }
        final String rpcName = requestAdaptor.getRpcName(request);
        recorder.recordRpcName(rpcName);
        if (isDebug) {
            logger.debug("Record rpcName={}", rpcName);
        }

        final String endPoint = requestAdaptor.getEndPoint(request);
        recorder.recordEndPoint(endPoint);
        if (isDebug) {
            logger.debug("Record endPoint={}", endPoint);
        }

        final String remoteAddress = requestAdaptor.getRemoteAddress(request);
        recorder.recordRemoteAddress(remoteAddress);
        if (isDebug) {
            logger.debug("Record remoteAddress={}", remoteAddress);
        }

        if (!recorder.isRoot()) {
            recordParentInfo(recorder, request);
        }
    }

    private void recordParentInfo(final SpanRecorder recorder, final T request) {
        final String parentApplicationName = requestAdaptor.getHeader(request, Header.HTTP_PARENT_APPLICATION_NAME.toString());
        if (parentApplicationName != null) {
            String host = requestAdaptor.getHeader(request, Header.HTTP_HOST.toString());
            if (host == null) {
                host = requestAdaptor.getAcceptorHost(request);
            }
            recorder.recordAcceptorHost(host);
            if (isDebug) {
                logger.debug("Record acceptorHost={}", host);
            }

            final String type = requestAdaptor.getHeader(request, Header.HTTP_PARENT_APPLICATION_TYPE.toString());
            final short parentApplicationType = NumberUtils.parseShort(type, ServiceType.UNDEFINED.getCode());
            recorder.recordParentApplication(parentApplicationName, parentApplicationType);
            if (isDebug) {
                logger.debug("Record parentApplicationName={}, parentApplicationType={}", parentApplicationName, parentApplicationType);
            }
        } else {
            if (isDebug) {
                logger.debug("Not found parentApplication");
            }
        }
    }
}