package com.nhn.pinpoint.testweb.controller;

import com.nhncorp.lucy.net.invoker.InvocationFuture;
import com.nhncorp.lucy.net.invoker.InvocationFutureListener;
import com.nhncorp.lucy.npc.connector.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * @author netspider
 */
@Controller
public class NPCController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * using basic connector
     *
     * @return
     */
    @RequestMapping(value = "/npc/1")
    @ResponseBody
    public String npc() throws Exception {
        NpcHessianConnector connector = null;
        try {
            InetSocketAddress serverAddress = new InetSocketAddress("127.0.0.1", 5000);
            connector = new NpcHessianConnector(serverAddress, true);

            Map<String, Object> params = new HashMap<String, Object>();
            params.put("message", "hello pinpoint");

            InvocationFuture future = connector.invoke("welcome/com.nhncorp.lucy.bloc.welcome.EchoBO", "execute", params);

            future.await();

            // Object result = future.get();
            Object result = future.getReturnValue();
            logger.debug("npc result={}", result);
        } finally {
            if (connector != null) {
                connector.dispose();
            }
        }
        return "OK";
    }

    /**
     * using keepalive connector
     *
     * @return
     */
    @RequestMapping(value = "/npc/2")
    @ResponseBody
    public String npc2() throws Exception {
        KeepAliveNpcHessianConnector connector = null;
        try {
            InetSocketAddress serverAddress = new InetSocketAddress("127.0.0.1", 5000);

            connector = new KeepAliveNpcHessianConnector(serverAddress);

            Map<String, Object> params = new HashMap<String, Object>();
            params.put("message", "hello pinpoint");

            InvocationFuture future = connector.invoke("welcome/com.nhncorp.lucy.bloc.welcome.EchoBO", "execute", params);

            future.await();

            // Object result = future.get();
            Object result = future.getReturnValue();
            logger.debug("npc result={}", result);
        } finally {
            if (connector != null) {
                connector.dispose();
            }
        }
        return "OK";
    }

    /**
     * using connection factory
     *
     * @return
     */
    @RequestMapping(value = "/npc/3")
    @ResponseBody
    public String npc3() throws Exception {
        NpcHessianConnector connector = null;
        try {
            InetSocketAddress serverAddress = new InetSocketAddress("127.0.0.1", 5000);

            ConnectionFactory npcConnectionFactory = new NpcConnectionFactory();

            npcConnectionFactory.setTimeout(1000L);
            npcConnectionFactory.setAddress(serverAddress);

            connector = npcConnectionFactory.create();

            Map<String, Object> params = new HashMap<String, Object>();
            params.put("message", "hello pinpoint");

            InvocationFuture future = connector.invoke("welcome/com.nhncorp.lucy.bloc.welcome.EchoBO", "execute", params);

            future.await();

            // Object result = future.get();
            Object result = future.getReturnValue();
            logger.debug("npc result={}", result);
        } finally {
            if (connector != null) {
                connector.dispose();
            }
        }
        return "OK";
    }

    /**
     * using lightweight connector
     *
     * @return
     */
    @RequestMapping(value = "/npc/4")
    @ResponseBody
    public String npc4() throws Exception {
        NpcHessianConnector connector = null;
        try {
            InetSocketAddress serverAddress = new InetSocketAddress("127.0.0.1", 5000);

            ConnectionFactory npcConnectionFactory = new NpcConnectionFactory();

            npcConnectionFactory.setTimeout(1000L);
            npcConnectionFactory.setAddress(serverAddress);
            npcConnectionFactory.setLightWeight(true);

            connector = npcConnectionFactory.create();

            Map<String, Object> params = new HashMap<String, Object>();
            params.put("message", "hello pinpoint");

            InvocationFuture future = connector.invoke("welcome/com.nhncorp.lucy.bloc.welcome.EchoBO", "execute", params);

            future.await();

            // Object result = future.get();
            Object result = future.getReturnValue();
            logger.debug("npc result={}", result);
        } finally {
            if (connector != null) {
                connector.dispose();
            }
        }
        return "OK";
    }

    /**
     * using lightweight connector and listener
     *
     * @return
     */
    @RequestMapping(value = "/npc/5")
    @ResponseBody
    public String npc5() throws NpcCallException {
        NpcHessianConnector connector = null;
        try {
            InetSocketAddress serverAddress = new InetSocketAddress("127.0.0.1", 5000);

            ConnectionFactory npcConnectionFactory = new NpcConnectionFactory();

            npcConnectionFactory.setTimeout(1000L);
            npcConnectionFactory.setAddress(serverAddress);
            npcConnectionFactory.setLightWeight(true);

            connector = npcConnectionFactory.create();

            Map<String, Object> params = new HashMap<String, Object>();
            params.put("message", "hello pinpoint");

            InvocationFuture future = connector.invoke("welcome/com.nhncorp.lucy.bloc.welcome.EchoBO", "execute", params);

            future.addListener(new InvocationFutureListener() {
                @Override
                public void invocationComplete(InvocationFuture future) throws Exception {
                    Object result = future.getReturnValue();
                    logger.debug("npc result={}", result);
                }
            });
        } finally {
            if (connector != null) {
                connector.dispose();
            }
        }
        return "OK";
    }

    @RequestMapping(value = "/npc/6")
    @ResponseBody
    public String npcStream() {
        return "NOT_IMPLEMENTED";
    }
}
