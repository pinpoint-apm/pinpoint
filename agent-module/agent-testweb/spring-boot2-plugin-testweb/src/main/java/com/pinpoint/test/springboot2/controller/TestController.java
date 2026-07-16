package com.pinpoint.test.springboot2.controller;

import com.pinpoint.test.common.view.ApiLinkPage;
import com.pinpoint.test.common.view.HrefTag;
import com.pinpoint.test.springboot2.service.TestService;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.GZIPOutputStream;

@RestController
public class TestController {

    private final TestService testService;
    private final ApplicationContext applicationContext;

    public TestController(TestService testService, ApplicationContext applicationContext) {
        this.testService = testService;
        this.applicationContext = applicationContext;
    }

    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public String listMappings() {
        List<HrefTag> tags = new ArrayList<>();

        Map<String, RequestMappingHandlerMapping> rmhmBeans =
                applicationContext.getBeansOfType(RequestMappingHandlerMapping.class);
        for (RequestMappingHandlerMapping rmhm : rmhmBeans.values()) {
            for (RequestMappingInfo info : rmhm.getHandlerMethods().keySet()) {
                Set<String> patterns = (info.getPatternsCondition() != null)
                        ? info.getPatternsCondition().getPatterns()
                        : info.getPatternValues();
                for (String pattern : patterns) {
                    tags.add(HrefTag.of(pattern, toClickableUrl(pattern)));
                }
            }
        }

        tags.sort(Comparator.comparing(HrefTag::getText));
        return new ApiLinkPage("spring-boot2-plugin-testweb")
                .addHrefTag(tags)
                .build();
    }

    private static String toClickableUrl(String pattern) {
        return pattern
                .replaceAll("\\{[^/]+}", "sample")
                .replace("/**", "/sample")
                .replace("/*", "/sample");
    }

    @GetMapping(value = "/helloworld")
    public String helloworld() {
        return "helloworld";
    }

    @GetMapping(value = "/users/{id}")
    public String getUser(@PathVariable String id) {
        return "user-" + id;
    }

    @GetMapping(value = "/async")
    public CompletableFuture<String> async() {
        return testService.getHelloAsync();
    }

    @GetMapping(value = "/async/future")
    public String asyncFuture() throws InterruptedException, ExecutionException, TimeoutException {
        Future<String> future = testService.getHelloFuture();
        return future.get(5, TimeUnit.SECONDS);
    }

    @GetMapping(value = "/async/void")
    public String asyncVoid() {
        testService.fireAndForget();
        return "fired";
    }

    @GetMapping(value = "/async/executor/completable")
    public String executorSubmitCompletable() throws InterruptedException, ExecutionException, TimeoutException {
        return testService.submitCompletableDirect().get(5, TimeUnit.SECONDS);
    }

    @GetMapping(value = "/async/executor/submit")
    public String executorSubmit() throws InterruptedException, ExecutionException, TimeoutException {
        return testService.submitDirect().get(5, TimeUnit.SECONDS);
    }

    @GetMapping(value = "/sleep")
    public String sleep() throws InterruptedException {
        Thread.sleep(2000);
        return "sleep 2000ms";
    }

    /**
     * Bootstrap classloader instrumentation check.
     * Loads java.util.zip.CheckedInputStream lazily so the agent can transform it
     * when configured with profiler.include + profiler.instrument.jdk.allow.classnames.
     */
    @GetMapping(value = "/bootstrap/zip")
    public String bootstrapZip() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(bos);
        gzip.write("pinpoint".getBytes());
        gzip.close();
        CheckedInputStream checked = new CheckedInputStream(new ByteArrayInputStream(bos.toByteArray()), new CRC32());
        int total = 0;
        while (checked.read() != -1) {
            total++;
        }
        return "gzip bytes=" + bos.size() + " checked=" + total + " crc=" + checked.getChecksum().getValue();
    }
}
