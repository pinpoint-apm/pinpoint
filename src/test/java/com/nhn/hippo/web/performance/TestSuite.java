package com.nhn.hippo.web.performance;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Test;

import java.io.IOException;

/**
 *
 */
public class TestSuite {
    @Test
    public void insertData() throws IOException, InterruptedException {


        for (int i = 0; i < 10000; i++) {
            int mod = i % 4;
            if (mod == 0) {
                HttpClient client = new DefaultHttpClient();
                HttpGet get1 = new HttpGet("http://localhost:8080/combination.hippo");
                HttpResponse execute = client.execute(get1);
            } else if (mod == 1) {
                HttpClient client = new DefaultHttpClient();
                HttpGet get2 = new HttpGet("http://localhost:8080/mysql.hippo");
                HttpResponse execute = client.execute(get2);
            } else if (mod == 2) {
                HttpClient client = new DefaultHttpClient();
                HttpGet get3 = new HttpGet("http://localhost:8080/donothing.hippo");
                HttpResponse execute = client.execute(get3);
            } else if (mod == 3) {
                HttpClient client = new DefaultHttpClient();
                HttpGet get4 = new HttpGet("http://localhost:8080/combination.hippo");
                HttpResponse execute = client.execute(get4);
            }
        }

    }
}
