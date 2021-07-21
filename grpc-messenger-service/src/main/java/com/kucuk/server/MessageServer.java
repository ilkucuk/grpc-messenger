package com.kucuk.server;

import io.grpc.Server;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyServerBuilder;
import io.netty.handler.ssl.ClientAuth;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.logging.Logger;

public class MessageServer {

    private static final Logger logger = Logger.getLogger(MessageServer.class.getName());

    private Server server;

    private void start(String certRoot) throws IOException {
        File certFile = Paths.get( certRoot, "kucuk.com.crt").toFile();
        File keyFile = Paths.get(certRoot, "kucuk.com.pem").toFile();
        File caFile = Paths.get(certRoot, "ca.crt").toFile();

        int port = 443;
        server = NettyServerBuilder.forPort(port)
                .addService(new MessageService())
                .sslContext(GrpcSslContexts.forServer(certFile, keyFile)
                        .trustManager(caFile)
                        .clientAuth(ClientAuth.NONE)
                        .build())
                .build()
                .start();
        logger.info("Server started, listening on " + port + " version: async jersey client, with deadline check");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // Use stderr here since the logger may have been reset by its JVM shutdown hook.
            logger.info("*** shutting down gRPC server since JVM is shutting down");
            MessageServer.this.stop();
            logger.info("*** server shut down");
        }));
    }

    private void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    /**
     * Main launches the server from the command line.
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        final MessageServer server = new MessageServer();
        server.start(args[0]);
        server.blockUntilShutdown();
    }
}
