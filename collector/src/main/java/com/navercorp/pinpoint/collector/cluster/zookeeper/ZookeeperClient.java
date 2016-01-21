package com.navercorp.pinpoint.collector.cluster.zookeeper;

import com.navercorp.pinpoint.collector.cluster.zookeeper.exception.PinpointZookeeperException;

import java.io.IOException;
import java.util.List;

/**
 * @Author Taejin Koo
 */
public interface ZookeeperClient {

    void connect() throws IOException;

    void reconnectWhenSessionExpired();

    void createPath(String path) throws PinpointZookeeperException, InterruptedException;

    void createPath(String path, boolean createEndNode) throws PinpointZookeeperException, InterruptedException;

    String createNode(String zNodePath, byte[] data) throws PinpointZookeeperException, InterruptedException;

    byte[] getData(String path) throws PinpointZookeeperException, InterruptedException;

    void setData(String path, byte[] data) throws PinpointZookeeperException, InterruptedException;

    void delete(String path) throws PinpointZookeeperException, InterruptedException;

    boolean exists(String path) throws PinpointZookeeperException, InterruptedException;

    boolean isConnected();

    List<String> getChildrenNode(String path, boolean watch) throws PinpointZookeeperException, InterruptedException;

    void close();

 }
