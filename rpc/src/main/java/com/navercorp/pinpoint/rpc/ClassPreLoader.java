/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.rpc;

import com.navercorp.pinpoint.rpc.client.DefaultPinpointClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.rpc.client.PinpointClient;
import com.navercorp.pinpoint.rpc.client.PinpointClientFactory;
import com.navercorp.pinpoint.rpc.server.PinpointServerAcceptor;

/**
 * @author emeroad
 */
public final class ClassPreLoader {


    public static void preload() {
        try {
            preload(65535);
        } catch (Exception ignore) {
            // skip
        }
    }

    public static void preload(int port) {
        PinpointServerAcceptor serverAcceptor = null;
        PinpointClient client = null;
        PinpointClientFactory clientFactory = null;
        try {
            serverAcceptor = new PinpointServerAcceptor();
            serverAcceptor.bind("127.0.0.1", port);

            clientFactory = new DefaultPinpointClientFactory();
            client = clientFactory.connect("127.0.0.1", port);
            client.sendSync(new byte[0]);


        } catch (Exception ex) {

            System.err.print("preLoad error Caused:" + ex.getMessage());
            ex.printStackTrace();

            final Logger logger = LoggerFactory.getLogger(ClassPreLoader.class);
            logger.warn("preLoad error Caused:{}", ex.getMessage(), ex);
            if (ex instanceof PinpointSocketException) {
                throw (PinpointSocketException)ex;
            } else {
                throw new PinpointSocketException(ex.getMessage(), ex);
            }
        } finally {
            if (client != null) {
                try {
                    client.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if(clientFactory != null) {
                try {
                    clientFactory.release();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (serverAcceptor != null) {
                try {
                    serverAcceptor.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
    }

}
