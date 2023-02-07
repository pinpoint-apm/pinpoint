package com.pinpoint.test.plugin;

import io.vertx.core.Vertx;

/**
 * @author Woonduk Kang(emeroad)
 */
public class Vertx3VerticalMain {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new Vertx3PluginTestStarter());
    }
}
