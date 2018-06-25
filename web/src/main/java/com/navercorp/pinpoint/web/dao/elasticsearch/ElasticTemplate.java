package com.navercorp.pinpoint.web.dao.elasticsearch;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.elasticsearch.xpack.client.PreBuiltXPackTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by yuanxiaozhong on 2018/3/26.
 */
public class ElasticTemplate{
    private static final Logger logger = LoggerFactory.getLogger(ElasticTemplate.class);

    /**
     * elasticsearch inner host
     */
    private String ipAddress;

    /**
     * elasticsearch cluster name
     */
    private String cluster;

    private Client client;

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getCluster() {
        return cluster;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }


    public Client getClient() {
        if (client == null){
            synchronized (this){
                if (client == null){
                    init();
                }
            }
        }
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public void init(){
        Settings settings = Settings.builder()
                .put("cluster.name", this.cluster)
                .put("client.transport.sniff", false)
                .build();

        try {
            this.client = new PreBuiltTransportClient(settings)
                    .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(this.ipAddress), 9300));
        } catch (UnknownHostException e) {
            logger.error("UnknowHost host=[{}], cluster=[{}]", ipAddress, cluster);
        }
    }

    public void destroy(){
        this.client.close();
    }
}