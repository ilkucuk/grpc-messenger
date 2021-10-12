package com.kucuk.client.callers.messageServiceCaller;

import com.kucuk.client.MessageServiceTestHelper;
import com.kucuk.client.callers.CallResult;
import com.kucuk.client.resultWriters.ClientCallLogEntry;
import com.kucuk.client.resultWriters.CustomCallLogWriter;
import com.kucuk.message.CreateMessageRequest;
import com.kucuk.message.CreateMessageResponse;
import com.kucuk.message.MessageServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;

import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class CreateMessageStreamingCaller extends MessageServiceCallerBase {

    private int callCount;
    private final MessageServiceTestHelper testHelper;
    private final ManagedChannel channel;
    private final MessageServiceGrpc.MessageServiceStub asyncStub;
    private long responseAccumulator = 0;
    private StreamObserver<CreateMessageRequest> requestObserver;
    private final CustomCallLogWriter customCallLogWriter;

    public CreateMessageStreamingCaller(int callCount, MessageServiceTestHelper testHelper, CustomCallLogWriter customCallLogWriter) throws Exception {
        this.callCount = callCount;
        this.testHelper = testHelper;
        this.customCallLogWriter = customCallLogWriter;

        channel = getChannel();
        asyncStub = MessageServiceGrpc.newStub(channel);
    }

    @Override
    public CallResult call() throws Exception {

        final int[] success = {0};
        final int[] failure = {0};
        final long[] callStartTime = {0L};
        final long[] totalDuration = {0};

        final CountDownLatch finishLatch = new CountDownLatch(1);

        requestObserver = asyncStub.createMessageStreaming(new StreamObserver<>() {
            @Override
            public void onNext(CreateMessageResponse response) {
                // 1. time calculations
                final long callEndTime = Instant.now().toEpochMilli();
                totalDuration[0] += callEndTime - callStartTime[0];

                // 2. analyse the response
                boolean wasCallSuccessful = false;
                if (isValidCreateMessageResponse(response)) {
                    responseAccumulator++;
                    success[0]++;
                    wasCallSuccessful = true;
                } else {
                    failure[0]++;
                }

                // 3. write log line corresponding to the call
                customCallLogWriter.write(ClientCallLogEntry.builder()
                        .duration(callEndTime - callStartTime[0])
                        .success(wasCallSuccessful)
                        .timeStamp(callEndTime)
                        .build());

                // 4. Make a new create call if the callCount is not zero
                if (callCount > 0) {
                    callStartTime[0] = Instant.now().toEpochMilli();
                    CreateMessageRequest request = testHelper.newCreateMessageRequest();
                    requestObserver.onNext(request);
                    callCount--;
                } else {
                    // Mark the end of requests
                    requestObserver.onCompleted();
                }
            }

            @Override
            public void onError(Throwable throwable) {
                throwable.printStackTrace();
                finishLatch.countDown();
            }

            @Override
            public void onCompleted() {
                finishLatch.countDown();
            }
        });

        try {
            // Make the first call and reduce the call count by 1.
            callStartTime[0] = Instant.now().toEpochMilli();
            CreateMessageRequest request = testHelper.newCreateMessageRequest();
            requestObserver.onNext(request);
            callCount--;

            // Wait for all responses from the server
            // A single call to server cannot take longer than 20 seconds
            // So waiting at the most for 20 seconds *  number of calls.
            final int waitPeriod = 20 * callCount;
            if (!finishLatch.await(waitPeriod, TimeUnit.SECONDS)) {
                System.out.printf("CreateMessageStreamingCaller can not finish within  %s seconds", waitPeriod);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            requestObserver.onError(ex);
        }

        channel.shutdownNow().awaitTermination(5, TimeUnit.MINUTES);

        return CallResult.builder()
                .successCount(success[0])
                .failureCount(failure[0])
                .duration(totalDuration[0])
                .accumulator(responseAccumulator)
                .build();
    }

    private boolean isValidCreateMessageResponse(CreateMessageResponse response) {
        return response.getResponseId() > 0;
    }

}
