/*
 * Copyright 2026 NAVER Corp.
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

package com.pinpoint.test.plugin;

import com.pinpoint.test.common.view.ApiLinkPage;
import com.pinpoint.test.common.view.HrefTag;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@RestController
public class ActiveMQ6Controller {

    static final String LISTENER_QUEUE = "pinpoint.activemq6.listener";
    static final String SIMPLE_LISTENER_QUEUE = "pinpoint.activemq6.simple-listener";
    static final String RECEIVE_QUEUE = "pinpoint.activemq6.receive";

    private final JmsTemplate jmsTemplate;
    private final RequestMappingHandlerMapping handlerMapping;

    public ActiveMQ6Controller(JmsTemplate jmsTemplate,
                               @Qualifier("requestMappingHandlerMapping") RequestMappingHandlerMapping handlerMapping) {
        this.jmsTemplate = jmsTemplate;
        this.handlerMapping = handlerMapping;
    }

    @GetMapping("/")
    public String welcome() {
        List<HrefTag> list = new ArrayList<>();
        for (RequestMappingInfo info : handlerMapping.getHandlerMethods().keySet()) {
            for (String path : info.getDirectPaths()) {
                list.add(HrefTag.of(path));
            }
        }
        list.sort(Comparator.comparing(HrefTag::getPath));
        return new ApiLinkPage("activemq-6-springboot")
                .addHrefTag(list)
                .build();
    }

    /**
     * Sends with JmsTemplate to the queue of the default @JmsListener container.
     */
    @GetMapping("/activemq6/send")
    public String send() {
        jmsTemplate.convertAndSend(LISTENER_QUEUE, "send");
        return "sent to " + LISTENER_QUEUE;
    }

    /**
     * Sends with JmsTemplate to the queue of the simple @JmsListener container.
     */
    @GetMapping("/activemq6/sendToSimpleListener")
    public String sendToSimpleListener() {
        jmsTemplate.convertAndSend(SIMPLE_LISTENER_QUEUE, "sendToSimpleListener");
        return "sent to " + SIMPLE_LISTENER_QUEUE;
    }

    /**
     * Sends and then pulls the message back with JmsTemplate.receive.
     */
    @GetMapping("/activemq6/sendAndReceive")
    public String sendAndReceive() {
        jmsTemplate.convertAndSend(RECEIVE_QUEUE, "sendAndReceive");
        Object received = jmsTemplate.receiveAndConvert(RECEIVE_QUEUE);
        if (received == null) {
            return "nothing received from " + RECEIVE_QUEUE;
        }
        return "received " + received;
    }
}
