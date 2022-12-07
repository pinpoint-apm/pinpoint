package com.pinpoint.test.plugin;

import io.vertx.core.Vertx;

/**
 * @author Woonduk Kang(emeroad)
 */
public class Vertx4VerticalMain {

    public static void main(String[] args) {
//        Launcher launcher = new Launcher(args);
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new Vertx4PluginTestStarter());
    }
}
