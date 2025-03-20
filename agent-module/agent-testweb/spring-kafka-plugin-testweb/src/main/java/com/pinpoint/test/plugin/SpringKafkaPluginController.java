package com.pinpoint.test.plugin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SpringKafkaPluginController {

    @Autowired
    private SpringKafkaApplication.MessageProducer producer;

    @GetMapping("/")
    public String welcome() {
        return "WELCOME";
    }

    @GetMapping("/producer")
    public String producer() {

        producer.sendMessage("Hello, World!");

        for (int i = 0; i < 5; i++) {
            producer.sendMessageToPartition("Hello To Partitioned Topic!", i);
        }

        /*
         * Sending message to 'filtered' topic. As per listener
         * configuration,  all messages with char sequence
         * 'World' will be discarded.
         */
        producer.sendMessageToFiltered("Hello Baeldung!");
        producer.sendMessageToFiltered("Hello World!");

        /*
         * Sending message to 'greeting' topic. This will send
         * and received a java object with the help of
         * greetingKafkaListenerContainerFactory.
         */
        producer.sendGreetingMessage(new Greeting("Greetings", "World!"));

        return "OK";
    }

}
