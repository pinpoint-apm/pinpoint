package com.navercorp.pinpoint.web.config;

import org.springframework.beans.factory.annotation.Value;

public class UserConfigProperties {

    @Value("${config.openSource:true}")
    private boolean openSource;

    public boolean isOpenSource() {
        return this.openSource;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UserConfigProperties{");
        sb.append(", openSource=").append(openSource);
        sb.append('}');
        return sb.toString();
    }

}
