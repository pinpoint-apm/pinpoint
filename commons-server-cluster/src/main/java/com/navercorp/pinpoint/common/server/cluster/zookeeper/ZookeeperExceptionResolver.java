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

package com.navercorp.pinpoint.common.server.cluster.zookeeper;

import com.navercorp.pinpoint.common.server.cluster.zookeeper.exception.AuthException;
import com.navercorp.pinpoint.common.server.cluster.zookeeper.exception.BadOperationException;
import com.navercorp.pinpoint.common.server.cluster.zookeeper.exception.ConnectionException;
import com.navercorp.pinpoint.common.server.cluster.zookeeper.exception.NoNodeException;
import com.navercorp.pinpoint.common.server.cluster.zookeeper.exception.PinpointZookeeperException;
import com.navercorp.pinpoint.common.server.cluster.zookeeper.exception.TimeoutException;
import com.navercorp.pinpoint.common.server.cluster.zookeeper.exception.UnknownException;
import org.apache.zookeeper.KeeperException;

/**
 * @author Taejin Koo
 */
public final class ZookeeperExceptionResolver {

    private ZookeeperExceptionResolver() {
    }

    public static void resolveAndThrow(Exception exception) throws PinpointZookeeperException {
        throw resolve(exception);
    }

    public static PinpointZookeeperException resolve(Exception exception) {
        if (exception instanceof KeeperException keeperException) {
            return switch (keeperException.code()) {
                case CONNECTIONLOSS, SESSIONEXPIRED ->
                        new ConnectionException(keeperException.getMessage(), keeperException);
                case AUTHFAILED, INVALIDACL, NOAUTH ->
                        new AuthException(keeperException.getMessage(), keeperException);
                case BADARGUMENTS, BADVERSION, NOCHILDRENFOREPHEMERALS, NOTEMPTY, NODEEXISTS ->
                        new BadOperationException(keeperException.getMessage(), keeperException);
                case NONODE ->
                        new NoNodeException(keeperException.getMessage(), keeperException);
                case OPERATIONTIMEOUT ->
                        new TimeoutException(keeperException.getMessage(), keeperException);
                default -> new UnknownException(keeperException.getMessage(), keeperException);
            };
        } else {
            return new UnknownException(exception.getMessage(), exception);
        }
    }

}
