package com.navercorp.pinpoint.common.server.banner;

import com.navercorp.pinpoint.banner.Banner;
import com.navercorp.pinpoint.banner.Mode;
import com.navercorp.pinpoint.banner.PinpointBanner;
import com.navercorp.pinpoint.common.server.util.ServerBootLogger;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;

import java.util.List;

public class PinpointSpringBanner implements ApplicationListener<ApplicationStartedEvent> {

    private static final String CONFIG_SEPARATOR = ",";

    private final ServerBootLogger logger = ServerBootLogger.getLogger(PinpointSpringBanner.class);

    public PinpointSpringBanner() {
    }

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        final Environment environment = event.getApplicationContext().getEnvironment();

        String bannerMode = environment.getProperty("pinpoint.banner.mode", Mode.LOG.toString());
        Mode mode = Mode.valueOf(bannerMode.toUpperCase());

        String dumpKeys = environment.getProperty("pinpoint.banner.configs", "");
        List<String> config = List.of(dumpKeys.split(CONFIG_SEPARATOR));

        Banner banner = new PinpointBanner(mode, config, environment::getProperty, logger::info);
        banner.printBanner();
    }

}
