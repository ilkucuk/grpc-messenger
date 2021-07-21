package com.kucuk.client;

import com.kucuk.message.CreateMessageRequest;
import com.kucuk.message.CreateMessageResponse;
import com.kucuk.message.MessageServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.channel.ChannelOption;
import lombok.Builder;
import lombok.Getter;

import javax.net.ssl.SSLException;
import java.io.Closeable;
import java.io.File;
import java.time.Instant;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class MessageServiceCaller implements Callable<MessageServiceCaller.CallResult>, Closeable {

    private final int loopCount;
    private final String author;
    private final String title;
    private final String content;
    private final int sleepPeriod;
    private final ManagedChannel channel;
    private final MessageServiceGrpc.MessageServiceBlockingStub clientStub;

    private long requestId;
    private boolean sampleBoolean = false;
    private double sampleDouble = 1.0d;
    private int sampleInteger = 0;

    @Getter
    private CallResult callResult;

    public MessageServiceCaller(int loopCount, String host, int port, long requestId, String author, String title, String content, int sleepPeriod, File ca) throws SSLException {
        this.loopCount = loopCount;
        this.requestId = requestId;
        this.author = author;
        this.title = title;
        this.content = content;
        this.sleepPeriod = sleepPeriod;

        channel = NettyChannelBuilder.forAddress(host, port)
                .useTransportSecurity()
                .sslContext(GrpcSslContexts.forClient().trustManager(ca).build())
                .withOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) TimeUnit.SECONDS.toMillis(10))
                .keepAliveWithoutCalls(true)
                .keepAliveTime(120, TimeUnit.SECONDS)
                .keepAliveTimeout(60, TimeUnit.SECONDS)
                .build();

        clientStub = MessageServiceGrpc.newBlockingStub(channel);
    }


    @Override
    public CallResult call() {

        int success = 0;
        int failure = 0;

        long start = Instant.now().toEpochMilli();
        for(int i=0; i<loopCount; i++) {
            CreateMessageRequest request = createRequest();
            CreateMessageResponse response = clientStub.createMessage(request);

            if (processResponse(response)) {
                success++;
            } else {
                failure++;
            }
        }
        long duration = Instant.now().toEpochMilli() - start;

        callResult = CallResult.builder()
                .successCount(success)
                .failureCount(failure)
                .duration(duration)
                .build();
        return callResult;
    }

    private boolean processResponse(CreateMessageResponse response) {
        return response.getResponseId() > 0;
    }

    private CreateMessageRequest createRequest() {
        requestId++;
        sampleBoolean = !sampleBoolean;
        sampleDouble += 0.001;
        sampleInteger++;

        return CreateMessageRequest.newBuilder()
                .setRequestId(requestId)
                .setAuthor(author)
                .setTitle(title)
                .setContent(content)
                .setTime(Instant.now().toEpochMilli())
                .setSleepPeriod(sleepPeriod)
                .setSampleBooleanField(sampleBoolean)
                .setSampleDoubleField(sampleDouble)
                .setSampleIntegerField(sampleInteger)
                .build();
    }

    @Override
    public void close() {
        channel.shutdown();
    }

    @Builder
    @Getter
    static class CallResult {
        private final int successCount;
        private final int failureCount;
        private final long duration;
    }
}
