package com.navercorp.pinpoint.plugin.rabbitmq;

import com.navercorp.pinpoint.bootstrap.config.ExcludePathFilter;
import com.navercorp.pinpoint.bootstrap.config.Filter;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.SkipFilter;
import com.navercorp.pinpoint.bootstrap.util.StringUtils;

/**
 * @author Jiaqi Feng
 */
public class RabbitMQClientPluginConfig {

    private final boolean traceRabbitMQClient;
    private final boolean traceRabbitMQClientProducer;
    private final Boolean traceRabbitMQClientConsumer;
    private final Filter<String> excludeExchangeFilter;

    public RabbitMQClientPluginConfig(ProfilerConfig config) {
        this.traceRabbitMQClient = config.readBoolean("profiler.rabbitmq.client.enable", true);
        this.traceRabbitMQClientProducer = config.readBoolean("profiler.rabbitmq.client.producer.enable", true);
        this.traceRabbitMQClientConsumer = config.readBoolean("profiler.rabbitmq.client.consumer.enable", true);

        String excludeExchnage = config.readString("profiler.rabbitmq.client.exchange.exclude", "");
        if (!excludeExchnage.isEmpty()) {
            this.excludeExchangeFilter = new ExcludePathFilter(excludeExchnage);
        } else {
            this.excludeExchangeFilter = new SkipFilter<String>();
        }
    }

    public boolean isTraceRabbitMQClient() {
        return this.traceRabbitMQClient;
    }

    public boolean isTraceRabbitMQClientProducer() {
        return this.traceRabbitMQClientProducer;
    }

    public boolean isTraceRabbitMQClientConsumer() {
        return this.traceRabbitMQClientConsumer;
    }

    public Filter<String> getExcludeExchangeFilter() {
        return this.excludeExchangeFilter;
    }

    public static boolean isExchangeExcluded(String exchange, Filter<String> filter) {
        if (exchange == null || filter == null)
            return false;
        try {
            if (filter.filter(exchange))
                return true;
        } catch (Exception e) {
        }

        return false;
    }
}
