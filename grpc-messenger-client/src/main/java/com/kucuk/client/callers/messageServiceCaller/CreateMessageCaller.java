package com.kucuk.client.callers.messageServiceCaller;

import com.kucuk.client.MessageServiceTestHelper;
import com.kucuk.client.callers.CallResult;
import com.kucuk.client.resultWriters.ClientCallLogEntry;
import com.kucuk.client.resultWriters.CustomCallLogWriter;
import com.kucuk.message.CreateMessageRequest;
import com.kucuk.message.CreateMessageResponse;
import com.kucuk.message.MessageServiceGrpc;
import io.grpc.ManagedChannel;

import java.time.Instant;

public class CreateMessageCaller extends MessageServiceCallerBase {

    private final int callCount;
    private final MessageServiceTestHelper testHelper;
    private final ManagedChannel channel;
    private final CustomCallLogWriter customCallLogWriter;
    private final MessageServiceGrpc.MessageServiceBlockingStub clientStub;

    private long responseAccumulator = 0;

    public CreateMessageCaller(int callCount, MessageServiceTestHelper testHelper, CustomCallLogWriter customCallLogWriter) throws Exception {
        this.callCount = callCount;
        this.testHelper = testHelper;
        this.customCallLogWriter = customCallLogWriter;
        channel = getChannel();
        clientStub = MessageServiceGrpc.newBlockingStub(channel);
    }

    @Override
    public CallResult call() {

        int success = 0;
        int failure = 0;
        long totalDuration = 0;
        for (int i = 0; i < callCount; i++) {
            final long callStartTime = Instant.now().toEpochMilli();

            // 1. Make a create call.
            CreateMessageRequest request = testHelper.newCreateMessageRequest();
            CreateMessageResponse response = clientStub.createMessage(request);

            // 2. time calculations
            final long callEndTime = Instant.now().toEpochMilli();
            totalDuration += callEndTime - callStartTime;

            // 3. validate the response.
            boolean wasCallSuccessful = false;
            if (isValidCreateMessageResponse(response)) {
                responseAccumulator++;
                success++;
                wasCallSuccessful = true;
            } else {
                failure++;
            }

            // 4. write log line corresponding to the call
            customCallLogWriter.write(ClientCallLogEntry.builder()
                    .duration(callEndTime - callStartTime)
                    .success(wasCallSuccessful)
                    .timeStamp(callEndTime)
                    .build());
        }
        channel.shutdown();

        return CallResult.builder()
                .successCount(success)
                .failureCount(failure)
                .duration(totalDuration)
                .accumulator(responseAccumulator)
                .build();
    }

    private boolean isValidCreateMessageResponse(CreateMessageResponse response) {
        return response.getResponseId() > 0;
    }

}
