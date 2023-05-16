package com.navercorp.pinpoint.uristat.collector.dao;

import com.navercorp.pinpoint.pinot.kafka.util.KafkaCallbacks;
import com.navercorp.pinpoint.uristat.collector.model.UriStat;
import com.navercorp.pinpoint.uristat.collector.util.StringPrecondition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Repository;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.List;
import java.util.Objects;

@Repository
public class PinotUriStatDao implements UriStatDao {
    private final Logger logger = LogManager.getLogger(getClass());

    private final KafkaTemplate<String, UriStat> kafkaUriStatTemplate;

    private final String topic;

    private final ListenableFutureCallback<SendResult<String, UriStat>> resultCallback
            = KafkaCallbacks.loggingCallback("Kafka(UriStat)", logger);

    public PinotUriStatDao(@Qualifier("kafkaUriStatTemplate") KafkaTemplate<String, UriStat> kafkaUriStatTemplate,
                           @Value("${kafka.uri.topic}") String topic) {
        this.kafkaUriStatTemplate = Objects.requireNonNull(kafkaUriStatTemplate, "kafkaUriStatTemplate");
        this.topic = StringPrecondition.requireHasLength(topic, "topic");
    }

    @Override
    public void insert(List<UriStat> data) {
        Objects.requireNonNull(data);

        for (UriStat uriStat : data) {
            ListenableFuture<SendResult<String, UriStat>> response = this.kafkaUriStatTemplate.send(topic, uriStat.getApplicationName(), uriStat);
            response.addCallback(resultCallback);
        }

    }
}
