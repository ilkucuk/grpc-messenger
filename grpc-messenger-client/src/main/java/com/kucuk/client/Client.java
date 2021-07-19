package com.kucuk.client;

import com.kucuk.message.CreateMessageRequest;
import com.kucuk.message.CreateMessageResponse;
import com.kucuk.message.MessageServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.channel.ChannelOption;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;

import javax.net.ssl.SSLException;
import java.io.File;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

public class Client {

    public static void main(String[] args) throws SSLException {

        File ca = new File("../cert/ca.crt");
        if (!ca.exists()) {
            System.out.println("Cert chain error!");
            return;
        }

         ManagedChannel channel = NettyChannelBuilder.forAddress("kucuk.com", 443)
                .useTransportSecurity()
                .sslContext(GrpcSslContexts.forClient().trustManager(ca).build())
                 .withOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) TimeUnit.SECONDS.toMillis(1))
                 .build();

        MessageServiceGrpc.MessageServiceBlockingStub clientStub = MessageServiceGrpc.newBlockingStub(channel);

        CreateMessageRequest request = CreateMessageRequest.newBuilder()
                .setRequestId(Instant.now().toEpochMilli())
                .setAuthor("ikucuk@gmail.com")
                .setTitle("Message Title For Medium Message")
                .setContent("Message Content")
                .setTime(Instant.now().toEpochMilli())
                .setSleepPeriod(100)
                .setSampleBooleanField(true)
                .setSampleDoubleField(3.14d)
                .setSampleIntegerField(12345)
                .build();

        CreateMessageResponse response = clientStub.createMessage(request);
        channel.shutdown();

        Arguments arguments = new Arguments();
        arguments.addArgument("host", "kucuk.com");
        //arguments.addArgument("host", "127.0.0.1");
        arguments.addArgument("port", "443");
        arguments.addArgument("ca", "../cert/ca.crt");
        arguments.addArgument("requestId", "1000");
        arguments.addArgument("author", "ikucuk@gmail.com");
        arguments.addArgument("title", "Message Title For Medium Message");
        arguments.addArgument("content", "Message Content");
        arguments.addArgument("sleepPeriod", "2000");
        JavaSamplerContext context = new JavaSamplerContext(arguments);
        GrpcMessengerServiceSampler sampler = new GrpcMessengerServiceSampler();
        sampler.setupTest(context);
        sampler.runTest(context);
    }
}
