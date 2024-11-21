package com.navercorp.pinpoint.banner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

class BannerUtils {

    static String readAllString(InputStream inputStream, Charset charset) throws IOException {
        try (Reader inputStreamReader = new InputStreamReader(inputStream, charset);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
            StringBuilder buffer = new StringBuilder(64);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                buffer.append(line);
                buffer.append(System.lineSeparator());
            }
            return buffer.toString();
        }
    }

    static String banner() {
        return banner("/pinpoint-banner/banner.txt");
    }

    private static String banner(String bannerFile) {
        try (InputStream inputStream = BannerUtils.class.getResourceAsStream(bannerFile)) {
            return BannerUtils.readAllString(inputStream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("banner IO failed", e);
        }
    }
}
