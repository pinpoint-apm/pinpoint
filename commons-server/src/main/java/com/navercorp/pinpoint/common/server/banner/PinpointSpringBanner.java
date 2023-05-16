package com.navercorp.pinpoint.common.server.banner;

import com.navercorp.pinpoint.common.Version;
import com.navercorp.pinpoint.common.banner.PinpointBanner;
import com.navercorp.pinpoint.common.server.util.ServerBootLogger;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;

public class PinpointSpringBanner extends PinpointBanner implements ApplicationListener<ApplicationStartedEvent> {
    private ServerBootLogger logger = ServerBootLogger.getLogger(PinpointSpringBanner.class);

    private Environment environment;

    public PinpointSpringBanner() {
        this.pinpointBannerMode = Mode.CONSOLE;
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
            this.keysToPrint = new ArrayList<>();
        } else {
            this.keysToPrint = Arrays.asList(bannerConfigs.split(","));
        }

        printBanner();
    }

    @Override
    public void printBanner() {
        if ( environment == null ) {
            logger.info("Environment not ready for banner.");
            return;
        }

        switch (this.pinpointBannerMode) {
            case OFF:
                return;
            case LOG:
                printBanner(logger);
                return;
            default:
                printBanner(System.out);
                return;
        }
    }

    private void printBanner(ServerBootLogger logger) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(outputStream);
        printBanner(ps);
        logger.info(outputStream.toString());
    }

    private void printBanner(PrintStream out) {
        for (String line : BANNER) {
            out.println(line);
        }
        out.println(format("Pinpoint Version", Version.VERSION));

        for (String key: this.keysToPrint) {
            String value = environment.getProperty(key);
            if ( value != null ) {
                out.println(format(key, value));
            }
        }

        out.println();
    }

    @Override
    public void setPinpointBannerMode(Mode mode) {
        this.pinpointBannerMode = mode;
    }
}
