package com.navercorp.pinpoint.tools.network;

import java.io.IOException;

/**
 * @author Taejin Koo
 */
public interface NetworkChecker {

    void check() throws IOException;

    void check(byte[] requestData, byte[] expectedResponseData) throws IOException;

}
