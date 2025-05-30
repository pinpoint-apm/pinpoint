package com.navercorp.pinpoint.channel.serde;

public interface JsonSerdeFactory {
    <T> Serde<T> byClass(Class<T> clazz);

    <T> Serde<T> byParameterized(
            Class<?> parameterized,
            Class<?>... parameterizedClasses
    );
}
