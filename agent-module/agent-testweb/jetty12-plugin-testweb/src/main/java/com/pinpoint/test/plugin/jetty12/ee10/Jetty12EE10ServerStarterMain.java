package com.pinpoint.test.plugin.jetty12.ee10;

import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

public class Jetty12EE10ServerStarterMain {

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
        Jetty12EE10ServerStarterMain starter = new Jetty12EE10ServerStarterMain();
        try {
            starter.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
