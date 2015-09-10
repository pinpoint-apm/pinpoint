package com.navercorp.pinpoint.plugin.okhttp;

import com.navercorp.pinpoint.bootstrap.plugin.test.Expectations;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;
import com.squareup.okhttp.*;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by nbp on 2015-09-04.
 */
@RunWith(PinpointPluginTestSuite.class)
@Dependency({"com.squareup.okhttp:okhttp:[2.5.0]"})
public class OkHttpClientIT {

    @Test
    public void execute() throws Exception {
        Request request = new Request.Builder().url("http://google.com").build();
        OkHttpClient client = new OkHttpClient();
        Response response = client.newCall(request).execute();

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();

        Method callMethod = Call.class.getDeclaredMethod("execute");
        verifier.verifyTrace(Expectations.event("OK_HTTP_CLIENT_INTERNAL", callMethod));
    }

    @Test
    public void enqueue() throws Exception {
        Request request = new Request.Builder().url("http://google.com").build();
        OkHttpClient client = new OkHttpClient();
        final CountDownLatch latch = new CountDownLatch(1);
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                latch.countDown();
            }

            @Override
            public void onResponse(Response response) throws IOException {
                latch.countDown();
            }
        });
        latch.await(3, TimeUnit.SECONDS);

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();


        Method enqueueMethod = Call.class.getDeclaredMethod("enqueue", com.squareup.okhttp.Callback.class);
        verifier.verifyTrace(Expectations.event("OK_HTTP_CLIENT_INTERNAL", enqueueMethod));
    }

}
