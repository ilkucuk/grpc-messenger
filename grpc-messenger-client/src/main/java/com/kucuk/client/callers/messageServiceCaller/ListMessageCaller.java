package com.kucuk.client.callers.messageServiceCaller;

import com.kucuk.client.MessageServiceTestHelper;
import com.kucuk.client.callers.CallResult;
import com.kucuk.message.ListMessageRequest;
import com.kucuk.message.ListMessageResponse;
import com.kucuk.message.MessageServiceGrpc;
import io.grpc.ManagedChannel;

import java.time.Instant;

public class ListMessageCaller extends MessageServiceCallerBase {

    private final int callCount;
    private final MessageServiceTestHelper testHelper;
    private final ManagedChannel channel;
    private final MessageServiceGrpc.MessageServiceBlockingStub clientStub;

    private long responseAccumulator = 0;

    public ListMessageCaller(int callCount, MessageServiceTestHelper testHelper) throws Exception {
        this.callCount = callCount;
        this.testHelper = testHelper;

        channel = getChannel();
        clientStub = MessageServiceGrpc.newBlockingStub(channel);
    }


    @Override
    public CallResult call() {

        int success = 0;
        int failure = 0;

        long start = Instant.now().toEpochMilli();
        for (int i = 0; i < callCount; i++) {
            ListMessageRequest request = testHelper.newListMessageRequest();
            ListMessageResponse response = clientStub.listMessage(request);

            if (isValidListMessageResponse(response)) {
                responseAccumulator++;
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

    private boolean isValidListMessageResponse(ListMessageResponse response) {
        return response.getMessagesCount() > 0;
    }

}
