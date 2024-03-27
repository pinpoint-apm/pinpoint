package com.navercorp.pinpoint.common.server.banner;

import com.navercorp.pinpoint.common.Version;
import com.navercorp.pinpoint.common.banner.PinpointBanner;
import com.navercorp.pinpoint.common.server.util.ServerBootLogger;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.List;

public class PinpointSpringBanner extends PinpointBanner implements ApplicationListener<ApplicationStartedEvent> {

    private static final String CONFIG_SEPARATOR = ",";

    private final ServerBootLogger logger = ServerBootLogger.getLogger(PinpointSpringBanner.class);

    private Environment environment;

    public PinpointSpringBanner() {
        this.setPinpointBannerMode(Mode.CONSOLE);
    }

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        environment = event.getApplicationContext().getEnvironment();

        String bannerMode = environment.getProperty("pinpoint.banner.mode");
        String bannerConfigs = environment.getProperty("pinpoint.banner.configs");

        Mode mode;
        if (bannerMode == null) {
            mode = Mode.CONSOLE;
        } else {
            mode = PinpointBanner.Mode.valueOf(bannerMode.toUpperCase());
        }
        this.setPinpointBannerMode(mode);

        if (bannerConfigs == null) {
            this.setKeysToPrint(List.of());
        } else {
            this.setKeysToPrint(Arrays.asList(bannerConfigs.split(CONFIG_SEPARATOR)));
        }

        printBanner();
    }

    @Override
    public void printBanner() {
        if ( environment == null ) {
            logger.info("Environment not ready for banner.");
            return;
        }

        switch (this.getPinpointBannerMode()) {
            case OFF -> {
            }
            case LOG -> logger.info(buildBannerString());
            default -> System.out.println(buildBannerString());
        }
    }

    private String buildBannerString() {
        StringBuilder sb = new StringBuilder();

        for (String line : BANNER) {
            sb.append(line).append(System.lineSeparator());
        }
        sb.append(format("Pinpoint Version", Version.VERSION)).append(System.lineSeparator());

        for (String key: this.getKeysToPrint()) {
            String value = environment.getProperty(key);
            if ( value != null ) {
                sb.append(format(key, value)).append(System.lineSeparator());
            }
        }

        return sb.toString();
    }

}
