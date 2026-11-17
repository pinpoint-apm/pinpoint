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
import org.apache.activemq.ActiveMQMessageProducer;
import org.apache.activemq.AsyncCallback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@RestController
public class ActiveMQ5Controller {

    static final String LISTENER_QUEUE = "pinpoint.activemq5.listener";
    static final String RECEIVE_QUEUE = "pinpoint.activemq5.receive";

    private static final long TIMEOUT_MILLIS = 3000;

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final Connection connection;
    private final RequestMappingHandlerMapping handlerMapping;

    public ActiveMQ5Controller(Connection connection,
                               @Qualifier("requestMappingHandlerMapping") RequestMappingHandlerMapping handlerMapping) {
        this.connection = connection;
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
        return new ApiLinkPage("activemq-5-original")
                .addHrefTag(list)
                .build();
    }

    /**
     * Sends to the queue that {@link ActiveMQ5Listener} consumes.
     */
    @GetMapping("/activemq5/send")
    public String send() throws JMSException {
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        try {
            MessageProducer producer = session.createProducer(session.createQueue(LISTENER_QUEUE));
            producer.send(session.createTextMessage("send"));
            return "sent to " + LISTENER_QUEUE;
        } finally {
            session.close();
        }
    }

    /**
     * Sends with an ActiveMQ AsyncCallback to the queue that {@link ActiveMQ5Listener} consumes.
     */
    @GetMapping("/activemq5/sendAsync")
    public String sendAsync() throws Exception {
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        try {
            Queue queue = session.createQueue(LISTENER_QUEUE);
            ActiveMQMessageProducer producer = (ActiveMQMessageProducer) session.createProducer(null);
            CompletableFuture<String> result = new CompletableFuture<>();
            producer.send(queue, session.createTextMessage("sendAsync"), new AsyncCallback() {
                @Override
                public void onSuccess() {
                    result.complete("sent asynchronously to " + LISTENER_QUEUE);
                }

                @Override
                public void onException(JMSException exception) {
                    logger.warn("Failed to send", exception);
                    result.completeExceptionally(exception);
                }
            });
            return result.get(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
        } finally {
            session.close();
        }
    }

    /**
     * Sends and then pulls the message back with MessageConsumer.receive, the receive path of the consumer.
     */
    @GetMapping("/activemq5/sendAndReceive")
    public String sendAndReceive() throws JMSException {
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        try {
            Queue queue = session.createQueue(RECEIVE_QUEUE);
            MessageConsumer consumer = session.createConsumer(queue);
            session.createProducer(queue).send(session.createTextMessage("sendAndReceive"));
            Message message = consumer.receive(TIMEOUT_MILLIS);
            if (message == null) {
                return "nothing received from " + RECEIVE_QUEUE;
            }
            return "received " + ((TextMessage) message).getText();
        } finally {
            session.close();
        }
    }
}
