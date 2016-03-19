package com.navercorp.pinpoint.plugin.jdk.exec;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

/**
 * @author hamlet-lee
 */
public class JdkExecConfig {
    private boolean profile = true;

    public JdkExecConfig(ProfilerConfig src) {
        this.profile = src.readBoolean("profiler.jdk.exec", true);
    }

    public boolean isProfile() {
        return profile;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("JdkExecConfig{");
        sb.append("profile=").append(profile);
        sb.append('}');
        return sb.toString();
    }
}
