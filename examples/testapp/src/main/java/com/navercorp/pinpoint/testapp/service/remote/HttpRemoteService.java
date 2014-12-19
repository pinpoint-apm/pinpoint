package com.navercorp.pinpoint.testapp.service.remote;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author koo.taejin
 */
@Component
public class HttpRemoteService implements RemoteService {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public <R> R get(String url, Class<R> responseType) throws Exception {
        HttpUriRequest httpMethod = createGet(url, new LinkedMultiValueMap<String, String>());
        return execute(httpMethod, responseType);
    }

    @Override
    public <R> R get(String url, MultiValueMap<String, String> params, Class<R> responseType) throws Exception {
        HttpUriRequest httpMethod = createGet(url, params);
        return execute(httpMethod, responseType);
    }

    @Override
    public <R> R post(String url, Class<R> responseType) throws Exception {
        HttpUriRequest httpMethod = createPost(url, new LinkedMultiValueMap<String, String>());
        return execute(httpMethod, responseType);
    }

    @Override
    public <R> R post(String url, MultiValueMap<String, String> params, Class<R> responseType) throws Exception {
        HttpUriRequest httpMethod = createPost(url, params);
        return execute(httpMethod, responseType);
    }

    private HttpGet createGet(String url, MultiValueMap<String, String> params) throws URISyntaxException {
        URIBuilder uri = new URIBuilder(url);

        for (Map.Entry<String, List<String>> entry : params.entrySet()) {
            String key = entry.getKey();

            for (String value : entry.getValue()) {
                uri.addParameter(key, value);
            }
        }

        return new HttpGet(uri.build());
    }

    private HttpPost createPost(String url, MultiValueMap<String, String> params) throws UnsupportedEncodingException {
        HttpPost post = new HttpPost(url);

        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        for (Map.Entry<String, List<String>> entry : params.entrySet()) {
            String key = entry.getKey();

            for (String value : entry.getValue()) {
                nvps.add(new BasicNameValuePair(key, value));
            }
        }

        post.setEntity(new UrlEncodedFormEntity(nvps));

        return post;
    }

    private <R> R execute(HttpUriRequest httpMethod, Class<R> responseType) throws Exception {
        CloseableHttpClient httpclient = HttpClients.createDefault();

        CloseableHttpResponse response = httpclient.execute(httpMethod);
        HttpEntity entity = response.getEntity();

        return objectMapper.readValue(entity.getContent(), responseType);
    }

}
