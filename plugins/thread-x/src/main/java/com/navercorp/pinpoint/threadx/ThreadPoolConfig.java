package com.navercorp.pinpoint.threadx;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
public class ThreadPoolConfig {

    private static final String[] DEFAULT_ASYNC_TASK_EXECUTOR = {
            "org.springframework.scheduling.concurrent.ConcurrentTaskExecutor",
            "org.springframework.core.task.SimpleAsyncTaskExecutor",
            "org.springframework.scheduling.quartz.SimpleThreadPoolTaskExecutor",
            "org.springframework.core.task.support.TaskExecutorAdapter",
            "org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor",
            "org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler",
            "org.springframework.jca.work.WorkManagerTaskExecutor",
            "org.springframework.scheduling.commonj.WorkManagerTaskExecutor"
    };

    private final List<String> asyncTaskExecutorClassNameList = new ArrayList<String>(Arrays.asList(DEFAULT_ASYNC_TASK_EXECUTOR));

    private final boolean enable;



    public List<String> getAsyncTaskExecutorClassNameList() {
        return asyncTaskExecutorClassNameList;
    }

    public ThreadPoolConfig(ProfilerConfig config) {
        this.enable = config.readBoolean("profiler.thread.pool.enable", true);
    }

    public boolean isEnable() {
        return enable;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ThreadPoolConfig{");
        sb.append("enable=").append(enable);
        sb.append('}');
        return sb.toString();
    }
}
