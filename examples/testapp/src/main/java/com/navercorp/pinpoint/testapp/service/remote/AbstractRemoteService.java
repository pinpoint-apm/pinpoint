package com.navercorp.pinpoint.testapp.service.remote;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Map;

import org.apache.commons.codec.CharEncoding;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.web.client.RestClientException;

public abstract class AbstractRemoteService implements RemoteService {

    @Override
    public final Map<String, Object> getGeoCode(String address) {
        try {
            String encodedAddress = URLEncoder.encode(address, CharEncoding.UTF_8);
            URI uri = new URIBuilder()
                    .setScheme("http")
                    .setHost("maps.googleapis.com")
                    .setPath("/maps/api/geocode/json")
                    .setParameter("address", encodedAddress)
                    .setParameter("sensor", "false")
                    .build();
            @SuppressWarnings("unchecked")
            Map<String, Object> response = this.getForObject(uri, Map.class);
            return response;
        } catch (UnsupportedEncodingException e) {
            throw new RestClientException(e.getMessage());
        } catch (URISyntaxException e) {
            throw new RestClientException(e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getTwitterUrlCount(String url) {
        try {
            URI uri = new URIBuilder()
                    .setScheme("http")
                    .setHost("urls.api.twitter.com")
                    .setPath("/1/urls/count.json")
                    .setParameter("url", url)
                    .build();
            @SuppressWarnings("unchecked")
            Map<String, Object> response = this.getForObject(uri, Map.class);
            return response;
        } catch (URISyntaxException e) {
            throw new RestClientException(e.getMessage());
        }
    }
    
    protected abstract <T> T getForObject(URI url, Class<T> responseType);

}
