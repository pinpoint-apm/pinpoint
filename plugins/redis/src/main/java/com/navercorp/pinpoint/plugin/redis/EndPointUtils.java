/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.redis;

import com.navercorp.pinpoint.common.plugin.util.HostAndPort;

/**
 * @author Woonduk Kang(emeroad)
 */
public class EndPointUtils {

    public static String getEndPoint(Object[] args) {
        if (args[0] instanceof String) {
            final String host = (String) args[0];
            final int port = getPort(args);
            return HostAndPort.toHostAndPortString(host, port);
        }
        return "";
    }

    private static int getPort(Object[] args) {
        // second argument is port
        if (args.length >= 2 && args[1] instanceof Integer) {
            return (Integer) args[1];
        }
        // default port
        return 6379;
    }

}
