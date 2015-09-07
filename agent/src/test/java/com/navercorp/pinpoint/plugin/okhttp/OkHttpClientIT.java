package com.navercorp.pinpoint.plugin.okhttp;

import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by nbp on 2015-09-04.
 */
@RunWith(PinpointPluginTestSuite.class)
@Dependency({"com.squareup.okhttp:okhttp:[2.5.0]"})
public class OkHttpClientIT {
    @Test
    public void test() throws Exception {
        try {
            Request request = new Request.Builder().url("http://google.com").build();
            OkHttpClient client = new OkHttpClient();
            Response response = client.newCall(request).execute();

            PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
            verifier.printCache();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
