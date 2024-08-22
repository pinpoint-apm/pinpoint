package com.navercorp.pinpoint.common.hbase;

import org.apache.hadoop.hbase.client.CheckAndMutateResult;

public enum CasResult {
    INITIAL_UPDATE,
    CAS_NEW,
    CAS_OLD;

    public static CasResult casResult(CheckAndMutateResult casResult) {
        if (casResult.isSuccess()) {
            return CAS_NEW;
        } else {
            return CAS_OLD;
        }
    }
}
