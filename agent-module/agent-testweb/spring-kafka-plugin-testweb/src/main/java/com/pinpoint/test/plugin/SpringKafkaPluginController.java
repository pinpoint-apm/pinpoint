package com.pinpoint.test.plugin;

import com.pinpoint.test.common.view.ApiLinkPage;
import com.pinpoint.test.common.view.HrefTag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.reactive.result.method.RequestMappingInfo;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@RestController
public class SpringKafkaPluginController {

    @Autowired
    private SpringKafkaApplication.MessageProducer producer;
    private final RequestMappingHandlerMapping handlerMapping;

    @Autowired
    public SpringKafkaPluginController(RequestMappingHandlerMapping handlerMapping) {
        this.handlerMapping = handlerMapping;
    }

    @GetMapping("/")
    String welcome() {
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = this.handlerMapping.getHandlerMethods();
        List<HrefTag> list = new ArrayList<>();
        for (RequestMappingInfo info : handlerMethods.keySet()) {
            for (String path : info.getDirectPaths()) {
                list.add(HrefTag.of(path));
            }
        }
        list.sort(Comparator.comparing(HrefTag::getPath));
        return new ApiLinkPage("spring-webflux-plugin-testweb")
                .addHrefTag(list)
                .build();
    }

    @GetMapping("/producer/send")
    public String producer() {
        producer.sendMessage("Hello, World!");

        return "OK";
    }

    @GetMapping("/producer/partition")
    public String producerPartition() {
        for (int i = 0; i < 5; i++) {
            producer.sendMessageToPartition("Hello To Partitioned Topic!", i);
        }
        return "OK";
    }

    @GetMapping("/producer/filtered")
    public String producerFiltered() {
        /*
         * Sending message to 'filtered' topic. As per listener
         * configuration,  all messages with char sequence
         * 'World' will be discarded.
         */
        producer.sendMessageToFiltered("Hello Baeldung!");
        producer.sendMessageToFiltered("Hello World!");
        return "OK";
    }

    @GetMapping("/producer/greeting")
    public String producerGreeting() {
        /*
         * Sending message to 'greeting' topic. This will send
         * and received a java object with the help of
         * greetingKafkaListenerContainerFactory.
         */
        producer.sendGreetingMessage(new Greeting("Greetings", "World!"));

        return "OK";
    }

    @GetMapping("/producer/streams")
    public String producerStreams() {

        producer.sendStreamMessage("Hello, World!");
        return "OK";
    }
}