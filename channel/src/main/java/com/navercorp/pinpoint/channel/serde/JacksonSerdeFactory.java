package com.navercorp.pinpoint.channel.serde;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class JacksonSerdeFactory implements JsonSerdeFactory {

    private final ObjectMapper objectMapper;

    public JacksonSerdeFactory(ObjectMapper objectMapper) {
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
    }

    @Override
    public <T> Serde<T> byClass(Class<T> clazz) {
        JavaType javaType = objectMapper.constructType(clazz);
        ObjectReader reader = objectMapper.readerFor(javaType);
        ObjectWriter writer = objectMapper.writerFor(javaType);
        return new JacksonSerde<>(reader, writer);
    }

    @Override
    public <T> Serde<T> byParameterized(
            Class<?> parameterized,
            Class<?>... parameterizedClasses
    ) {
        JavaType type = objectMapper.getTypeFactory().constructParametricType(parameterized, parameterizedClasses);
        ObjectReader reader = objectMapper.readerFor(type);
        ObjectWriter writer = objectMapper.writerFor(type);
        return new JacksonSerde<>(reader, writer);
    }


}
