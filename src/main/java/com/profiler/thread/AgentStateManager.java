package com.profiler.thread;

import com.profiler.sender.AgentInfoSender;

public class AgentStateManager {
    public static void startJVMTraceThread() {
        CurrentJVMStateCheckThread tracer = new CurrentJVMStateCheckThread();
        tracer.setDaemon(true);
        tracer.start();
    }
    public static void sendJVMStoppedInfo() throws Exception {
        AgentInfoSender sender = new AgentInfoSender(false);
        sender.start();
    }
}
