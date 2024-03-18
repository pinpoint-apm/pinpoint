package com.navercorp.pinpoint.test.plugin.util;

import com.navercorp.pinpoint.test.plugin.PluginTestConstants;
import org.tinylog.Logger;
import org.tinylog.TaggedLogger;

public final class TestLogger {
    private TestLogger() {
    }

    public static TaggedLogger getLogger() {
        return Logger.tag(PluginTestConstants.TAG);
    }
}
