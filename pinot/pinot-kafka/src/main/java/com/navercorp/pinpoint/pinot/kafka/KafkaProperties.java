package com.navercorp.pinpoint.pinot.kafka;

import org.apache.kafka.clients.producer.internals.DefaultPartitioner;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

public class KafkaProperties {
    private String bootstrapServers;
    private String keySerializer = StringSerializer.class.getName();
    private String valueSerializer = JsonSerializer.class.getName();
    private String partitionerClass = DefaultPartitioner.class.getName();
    private String acks = "1";
    private String compressionType = "zstd";


    public String getBootstrapServers() {
        return bootstrapServers;
    }

    public void setBootstrapServers(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    public String getKeySerializer() {
        return keySerializer;
    }

    public void setKeySerializer(String keySerializer) {
        this.keySerializer = keySerializer;
    }

    public String getValueSerializer() {
        return valueSerializer;
    }

    public void setValueSerializer(String valueSerializer) {
        this.valueSerializer = valueSerializer;
    }

    public String getPartitionerClass() {
        return partitionerClass;
    }

    public void setPartitionerClass(String partitionerClass) {
        this.partitionerClass = partitionerClass;
    }

    public String getAcks() {
        return acks;
    }

    public void setAcks(String acks) {
        this.acks = acks;
    }

    public String getCompressionType() {
        return compressionType;
    }

    public void setCompressionType(String compressionType) {
        this.compressionType = compressionType;
    }


    @Override
    public String toString() {
        return "KafkaProperties{" +
                "bootstrapServers='" + bootstrapServers + '\'' +
                ", keySerializer='" + keySerializer + '\'' +
                ", valueSerializer='" + valueSerializer + '\'' +
                ", partitionerClass='" + partitionerClass + '\'' +
                ", acks='" + acks + '\'' +
                ", compressionType='" + compressionType + '\'' +
                '}';
    }
}
