package com.kucuk.server;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;

public class BlockingServiceClientFactory {

    static Client client = ClientBuilder.newClient();
    static WebTarget target = client.target("https://kucuk2.com").path("message");

    public static WebTarget getTarget(Integer blockMilliseconds) {
        return client
                .target("https://kucuk2.com")
                .path("block")
                .path(String.valueOf(blockMilliseconds));
    }
}
