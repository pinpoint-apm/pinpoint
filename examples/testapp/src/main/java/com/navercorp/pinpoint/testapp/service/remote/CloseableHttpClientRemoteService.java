package com.navercorp.pinpoint.testapp.service.remote;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public final class CloseableHttpClientRemoteService extends AbstractRemoteService {

    @Autowired
    @Qualifier("closeableHttpClientRestTemplate")
    private RestTemplate restTemplate;

    @Override
    protected <T> T getForObject(URI url, Class<T> responseType) {
        return this.restTemplate.getForObject(url, responseType);
    }
    
}
