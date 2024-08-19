package com.navercorp.pinpoint.web.dao;

public class AgentInfoQuery {
    private final boolean basic;
    private final boolean serverMetaData;
    private final boolean jvm;

    public AgentInfoQuery(boolean basic, boolean serverMetaData, boolean jvm) {
        this.basic = basic;
        this.serverMetaData = serverMetaData;
        this.jvm = jvm;
    }

    public static AgentInfoQuery all() {
        return new AgentInfoQuery(true, true, true);
    }

    public static AgentInfoQuery simple() {
        return new AgentInfoQuery(true, false, false);
    }

    public static AgentInfoQuery jvm() {
        return new AgentInfoQuery(true, false, true);
    }

    public boolean hasBasic() {
        return basic;
    }

    public boolean hasServerMetaData() {
        return serverMetaData;
    }

    public boolean hasJvm() {
        return jvm;
    }

    @Override
    public String toString() {
        return "AgentInfoQuery{" +
                "basic=" + basic +
                ", serverMetaData=" + serverMetaData +
                ", jvm=" + jvm +
                '}';
    }
}
