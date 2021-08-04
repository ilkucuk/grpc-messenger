package com.kucuk.server.service;

import com.kucuk.message.CreateMessageRequest;
import com.kucuk.message.CreateMessageResponse;
import com.kucuk.message.ListMessageResponse;
import com.kucuk.message.Message;
import com.kucuk.message.MessageServiceGrpc;
import com.kucuk.server.BlockingServiceClientFactory;
import com.kucuk.server.dao.MessageDao;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.InvocationCallback;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.commons.codec.digest.DigestUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class MessageService extends MessageServiceGrpc.MessageServiceImplBase {

    public MessageService() {
    }

    @Override
    public void createMessage(CreateMessageRequest request,
                              io.grpc.stub.StreamObserver<CreateMessageResponse> responseObserver) {

        final String sha256hex = DigestUtils.sha256Hex(request.getRequestId() + request.getTitle() + request.getContent() + request.getAuthor());

        if (request.getBlockingCallPeriod() > 0 && request.getBlockingCallPeriod() < 5001) {

            WebTarget target = BlockingServiceClientFactory.getTarget(request.getBlockingCallPeriod());
            Invocation.Builder invocationBuilder = target.request(MediaType.APPLICATION_JSON);
            invocationBuilder.async().get(new InvocationCallback<Response>() {
                @Override
                public void completed(Response response) {
                    Long blockingCallTimeStamp = response.readEntity(Long.class);
                    responseObserver.onNext(CreateMessageResponse.newBuilder()
                            .setResponseId(Instant.now().toEpochMilli())
                            .setHash(sha256hex)
                            .setTime(blockingCallTimeStamp)
                            .setSampleBooleanField(!request.getSampleBooleanField())
                            .setSampleDoubleField(request.getSampleDoubleField() + 0.1)
                            .setSampleIntegerField(request.getSampleIntegerField() + 1)
                            .build());
                    responseObserver.onCompleted();
                }

                @Override
                public void failed(Throwable throwable) {

                }
            });

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
    public void listMessage(com.kucuk.message.ListMessageRequest request,
                            io.grpc.stub.StreamObserver<com.kucuk.message.ListMessageResponse> responseObserver) {
        final List<Message> messageList = new ArrayList<>(MessageDao.getMessages(request.getPageSize()));

        if (request.getBlockingCallPeriod() > 0 && request.getBlockingCallPeriod() < 5001) {

            WebTarget target = BlockingServiceClientFactory.getTarget(request.getBlockingCallPeriod());
            Invocation.Builder invocationBuilder = target.request(MediaType.APPLICATION_JSON);
            invocationBuilder.async().get(new InvocationCallback<Response>() {
                @Override
                public void completed(Response response) {
                    Long blockingCallTimeStamp = response.readEntity(Long.class);

                    responseObserver.onNext(ListMessageResponse.newBuilder()
                            .setHasNext(true)
                            .setNextPageToken(request.getPageToken() + "-Next")
                            .setTime(blockingCallTimeStamp)
                            .addAllMessages(messageList)
                            .build());
                    responseObserver.onCompleted();
                }

                @Override
                public void failed(Throwable throwable) {

                }
            });

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
}
