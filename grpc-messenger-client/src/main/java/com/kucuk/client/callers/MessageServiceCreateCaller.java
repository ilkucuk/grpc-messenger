package com.kucuk.client.callers;

import com.kucuk.client.MessageServiceTestHelper;
import com.kucuk.message.CreateMessageRequest;
import com.kucuk.message.CreateMessageResponse;
import com.kucuk.message.MessageServiceGrpc;
import io.grpc.ManagedChannel;

import java.time.Instant;

public class MessageServiceCreateCaller implements MessageServiceCaller {

    private final int loopCount;
    private final MessageServiceTestHelper testHelper;
    private final ManagedChannel channel;
    private final MessageServiceGrpc.MessageServiceBlockingStub clientStub;

    private long responseAccumulator = 0;

    public MessageServiceCreateCaller(int loopCount, MessageServiceTestHelper testHelper) throws Exception {
        this.loopCount = loopCount;
        this.testHelper = testHelper;

        channel = MessageServiceCaller.getChannel();
        clientStub = MessageServiceGrpc.newBlockingStub(channel);
    }


    @Override
    public CallResult call() {

        int success = 0;
        int failure = 0;

        long start = Instant.now().toEpochMilli();
        for (int i = 0; i < loopCount; i++) {
            CreateMessageRequest request = testHelper.newCreateMessageRequest();
            CreateMessageResponse response = clientStub.createMessage(request);

            if (processResponse(response)) {
                success++;
            } else {
                failure++;
            }
        }
        long duration = Instant.now().toEpochMilli() - start;
        channel.shutdown();

        return CallResult.builder()
                .successCount(success)
                .failureCount(failure)
                .duration(duration)
                .accumulator(responseAccumulator)
                .build();
    }

    private boolean processResponse(CreateMessageResponse response) {
        if (response.getResponseId() > 0) {
            responseAccumulator++;
            return true;
        } else {
            return false;
        }
    }
}
