package com.kucuk.server;

import com.kucuk.server.config.ConfigReader;
import com.kucuk.server.config.MessageServiceConfig;
import com.kucuk.server.service.BlockService;
import com.kucuk.server.service.MessageService;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.NettyServerBuilder;
import io.netty.channel.ChannelOption;
import io.netty.handler.ssl.ClientAuth;

import javax.net.ssl.SSLException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class MessageServer {

    private static final Logger logger = Logger.getLogger(MessageServer.class.getName());

    private Server server;
    private final ManagedChannel blockingServiceChannel;


    public MessageServer(MessageServiceConfig config) throws IOException {
        blockingServiceChannel = createChannelForBlockingService();

        final File certFile = Paths.get(config.getCertFilePath()).toFile();
        final File keyFile = Paths.get(config.getKeyFilePath()).toFile();
        final File caFile = Paths.get(config.getTrustStorePath()).toFile();

        server = NettyServerBuilder.forPort(config.getPort())
                .addService(new MessageService(blockingServiceChannel))
                .addService(new BlockService())
                .sslContext(GrpcSslContexts.forServer(certFile, keyFile)
                        .trustManager(caFile)
                        .clientAuth(ClientAuth.NONE)
                        .build())
                .build()
                .start();

    }

    public void start(MessageServiceConfig config) {
        logger.info("Server started, listening on " + config.getPort() + " version: async jersey client, with deadline check");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // Use stderr here since the logger may have been reset by its JVM shutdown hook.
            logger.info("*** shutting down gRPC server since JVM is shutting down");
            MessageServer.this.stop();
            logger.info("*** server shut down");
        }));
    }

    public void stop() {
        if (server != null) {
            blockingServiceChannel.shutdown();
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

    private ManagedChannel createChannelForBlockingService() throws SSLException {
        final String serverEndpoint = "kucuk2.com";
        final int serverPort = 444;
        final String caPath = "../cert/ca.crt";
        return NettyChannelBuilder.forAddress(serverEndpoint, serverPort)
                .useTransportSecurity()
                .sslContext(GrpcSslContexts.forClient().trustManager(new File(caPath)).build())
                .withOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) TimeUnit.SECONDS.toMillis(10))
                .keepAliveWithoutCalls(true)
                .keepAliveTime(120, TimeUnit.SECONDS)
                .keepAliveTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    /**
     * Main launches the server from the command line.
     */
    public static void main(String[] args) throws IOException, InterruptedException {

        final MessageServiceConfig messageServiceConfig = ConfigReader.readServiceConfig(Path.of(args[0]));
        if (messageServiceConfig == null) {
            System.out.println("Invalid Config File Path");
            return;
        }

        final MessageServer server = new MessageServer(messageServiceConfig);
        server.start(messageServiceConfig);
        server.blockUntilShutdown();
    }
}
