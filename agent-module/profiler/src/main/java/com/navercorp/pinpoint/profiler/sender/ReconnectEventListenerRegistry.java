package com.navercorp.pinpoint.profiler.sender;

import java.util.function.Consumer;

public interface ReconnectEventListenerRegistry<T> {
    boolean addEventListener(Consumer<T> eventListener);

}
