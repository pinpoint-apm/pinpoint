package com.navercorp.pinpoint.test.plugin.util;

public final class TLSOption {
    public static final String TLS_KEY = "https.protocols";

    private TLSOption() {
    }

    public static void applyTLSv12() {
        if (JDKUtils.isJdk8Plus()) {
            return;
        }

        System.setProperty(TLS_KEY, "SSLv3,TLSv1,TLSv1.1,TLSv1.2");
    }
}
