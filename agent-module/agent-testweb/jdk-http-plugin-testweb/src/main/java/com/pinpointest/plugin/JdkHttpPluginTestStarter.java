package com.pinpointest.plugin;

import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class JdkHttpPluginTestStarter {

    public static void main(String[] args) {
        SpringApplication.run(JdkHttpPluginTestStarter.class, args);
    }

    @Bean
    public ServletWebServerFactory serverFactory(@Value("${server.http.port}") int httpPort) {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
        tomcat.addAdditionalTomcatConnectors(createHttpConnector(httpPort));
        tomcat.setDisableMBeanRegistry(false);
        return tomcat;
    }

    private Connector createHttpConnector(int httpPort) {
        Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
        connector.setPort(httpPort);
        return connector;
    }

}
