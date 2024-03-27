package com.navercorp.pinpoint.bootstrap.banner;

import com.navercorp.pinpoint.bootstrap.BootLogger;
import com.navercorp.pinpoint.common.Version;
import com.navercorp.pinpoint.common.banner.PinpointBanner;

import java.util.List;
import java.util.Objects;
import java.util.Properties;

public class PinpointBannerImpl extends PinpointBanner {
    private final BootLogger logger;
    private Properties properties;

    public PinpointBannerImpl(List<String> keysToPrint, BootLogger logger) {
        this.setPinpointBannerMode(Mode.CONSOLE);
        this.logger = Objects.requireNonNull(logger, "logger");
        this.setKeysToPrint(Objects.requireNonNull(keysToPrint, "keysToPrint"));
    }

    public void setPinpointBannerProperty(Properties properties) {
        this.properties = Objects.requireNonNull(properties, "properties");
    }

    @Override
    public void printBanner() {
        if ( properties == null ) {
            logger.warn("Property not ready for Pinpoint Banner");
            return;
        }

        switch (this.getPinpointBannerMode()) {
            case OFF:
                return;
            case LOG:
                logger.info(buildBannerString());
                return;
            default:
                System.out.println(buildBannerString());
                return;
        }
    }

    private String buildBannerString() {
        StringBuilder sb = new StringBuilder();
        for (String line : BANNER) {
            sb.append(line).append(System.lineSeparator());
        }
        sb.append(format("Pinpoint Version", Version.VERSION)).append(System.lineSeparator());

        for (String key: getKeysToPrint()) {
            String value = properties.getProperty(key);
            if ( value != null ) {
                sb.append(format(key, value)).append('\n');
            }
        }

        return sb.toString();
    }

}
