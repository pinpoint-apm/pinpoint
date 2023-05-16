package com.pinpoint.test.plugin;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

public class JettyServerStarterMain {

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

        ServletHandler servletHandler = new ServletHandler();
        server.setHandler(servletHandler);

        servletHandler.addServletWithMapping(BlockingServlet.class, "/status");
        servletHandler.addServletWithMapping(AsyncServlet.class, "/async");

        server.start();
    }

    void stop() throws Exception {
        if (server != null) {
            server.stop();
        }
    }

    public static void main(String[] args) {
        JettyServerStarterMain starter = new JettyServerStarterMain();
        try {
            starter.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
