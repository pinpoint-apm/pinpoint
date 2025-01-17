package com.navercorp.pinpoint.common.profiler;

import java.io.Closeable;
import java.util.function.Consumer;

public final class IOUtils {

    public static void closeQuietly(Closeable closeable) {
        closeQuietly(closeable, null);
    }

    public static void closeQuietly(Closeable closeable, Consumer<Throwable> consumer) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Throwable ioe) {
                if (consumer != null) {
                    consumer.accept(ioe);
                }
            }
        }
    }
}
