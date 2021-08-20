package com.kucuk.server.service;

import com.google.common.util.concurrent.ListenableFuture;
import com.kucuk.block.BlockServiceGrpc;
import com.kucuk.block.BlockingCallRequest;
import com.kucuk.block.BlockingCallResponse;
import com.kucuk.message.CreateMessageRequest;
import com.kucuk.message.CreateMessageResponse;
import com.kucuk.message.ListMessageRequest;
import com.kucuk.message.ListMessageResponse;
import com.kucuk.message.Message;
import com.kucuk.message.MessageServiceGrpc;
import com.kucuk.server.dao.MessageDao;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import org.apache.commons.codec.digest.DigestUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MessageService extends MessageServiceGrpc.MessageServiceImplBase {
    private final ManagedChannel blockingServiceChannel;

    public MessageService(ManagedChannel blockingServiceChannel) {
        this.blockingServiceChannel = blockingServiceChannel;

    }

    @Override
    public void createMessage(CreateMessageRequest request,
                              StreamObserver<CreateMessageResponse> responseObserver) {
        final String sha256hex = DigestUtils.sha256Hex(request.getRequestId() + request.getTitle() + request.getContent() + request.getAuthor());
        if (request.getBlockingCallPeriod() > 0 && request.getBlockingCallPeriod() < 5001) {
            BlockServiceGrpc.BlockServiceFutureStub clientStub = BlockServiceGrpc.newFutureStub(blockingServiceChannel);
            ListenableFuture<BlockingCallResponse> response = clientStub.blockingCall(BlockingCallRequest.newBuilder()
                    .setBlockPeriodInMilliSeconds(request.getBlockingCallPeriod())
                    .build());
            response.addListener(() -> {
                try {
                    responseObserver.onNext(CreateMessageResponse.newBuilder()
                            .setResponseId(Instant.now().toEpochMilli())
                            .setHash(sha256hex)
                            .setTime(response.get().getCurrentTime())
                            .setSampleBooleanField(!request.getSampleBooleanField())
                            .setSampleDoubleField(request.getSampleDoubleField() + 0.1)
                            .setSampleIntegerField(request.getSampleIntegerField() + 1)
                            .build());
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                    responseObserver.onError(e);
                }
                responseObserver.onCompleted();
            }, Runnable::run);

        } else {
            responseObserver.onNext(CreateMessageResponse.newBuilder()
                    .setResponseId(Instant.now().toEpochMilli())
                    .setHash(sha256hex)
                    .setTime(0L)
                    .setSampleBooleanField(!request.getSampleBooleanField())
                    .setSampleDoubleField(request.getSampleDoubleField() + 0.1)
                    .setSampleIntegerField(request.getSampleIntegerField() + 1)
                    .build());
            responseObserver.onCompleted();
        }

    }

    @Override
    public void listMessage(ListMessageRequest request,
                            StreamObserver<ListMessageResponse> responseObserver) {

        final List<Message> messageList = new ArrayList<>(MessageDao.getMessages(request.getPageSize()));
        if (request.getBlockingCallPeriod() > 0 && request.getBlockingCallPeriod() < 5001) {
            BlockServiceGrpc.BlockServiceFutureStub clientStub = BlockServiceGrpc.newFutureStub(blockingServiceChannel);
            ListenableFuture<BlockingCallResponse> response = clientStub.blockingCall(BlockingCallRequest.newBuilder()
                    .setBlockPeriodInMilliSeconds(request.getBlockingCallPeriod())
                    .build());
            response.addListener(() -> {
                try {
                    responseObserver.onNext(ListMessageResponse.newBuilder()
                            .setHasNext(true)
                            .setNextPageToken(request.getPageToken() + "-Next")
                            .setTime(response.get().getCurrentTime())
                            .addAllMessages(messageList)
                            .build());
                    responseObserver.onCompleted();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                    responseObserver.onError(e);
                }
                responseObserver.onCompleted();
            }, Runnable::run);

        } else {
            responseObserver.onNext(ListMessageResponse.newBuilder()
                    .setHasNext(true)
                    .setNextPageToken(request.getPageToken() + "-Next")
                    .setTime(0L)
                    .addAllMessages(messageList)
                    .build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public StreamObserver<CreateMessageRequest> createMessageStreaming(
            StreamObserver<CreateMessageResponse> responseObserver) {
        return new StreamObserver<>() {
            @Override
            public void onNext(CreateMessageRequest request) {
                final String sha256hex = DigestUtils.sha256Hex(request.getRequestId() + request.getTitle() + request.getContent() + request.getAuthor());
                if (request.getBlockingCallPeriod() > 0 && request.getBlockingCallPeriod() < 5001) {
                    BlockServiceGrpc.BlockServiceFutureStub clientStub = BlockServiceGrpc.newFutureStub(blockingServiceChannel);
                    ListenableFuture<BlockingCallResponse> response = clientStub.blockingCall(BlockingCallRequest.newBuilder()
                            .setBlockPeriodInMilliSeconds(request.getBlockingCallPeriod())
                            .build());
                    response.addListener(() -> {
                        try {
                            responseObserver.onNext(CreateMessageResponse.newBuilder()
                                    .setResponseId(Instant.now().toEpochMilli())
                                    .setHash(sha256hex)
                                    .setTime(response.get().getCurrentTime())
                                    .setSampleBooleanField(!request.getSampleBooleanField())
                                    .setSampleDoubleField(request.getSampleDoubleField() + 0.1)
                                    .setSampleIntegerField(request.getSampleIntegerField() + 1)
                                    .build());
                        } catch (InterruptedException | ExecutionException e) {
                            e.printStackTrace();
                            responseObserver.onError(e);
                        }
                    }, Runnable::run);
                } else {
                    responseObserver.onNext(CreateMessageResponse.newBuilder()
                            .setResponseId(Instant.now().toEpochMilli())
                            .setHash(sha256hex)
                            .setTime(0L)
                            .setSampleBooleanField(!request.getSampleBooleanField())
                            .setSampleDoubleField(request.getSampleDoubleField() + 0.1)
                            .setSampleIntegerField(request.getSampleIntegerField() + 1)
                            .build());
                }
            }

            @Override
            public void onError(Throwable throwable) {
                throwable.printStackTrace();
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }
}
