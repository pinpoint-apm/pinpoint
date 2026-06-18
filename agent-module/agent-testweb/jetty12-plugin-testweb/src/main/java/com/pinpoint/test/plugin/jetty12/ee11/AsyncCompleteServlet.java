package com.pinpoint.test.plugin.jetty12.ee11;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Reproduces the Trino Gateway async path (Jersey + airlift AsyncResponseHandler):
 * the request is started async, work is handed off to an application-managed
 * worker thread, and on completion {@link AsyncContext#complete()} is called
 * directly from that thread instead of dispatching back to the container.
 * <p>
 * That completion re-enters {@code ServletChannel.handle()} with DispatcherType
 * still {@code REQUEST}, which used to defeat the async-dispatch guard and trigger
 * a duplicate "already Trace Object exist" trace creation. The entry point was
 * moved to {@code ServletHandler.handle} so this completion re-entry is no longer
 * intercepted.
 */
public class AsyncCompleteServlet extends HttpServlet {
    private static final String HEAVY_RESOURCE = "This is some heavy resource that will be served in an async way";

    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(4);

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final AsyncContext async = request.startAsync();
        EXECUTOR.submit(() -> {
            try {
                Thread.sleep(100);
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().println(HEAVY_RESOURCE);
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            } finally {
                async.complete();
            }
        });
    }
}
