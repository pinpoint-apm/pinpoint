package com.navercorp.pinpoint.banner;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public class PinpointBanner implements Banner {
    private final Mode bannerMode;
    private final Collection<String> dumpKeys;
    private final Function<String, String> properties;
    private final Consumer<String> consoleWriter = System.out::println;
    private final Consumer<String> loggerWriter;

    public PinpointBanner(Mode bannerMode,
                          Collection<String> dumpKeys,
                          Function<String, String> properties,
                          Consumer<String> loggerWriter) {
        this.bannerMode = Objects.requireNonNull(bannerMode, "bannerMode");
        this.dumpKeys = Objects.requireNonNull(dumpKeys, "dumpKeys");
        this.properties = Objects.requireNonNull(properties, "properties");
        this.loggerWriter = Objects.requireNonNull(loggerWriter, "loggerWriter");
    }


    @Override
    public void printBanner() {
        switch (bannerMode) {
            case OFF:
                return;
            case LOG:
                loggerWriter.accept(buildBannerString());
                return;
            default:
                consoleWriter.accept(buildBannerString());
        }
    }

    private String buildBannerString() {
        StringBuilder banner = new StringBuilder(128);
        banner.append(BannerUtils.banner());
        banner.append(format("Pinpoint Version", BannerVersionTemplate.VERSION));
        banner.append(System.lineSeparator());

        for (String key : dumpKeys) {
            String value = properties.apply(key);
            if (value != null) {
                banner.append(format(key, value));
                banner.append(System.lineSeparator());
            }
        }
        return banner.toString();
    }

    protected String format(String key, String value) {
        return String.format(" :: %55s :: %35s", key, value);
    }

}