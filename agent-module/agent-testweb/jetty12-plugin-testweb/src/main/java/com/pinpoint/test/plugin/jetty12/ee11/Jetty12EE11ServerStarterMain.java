package com.pinpoint.test.plugin.jetty12.ee11;

import org.eclipse.jetty.ee11.servlet.ServletContextHandler;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

public class Jetty12EE11ServerStarterMain {

    private Server server;

    void start() throws Exception {

        int maxThreads = 10;
        int minThreads = 3;
        int idleTimeout = 30;

        QueuedThreadPool threadPool = new QueuedThreadPool(maxThreads, minThreads, idleTimeout);

        server = new Server(threadPool);
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(18080);
        server.setConnectors(new Connector[]{connector});

        ServletContextHandler contextHandler = new ServletContextHandler();
        contextHandler.setContextPath("/");
        contextHandler.addServlet(BlockingServlet.class, "/status");
        contextHandler.addServlet(AsyncServlet.class, "/async");
        server.setHandler(contextHandler);

        server.start();
    }

    void stop() throws Exception {
        if (server != null) {
            server.stop();
        }
    }

    public static void main(String[] args) {
        Jetty12EE11ServerStarterMain starter = new Jetty12EE11ServerStarterMain();
        try {
            starter.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
