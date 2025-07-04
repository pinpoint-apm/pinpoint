package com.navercorp.pinpoint.common.hbase.wd;

/**
 * Copy from sematext/HBaseWD
 * Defines interface for storing object parameters in a String object to later restore the object by applying them to
 * new object instance
 *
 * @author Alex Baranau
 */
public interface Parametrizable {
    String getParamsToStore();
    void init(String storedParams);
}
