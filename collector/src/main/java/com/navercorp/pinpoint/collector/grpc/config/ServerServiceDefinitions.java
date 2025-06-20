package com.navercorp.pinpoint.collector.grpc.config;

import io.grpc.ServerServiceDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ServerServiceDefinitions {
    private final List<ServerServiceDefinition> serverServiceDefinitions;

    public static ServerServiceDefinitions of(ServerServiceDefinition... serverServiceDefinitions) {
        Objects.requireNonNull(serverServiceDefinitions, "serverServiceDefinitions");

        ServerServiceDefinitions definitions = new ServerServiceDefinitions();
        for (ServerServiceDefinition serverServiceDefinition : serverServiceDefinitions) {
            definitions.addServerServiceDefinition(serverServiceDefinition);
        }
        return definitions;
    }

    public ServerServiceDefinitions() {
        this.serverServiceDefinitions = new ArrayList<>();
    }

    public void addServerServiceDefinition(ServerServiceDefinition serverServiceDefinition) {
        Objects.requireNonNull(serverServiceDefinition, "serverServiceDefinition");
        this.serverServiceDefinitions.add(serverServiceDefinition);
    }

    public List<ServerServiceDefinition> getDefinitions() {
        return serverServiceDefinitions;
    }

    @Override
    public String toString() {
        return "ServerServiceDefinitions{"
                + serverServiceDefinitions +
               '}';
    }
}
