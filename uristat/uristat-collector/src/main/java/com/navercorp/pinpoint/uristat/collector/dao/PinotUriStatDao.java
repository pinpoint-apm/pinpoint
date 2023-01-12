package com.navercorp.pinpoint.uristat.collector.dao;

import com.navercorp.pinpoint.uristat.common.util.StringPrecondition;
import com.navercorp.pinpoint.uristat.common.model.UriStat;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

@Repository
public class PinotUriStatDao implements UriStatDao {

    private final KafkaTemplate<String, UriStat> kafkaUriStatTemplate;

    private final String topic;

    public PinotUriStatDao(@Qualifier("kafkaUriStatTemplate") KafkaTemplate<String, UriStat> kafkaUriStatTemplate,
                           @Value("${kafka.uri.topic}") String topic) {
        this.kafkaUriStatTemplate = Objects.requireNonNull(kafkaUriStatTemplate, "kafkaUriStatTemplate");
        this.topic = StringPrecondition.requireHasLength(topic, "topic");
    }

    @Override
    public void insert(List<UriStat> data) {
        Objects.requireNonNull(data);

        for (UriStat uriStat : data) {
            this.kafkaUriStatTemplate.send(topic, uriStat.getApplicationName(), uriStat);
        }

    }
}
