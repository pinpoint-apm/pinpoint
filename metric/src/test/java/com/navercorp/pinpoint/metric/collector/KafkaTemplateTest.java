package com.navercorp.pinpoint.metric.collector;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContext-kafka-template-test.xml"})
public class KafkaTemplateTest {

    @Autowired
    private KafkaTemplate<String, String> kafkaTestTemplate;

    @Ignore
    @Test
    public void testAutowiredTemplate() {
        kafkaTestTemplate.sendDefault("hello");
    }

    @Ignore
    @Test
    public void testKafkaTemplate() {
        Map<String, Object> senderProps = senderProps();
        ProducerFactory<String, String> pf = new DefaultKafkaProducerFactory<>(senderProps);
        KafkaTemplate<String, String> template = new KafkaTemplate<>(pf);
        template.setDefaultTopic("test-topic");
        template.sendDefault("hello");
    }

    private Map<String, Object> senderProps() {
        Map<String, Object> senderProps = new HashMap<>();
        senderProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:19092");
        senderProps.put(ProducerConfig.ACKS_CONFIG, "all");
        senderProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        senderProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        return senderProps;
    }
}
