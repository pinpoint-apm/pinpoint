/*
 * Copyright 2024 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.web.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author intr3p1d
 */
@RestController
@RequestMapping("/api/errorTest")
@Validated
public class ErrorMockController {

    @GetMapping(value = "/basicJsonError")
    public void basicJsonError(HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(400);
        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "basicJsonError");

        throw new IOException("basic Json Error");
    }

    @GetMapping(value = "/400requestHeader")
    public void htmlErrorPage(HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

        PrintWriter writer = response.getWriter();
        writer.write("<html lang=\"en\"><head><title>HTTP Status 400 – Bad Request</title><style type=\"text/css\">body {font-family:Tahoma,Arial,sans-serif;} h1, h2, h3, b {color:white;background-color:#525D76;} h1 {font-size:22px;} h2 {font-size:16px;} h3 {font-size:14px;} p {font-size:12px;} a {color:black;} .line {height:1px;background-color:#525D76;border:none;}</style><style data-emotion=\"css\" data-s=\"\"></style></head><body><h1>HTTP Status 400 – Bad Request</h1><hr class=\"line\"><p><b>Type</b> Exception Report</p><p><b>Message</b> Request header is too large</p><p><b>Description</b> The server cannot or will not process the request due to something that is perceived to be a client error (e.g., malformed request syntax, invalid request message framing, or deceptive request routing).</p><p><b>Exception</b></p><pre>java.lang.IllegalArgumentException: Request header is too large\n" +
                "\torg.apache.coyote.http11.Http11InputBuffer.fill(Http11InputBuffer.java:770)\n" +
                "\torg.apache.coyote.http11.Http11InputBuffer.parseRequestLine(Http11InputBuffer.java:442)\n" +
                "\torg.apache.coyote.http11.Http11Processor.service(Http11Processor.java:264)\n" +
                "\torg.apache.coyote.AbstractProcessorLight.process(AbstractProcessorLight.java:63)\n" +
                "\torg.apache.coyote.AbstractProtocol$ConnectionHandler.process(AbstractProtocol.java:896)\n" +
                "\torg.apache.tomcat.util.net.NioEndpoint$SocketProcessor.doRun(NioEndpoint.java:1744)\n" +
                "\torg.apache.tomcat.util.net.SocketProcessorBase.run(SocketProcessorBase.java:52)\n" +
                "\torg.apache.tomcat.util.threads.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1191)\n" +
                "\torg.apache.tomcat.util.threads.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:659)\n" +
                "\torg.apache.tomcat.util.threads.TaskThread$WrappingRunnable.run(TaskThread.java:63)\n" +
                "\tjava.base/java.lang.Thread.run(Thread.java:833)\n" +
                "</pre><p><b>Note</b> The full stack trace of the root cause is available in the server logs.</p><hr class=\"line\"><h3>Apache Tomcat/10.1.19</h3><div id=\"__endic_crx__\"><div class=\"css-diqpy0\"></div></div></body></html>");
        writer.flush();
    }

    @GetMapping(value = "/unterminatedJson")
    public void unterminatedJson(HttpServletResponse response) throws IOException {
        response.setContentType("text/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        PrintWriter writer = response.getWriter();
        writer.write("[\n" +
                "  {\n" +
                "    \"_id\": \"66826f1f4b11d4c2bd7e9ab0\",\n" +
                "    \"index\": 0,\n" +
                "    \"guid\": \"cfedcfa2-2706-43ed-83b7-9c9dfd4a8e46\",\n" +
                "    \"isActive\": true,\n" +
                "    \"balance\": \"$2,472.51\",\n" +
                "    \"picture\": \"http://placehold.it/32x32\",\n" +
                "    \"address\": \"826 Clifford Place, Nelson, Arkansas, 3533\",\n" +
                "    \"about\": \"Culpa consequat duis elit");

        writer.flush();
    }


}
