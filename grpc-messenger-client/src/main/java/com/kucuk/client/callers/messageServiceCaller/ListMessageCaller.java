package com.kucuk.client.callers.messageServiceCaller;

import com.kucuk.client.MessageServiceTestHelper;
import com.kucuk.client.callers.CallResult;
import com.kucuk.client.resultWriters.ClientCallLogEntry;
import com.kucuk.client.resultWriters.CustomCallLogWriter;
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
    private final CustomCallLogWriter customCallLogWriter;

    private long responseAccumulator = 0;

    public ListMessageCaller(int callCount, MessageServiceTestHelper testHelper, CustomCallLogWriter customCallLogWriter) throws Exception {
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
            ListMessageRequest request = testHelper.newListMessageRequest();
            ListMessageResponse response = clientStub.listMessage(request);

            // 2. time calculations
            final long callEndTime = Instant.now().toEpochMilli();
            totalDuration += callEndTime - callStartTime;

            // 3. validate the response.
            boolean wasCallSuccessful = false;
            if (isValidListMessageResponse(response)) {
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

    private boolean isValidListMessageResponse(ListMessageResponse response) {
        return response.getMessagesCount() > 0;
    }

}
