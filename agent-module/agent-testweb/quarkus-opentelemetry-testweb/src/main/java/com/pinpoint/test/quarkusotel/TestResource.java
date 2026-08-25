package com.pinpoint.test.quarkusotel;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.RestClientBuilder;

import java.net.URI;

/**
 * Quarkus app instrumented with {@code quarkus-opentelemetry} (OTel SDK embedded, stable HTTP semconv on the REST
 * server and REST client spans). Spans go to Pinpoint through {@code quarkus.otel.exporter.otlp.*}.
 * Do NOT attach the Pinpoint agent or the OpenTelemetry Java Agent to this app.
 */
@Path("/")
@Produces(MediaType.TEXT_PLAIN)
public class TestResource {

    @Path("/")
    public interface SelfClient {
        @GET
        @Path("/user/{id}")
        String user(@PathParam("id") String id);
    }

    private final SelfClient self = RestClientBuilder.newBuilder()
            .baseUri(URI.create("http://localhost:18084"))
            .build(SelfClient.class);

    @GET
    @Path("/helloworld")
    public String helloworld() {
        return "helloworld";
    }

    @GET
    @Path("/user/{id}")
    public String user(@PathParam("id") String id) {
        return "user " + id;
    }

    @GET
    @Path("/observed")
    public String observed() {
        return "observed " + observedHello();
    }

    @WithSpan("observed-hello")
    String observedHello() {
        return "hello";
    }

    @GET
    @Path("/remote")
    public String remote() {
        // self call: server span -> REST client span -> server span (context propagated via W3C traceparent)
        return "remote " + self.user("42");
    }

    @GET
    @Path("/throw")
    public String error() {
        throw new IllegalStateException("observed error");
    }
}
