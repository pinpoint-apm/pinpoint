package com.navercorp.pinpoint.it.plugin.utils;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.PullImageResultCallback;
import org.testcontainers.DockerClientFactory;

public class PullImage {

    public static void pullWithPlatform(String image, String platform) {
        DockerClient client = DockerClientFactory.instance().client();

        String[] parts = image.split(":");
        String repo = parts[0];
        String tag = parts.length > 1 ? parts[1] : "latest";

        try {
            client.pullImageCmd(repo)
                    .withTag(tag)
                    .withPlatform(platform)  // ← 핵심
                    .exec(new PullImageResultCallback())
                    .awaitCompletion();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Image pull interrupted", e);
        }
    }
}
