package com.navercorp.pinpoint.profiler.monitor.processor;

/**
 * @author dongdd
 * @description：
 */
public enum PropertiesKey {
    AGENT_ENABLE("profiler.agent.enable", "agent总开关"),

    SAMPLE_RATE("profiler.sampling.rate", "采样率"),
    SAMPLE_ENABLE("profiler.sampling.enable", "采样率开关"),

    AGENT_SENDER_IP("profiler.collector.ip", "agent数据发送到collect的ip"),
    AGENT_SENDER_PORT("profiler.collector.tcp.port", "agent数据发送到collect的port"),

    SPAN_SENDER_IP("profiler.collector.ip", "span数据发送到collect的ip"),
    SPAN_SENDER_PORT("profiler.collector.span.port", "span数据发送到collect的port"),

    STAT_SENDER_IP("profiler.collector.ip", "stat数据发送到collect的ip"),
    STAT_SENDER_PORT("profiler.collector.stat.port", "stat数据发送到collect的port"),

    STAT_BATCH_COUNR("profiler.jvm.stat.batch.send.count", "发送stat数据一批次的个数"),
    LOG_LEVEL_GLOBAL("profiler.log.global.level","全局日志级别"),
    LOG_LEVEL_PINPOINT("profiler.log.pinpoint.level","日志级别"),
    LOG_LEVEL_NACOS("profiler.log.nacos.level","nacos日志级别");
    public String key;
    public String desc;

    PropertiesKey(String key, String desc) {
        this.key = key;
        this.desc = desc;
    }
}
