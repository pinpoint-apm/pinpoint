package com.navercorp.pinpoint.banner;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class PinpointBanner implements Banner {
    private final Supplier<String> bannerSupplier;
    private final Collection<String> dumpKeys;
    private final Function<String, String> properties;
    private final Consumer<String> loggerWriter;

    PinpointBanner(Supplier<String> bannerSupplier,
                   Collection<String> dumpKeys,
                   Function<String, String> properties,
                   Consumer<String> loggerWriter) {
        this.bannerSupplier = Objects.requireNonNull(bannerSupplier, "bannerSupplier");
        this.dumpKeys = Objects.requireNonNull(dumpKeys, "dumpKeys");
        this.properties = Objects.requireNonNull(properties, "properties");
        this.loggerWriter = Objects.requireNonNull(loggerWriter, "loggerWriter");
    }


    @Override
    public void printBanner() {
        String banner = buildBanner();
        loggerWriter.accept(banner);
    }

    private String buildBanner() {
        StringBuilder banner = new StringBuilder(128);
        banner.append(bannerSupplier.get());
        banner.append(format("Pinpoint Version", BannerVersionTemplate.VERSION));
        banner.append(System.lineSeparator());

        for (String key : dumpKeys) {
            String value = properties.apply(key);
            banner.append(format(key, value));
            banner.append(System.lineSeparator());
        }
        return banner.toString();
    }

    protected String format(String key, String value) {
        return String.format(" :: %55s :: %35s", key, value);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        private Mode bannerMode = Mode.CONSOLE;
        private Supplier<String> bannerSupplier = new BannerSupplier();
        private Collection<String> dumpKeys;
        private Function<String, String> properties;
        private Consumer<String> loggerWriter;

        public void setBannerMode(Mode bannerMode) {
            this.bannerMode = bannerMode;
        }

        public void setBannerSupplier(Supplier<String> bannerSupplier) {
            this.bannerSupplier = bannerSupplier;
        }

        public void setDumpKeys(Collection<String> dumpKeys) {
            this.dumpKeys = dumpKeys;
        }

        public void setProperties(Function<String, String> properties) {
            this.properties = properties;
        }

        public void setLoggerWriter(Consumer<String> loggerWriter) {
            this.loggerWriter = loggerWriter;
        }

        public Banner build() {
            Consumer<String> logWriter = getLogWriter();
            return new PinpointBanner(bannerSupplier, dumpKeys, properties, logWriter);
        }

        private Consumer<String> getLogWriter() {
            switch (bannerMode) {
                case OFF:
                    return log -> {
                    };
                case LOG:
                    return System.out::println;
                default:
                    return Objects.requireNonNull(this.loggerWriter, "loggerWriter");
            }
        }

    }

}