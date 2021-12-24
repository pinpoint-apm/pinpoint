package com.navercorp.pinpoint.bootstrap.plugin.request;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;

import java.util.Objects;

public class DefaultApplicationInfoSender<REQ> implements ApplicationInfoSender<REQ> {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    private final ClientHeaderAdaptor<REQ> clientHeaderAdaptor;

    private final boolean enabled;
    private final String headerName;
    private final String applicationName;

    public DefaultApplicationInfoSender(ClientHeaderAdaptor<REQ> clientHeaderAdaptor, TraceContext traceContext) {
        this.clientHeaderAdaptor = Objects.requireNonNull(clientHeaderAdaptor, "clientHeaderAdaptor");
        this.applicationName = traceContext.getApplicationName();
        final ProfilerConfig config = traceContext.getProfilerConfig();
        this.enabled = config.readBoolean("profiler.sendAppName.enable", false);
        this.headerName = config.readString("profiler.sendAppName.headerName", "X-Caller-Application-Name");
    }

    @Override
    public void sendCallerApplicationName(REQ request) {
        if (!enabled || request == null) {
            return;
        }

        try {
            clientHeaderAdaptor.setHeader(request, headerName, applicationName);
        } catch (Exception e) {
            logger.info("Add caller application name header failed, caused by: ", e);
        }
    }

}
