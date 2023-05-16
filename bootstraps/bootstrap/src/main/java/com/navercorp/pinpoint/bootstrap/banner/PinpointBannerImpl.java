package com.navercorp.pinpoint.bootstrap.banner;

import com.navercorp.pinpoint.bootstrap.BootLogger;
import com.navercorp.pinpoint.common.Version;
import com.navercorp.pinpoint.common.banner.PinpointBanner;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

public class PinpointBannerImpl extends PinpointBanner {
    private BootLogger logger;
    private Properties properties;

    public PinpointBannerImpl(List<String> keysToPrint, BootLogger logger) {
        this.pinpointBannerMode = Mode.CONSOLE;
        this.logger = Objects.requireNonNull(logger, "logger");
        this.keysToPrint = Objects.requireNonNull(keysToPrint, "keysToPrint");
    }

    public void setPinpointBannerProperty(Properties properties) {
        this.properties = Objects.requireNonNull(properties, "properties");
    }

    @Override
    public void setPinpointBannerMode(Mode mode) {
        this.pinpointBannerMode = mode;
    }

    @Override
    public void printBanner() {
        if ( properties == null ) {
            logger.warn("Property not ready for Pinpoint Banner");
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

    private void printBanner(PrintStream out) {
        for (String line : BANNER) {
            out.println(line);
        }
        out.println(format("Pinpoint Version", Version.VERSION));

        for (String key: keysToPrint) {
            String value = properties.getProperty(key);
            if ( value != null ) {
                out.println(format(key, value));
            }
        }

        out.println();
    }

    private void printBanner(BootLogger logger) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(outputStream);
        printBanner(ps);
        logger.info(outputStream.toString());
    }
}
