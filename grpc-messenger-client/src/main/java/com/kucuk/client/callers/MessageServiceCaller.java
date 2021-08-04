package com.kucuk.client.callers;

import io.grpc.ManagedChannel;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.channel.ChannelOption;

import javax.net.ssl.SSLException;
import java.io.Closeable;
import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public interface MessageServiceCaller extends Callable<CallResult> {
    String serverEndpoint = "kucuk.com";
    int serverPort = 443;
    String caPath = "../cert/ca.crt";

    @Override
    CallResult call() throws Exception;

    static ManagedChannel getChannel() throws SSLException {
        return NettyChannelBuilder.forAddress(serverEndpoint, serverPort)
                .useTransportSecurity()
                .sslContext(GrpcSslContexts.forClient().trustManager(new File(caPath)).build())
                .withOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) TimeUnit.SECONDS.toMillis(10))
                .keepAliveWithoutCalls(true)
                .keepAliveTime(120, TimeUnit.SECONDS)
                .keepAliveTimeout(60, TimeUnit.SECONDS)
                .build();
    }
}
