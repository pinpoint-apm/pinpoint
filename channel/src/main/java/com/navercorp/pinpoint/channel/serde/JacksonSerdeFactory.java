package com.navercorp.pinpoint.channel.serde;

import tools.jackson.databind.JavaType;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.ObjectReader;
import tools.jackson.databind.ObjectWriter;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class JacksonSerdeFactory implements JsonSerdeFactory {

    private final JsonMapper jsonMapper;

    public JacksonSerdeFactory(JsonMapper jsonMapper) {
        this.jsonMapper = Objects.requireNonNull(jsonMapper, "jsonMapper");
    }

    @Override
    public <T> Serde<T> byClass(Class<T> clazz) {
        JavaType javaType = jsonMapper.constructType(clazz);
        ObjectReader reader = jsonMapper.readerFor(javaType);
        ObjectWriter writer = jsonMapper.writerFor(javaType);
        return new JacksonSerde<>(reader, writer);
    }

    @Override
    public <T> Serde<T> byParameterized(
            Class<?> parameterized,
            Class<?>... parameterizedClasses
    ) {
        JavaType type = jsonMapper.getTypeFactory().constructParametricType(parameterized, parameterizedClasses);
        ObjectReader reader = jsonMapper.readerFor(type);
        ObjectWriter writer = jsonMapper.writerFor(type);
        return new JacksonSerde<>(reader, writer);
    }


}
