package com.pinpoint.test.plugin;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;

import java.util.concurrent.TimeUnit;

/**
 * @author Woonduk Kang(emeroad)
 */
public class Vertx4VerticalMain {

    public static void main(String[] args) {
        VertxOptions options = new VertxOptions();
        options.setBlockedThreadCheckInterval(60 * 60 * 1000);
        options.setBlockedThreadCheckIntervalUnit(TimeUnit.MILLISECONDS);

        Vertx vertx = Vertx.vertx(options);
        vertx.deployVerticle(new Vertx4PluginTestStarter());
    }
}
