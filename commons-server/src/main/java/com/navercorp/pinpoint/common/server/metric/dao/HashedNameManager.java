package com.navercorp.pinpoint.common.server.metric.dao;

import org.apache.kafka.common.utils.Utils;

import java.nio.charset.StandardCharsets;

public class HashedNameManager implements NameManager {

    private final String prefix;
    private final String numberFormat;
    private final int count;


    public HashedNameManager(String prefix, int paddingLength, int count) {
        this.prefix = prefix;
        this.numberFormat = "%0" + paddingLength + "d";
        this.count = count;
    }

    protected int getHashValue(String applicationName) {
        int hash = Utils.murmur2(applicationName.getBytes(StandardCharsets.UTF_8));
        return Utils.toPositive(hash) % count;
    }

    public String getName(String applicationName) {
        int hashValue = getHashValue(applicationName);
        return prefix + String.format(numberFormat, hashValue);
    }


}
