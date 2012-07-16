package com.profiler.logging;

import com.profiler.util.NamedThreadLocal;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ThreadSafeSimpleDateFormat {

    private ThreadLocal<SimpleDateFormat> CACHE = new NamedThreadLocal<SimpleDateFormat>("SimpleDateFormatCache") {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        }
    };

    public String format(Date date) {
        return CACHE.get().format(date);
    }
}
