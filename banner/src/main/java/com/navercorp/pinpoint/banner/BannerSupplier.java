package com.navercorp.pinpoint.banner;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class BannerSupplier implements Supplier<String> {
    private final String file;

    public BannerSupplier() {
        this("/pinpoint-banner/banner.txt");
    }

    public BannerSupplier(String file) {
        this.file = Objects.requireNonNull(file, "file");
    }

    public String get() {
        return banner(file);
    }

    private String banner(String bannerFile) {
        List<String> lines = readLines(bannerFile);
        return writeBanner(lines);
    }

    private List<String> readLines(String bannerFile) {
        try (InputStream inputStream = getClass().getResourceAsStream(bannerFile)) {
            return BannerIOUtils.readLines(inputStream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("banner IO failed", e);
        }
    }

    private String writeBanner(List<String> lines) {
        String lineSeparator = System.lineSeparator();

        StringBuilder buffer = new StringBuilder(64);
        for (String line : lines) {
            buffer.append(line);
            buffer.append(lineSeparator);
        }
        return buffer.toString();
    }
}
