/*
 * Copyright 2023 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pinpoint.test.plugin;

import com.pinpoint.test.common.view.ApiLinkPage;
import com.pinpoint.test.common.view.HrefTag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.reactive.result.method.RequestMappingInfo;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.async.SdkAsyncHttpClient;
import software.amazon.awssdk.http.crt.AwsCrtHttpClient;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.net.URI;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
public class AwsSdkS3PluginController {
    private static final String ACCESS_KEY = "test";
    private static final String SECRET_KEY = "test";
    private static final String REGION = "us-east-1";
    private static final String ENDPOINT = "http://127.0.0.1:32805";

    private final RequestMappingHandlerMapping handlerMapping;

    @Autowired
    public AwsSdkS3PluginController(RequestMappingHandlerMapping handlerMapping) {
        this.handlerMapping = handlerMapping;
    }

    @GetMapping("/")
    String welcome() {
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = this.handlerMapping.getHandlerMethods();
        List<HrefTag> list = new ArrayList<>();
        for (RequestMappingInfo info : handlerMethods.keySet()) {
            for (String path : info.getDirectPaths()) {
                list.add(HrefTag.of(path));
            }
        }
        list.sort(Comparator.comparing(HrefTag::getPath));
        return new ApiLinkPage("aws-sdk-s3-plugin-testweb")
                .addHrefTag(list)
                .build();
    }

    @GetMapping("/list")
    public void list() throws Exception {
        S3Client s3Client = getClient();

        String bucketName = "my-bucket";
        if (!bucketExists(s3Client, bucketName)) {
            s3Client.createBucket(b -> b.bucket(bucketName));
        }

        List<Bucket> allBuckets = new ArrayList<>();
        String nextToken = null;
        do {
            String continuationToken = nextToken;
            ListBucketsResponse listBucketsResponse = s3Client.listBuckets(b -> b.continuationToken(continuationToken));
            allBuckets.addAll(listBucketsResponse.buckets());
            nextToken = listBucketsResponse.continuationToken();
        } while (nextToken != null);
        System.out.println("All buckets: " + allBuckets);
    }

    @GetMapping("/list/crt")
    public void crtList() throws Exception {
        S3Client s3Client = getCrtClient();

        String bucketName = "my-bucket";
        if (!bucketExists(s3Client, bucketName)) {
            s3Client.createBucket(b -> b.bucket(bucketName));
        }

        List<Bucket> allBuckets = new ArrayList<>();
        String nextToken = null;
        do {
            String continuationToken = nextToken;
            ListBucketsResponse listBucketsResponse = s3Client.listBuckets(b -> b.continuationToken(continuationToken));
            allBuckets.addAll(listBucketsResponse.buckets());
            nextToken = listBucketsResponse.continuationToken();
        } while (nextToken != null);
        System.out.println("All buckets: " + allBuckets);
    }


    S3Client getClient() throws Exception {
        AwsCredentials credentials = AwsBasicCredentials.create(ACCESS_KEY, SECRET_KEY);
        return S3Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .endpointOverride(new URI(ENDPOINT))
                .region(Region.of(REGION))
                .build();
    }

    S3Client getCrtClient() throws Exception {
        AwsCredentials credentials = AwsBasicCredentials.create(ACCESS_KEY, SECRET_KEY);
        SdkHttpClient client = AwsCrtHttpClient.builder()
                .maxConcurrency(10)
                .connectionTimeout(java.time.Duration.ofSeconds(10))
                .build();

        return S3Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .endpointOverride(new URI(ENDPOINT))
                .region(Region.of(REGION))
                .httpClient(client)
                .build();
    }


    boolean bucketExists(S3Client s3Client, String bucketName) {
        try {
            s3Client.headBucket(b -> b.bucket(bucketName));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @GetMapping("/list/async")
    public void asyncList() throws Exception {
        S3AsyncClient client = getAsyncClient();

        CompletableFuture future = client.headBucket(b -> b.bucket("my-bucket"));
        future.whenComplete((result, throwable) -> {
            if (throwable != null) {
                client.createBucket(b -> b.bucket("my-bucket"));
            } else {
            }
        });

        client.listBuckets(b -> b.continuationToken(null))
                .thenAccept(listBucketsResponse -> {
                    List<Bucket> allBuckets = listBucketsResponse.buckets();
                    System.out.println("All buckets: " + allBuckets);
                });
    }

    @GetMapping("/list/crt/async")
    public void crtAsyncList() throws Exception {
        S3AsyncClient client = getCrtAsyncClient();

        CompletableFuture future = client.headBucket(b -> b.bucket("my-bucket"));
        future.whenComplete((result, throwable) -> {
            if (throwable != null) {
                client.createBucket(b -> b.bucket("my-bucket"));
            } else {
            }
        });

        client.listBuckets(b -> b.continuationToken(null))
                .thenAccept(listBucketsResponse -> {
                    List<Bucket> allBuckets = listBucketsResponse.buckets();
                    System.out.println("All buckets: " + allBuckets);
                });
    }

    @GetMapping("/putObject/async")
    public void asyncPutObject() throws Exception {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket("my-bucket")
                .key("my-key")
                .build();

        S3AsyncClient client = getAsyncClient();
        URI uri = this.getClass().getResource("/test.txt").toURI();
        CompletableFuture future = client.putObject(putObjectRequest, AsyncRequestBody.fromFile(Paths.get(uri)));
        future.whenComplete((result, throwable) -> {
            if (throwable != null) {
                System.out.println("Error: " + throwable.toString());
            } else {
                System.out.println("Success: " + result);
            }
        });
        future.join();
    }

    @GetMapping("/pubObject")
    public void putObject() throws Exception {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket("my-bucket")
                .key("my-key")
                .build();

        S3Client client = getClient();
        URI uri = this.getClass().getResource("/test.txt").toURI();
        PutObjectResponse response = client.putObject(putObjectRequest, RequestBody.fromFile(Paths.get(uri)));
        System.out.println(response.toString());
    }


    S3AsyncClient getAsyncClient() {
        AwsCredentials credentials = AwsBasicCredentials.create(ACCESS_KEY, SECRET_KEY);
        SdkAsyncHttpClient sdkAsyncHttpClient = NettyNioAsyncHttpClient.builder()
                .maxConcurrency(10)
                .connectionTimeout(java.time.Duration.ofSeconds(10))
                .build();


        return S3AsyncClient.builder()
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .endpointOverride(URI.create(ENDPOINT))
                .region(Region.of(REGION))
                .httpClient(sdkAsyncHttpClient)
                .build();
    }

    S3AsyncClient getCrtAsyncClient() {
        AwsCredentials credentials = AwsBasicCredentials.create(ACCESS_KEY, SECRET_KEY);
        return S3AsyncClient.crtBuilder()
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .endpointOverride(URI.create(ENDPOINT))
                .region(Region.of(REGION))
                .build();
    }
}