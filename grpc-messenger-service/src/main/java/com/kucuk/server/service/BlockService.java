package com.kucuk.server.service;

import com.kucuk.block.BlockServiceGrpc;
import com.kucuk.block.BlockingCallRequest;
import com.kucuk.block.BlockingCallResponse;
import io.grpc.stub.StreamObserver;

import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class BlockService extends BlockServiceGrpc.BlockServiceImplBase {

    public BlockService() {
    }

    @Override
    public void blockingCall(BlockingCallRequest request,
                             StreamObserver<BlockingCallResponse> responseObserver) {
        // 1. blockingCall makes a Thread.sleep call so declaring the code as runnable to not block
        // the main thread.
        final Runnable blockingCallTask = () -> {
            try {
                if (request.getBlockPeriodInMilliSeconds() > 0) {
                    try {
                        Thread.sleep(request.getBlockPeriodInMilliSeconds());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                responseObserver.onNext(BlockingCallResponse.newBuilder()
                        .setCurrentTime(Instant.now().toEpochMilli())
                        .build());
                responseObserver.onCompleted();
            } catch (Exception ex) {
                ex.printStackTrace();
                responseObserver.onError(ex);
            }
        };

        // 2. Create a new executorService to run the blockingCall a new thread to not block the main thread
        final ExecutorService executor = Executors.newFixedThreadPool(1);
        executor.submit(blockingCallTask);

        // 3. Shutdown the executorService
        try {
            executor.shutdown();
            if (executor.awaitTermination(1, TimeUnit.MINUTES)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
