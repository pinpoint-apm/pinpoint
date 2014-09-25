package com.nhn.pinpoint.testweb.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

import com.nhncorp.redis.cluster.gateway.GatewayClient;
import com.nhncorp.redis.cluster.pipeline.RedisClusterPipeline;
import com.nhncorp.redis.cluster.spring.StringRedisClusterTemplate;

@Controller
public class RedisController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String HOST = "10.99.116.91";
    private static final int PORT = 6390;

    public static final String IBATIS_VIEW = "orm/ibatis";
    public static final String MYBATIS_VIEW = "orm/mybatis";

    @Autowired
    private GatewayClient client;

    @Autowired
    private StringRedisClusterTemplate redisTemplate;

    @RequestMapping(value = "/redis/jedis")
    @ResponseBody
    public String jedis(Model model) {
        logger.info("/redis/jedis");

        final Jedis jedis = new Jedis(HOST, PORT);

        jedis.get("foo");
        jedis.close();

        return "OK";
    }

    @RequestMapping(value = "/redis/jedis/pipeline")
    @ResponseBody
    public String jedisPipeline(Model model) {
        logger.info("/redis/jedis/pipeline - add, update, get, delete");

        final Jedis jedis = new Jedis(HOST, PORT);
        Pipeline pipeline = jedis.pipelined();

        pipeline.set("foo", "bar");
        pipeline.get("foo");
        pipeline.expire("foo", 1);
        pipeline.syncAndReturnAll();

        jedis.close();

        return "OK";
    }

    @RequestMapping(value = "/redis/nBaseArc")
    @ResponseBody
    public String nBaseArc(Model model) {
        logger.info("/redis/nBaseArc");

        client.get("foo");

        return "OK";
    }

    @RequestMapping(value = "/redis/nBaseArc/pipeline")
    @ResponseBody
    public String nBaseArcPipeline(Model model) {
        logger.info("/redis/nBaseArc/pipeline");

        RedisClusterPipeline pipeline = null;
        try {
            pipeline = client.pipeline();
            pipeline.set("foo", "bar");
            pipeline.get("foo");
            pipeline.expire("foo", 1);
            pipeline.syncAndReturnAll();
        } finally {
            if (pipeline != null) {
                pipeline.close();
            }
        }

        return "OK";
    }
}