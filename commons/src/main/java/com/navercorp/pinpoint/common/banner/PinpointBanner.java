package com.navercorp.pinpoint.common.banner;

import java.util.List;

public abstract class PinpointBanner {
    protected Mode pinpointBannerMode;
    protected List<String> keysToPrint;

    protected final String BANNER[] = {
            "",
            "        88888888ba   88  888b      88  88888888ba     ,ad8888ba,    88  888b      88  888888888888",
            "        88      ,8P  88  88 `8b    88  88      ,8P  d8'        `8b  88  88 `8b    88       88",
            "        88aaaaaa8P'  88  88  `8b   88  88aaaaaa8P'  88    da    88  88  88  `8b   88       88",
            "        88           88  88    `8b 88  88           Y8,        ,8P  88  88    `8b 88       88",
            "        88           88  88      `888  88             `\"Y8888Y\"'    88  88      `888       88",
            "",
            "                               https://github.com/pinpoint-apm/pinpoint",
            "",
    };

    public abstract void printBanner();
    public abstract void setPinpointBannerMode(Mode mode);

    protected String format(String key, String value) {
        return String.format(" :: %55s :: %35s", key, value);
    }

    public enum Mode {
        OFF,
        CONSOLE,
        LOG;
    }
}