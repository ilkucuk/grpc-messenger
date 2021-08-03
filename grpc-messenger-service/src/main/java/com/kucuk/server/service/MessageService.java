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
        final long blockingCallTimeStamp = makeBlockingCall(request.getBlockingCallPeriod());
        final String sha256hex = DigestUtils.sha256Hex(request.getRequestId() + request.getTitle() + request.getContent() + request.getAuthor());

        CreateMessageResponse response = CreateMessageResponse.newBuilder()
                .setResponseId(Instant.now().toEpochMilli())
                .setHash(sha256hex)
                .setTime(blockingCallTimeStamp)
                .setSampleBooleanField(!request.getSampleBooleanField())
                .setSampleDoubleField(request.getSampleDoubleField() + 0.1)
                .setSampleIntegerField(request.getSampleIntegerField() + 1)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();

    }

    @Override
    public void listMessage(com.kucuk.message.ListMessageRequest request,
                            io.grpc.stub.StreamObserver<com.kucuk.message.ListMessageResponse> responseObserver) {
        final long blockingCallTimeStamp = makeBlockingCall(request.getBlockingCallPeriod());

        final List<Message> messageList = new ArrayList<>(MessageDao.getMessages(request.getPageSize()));

        ListMessageResponse listMessageResponse = ListMessageResponse.newBuilder()
                .setHasNext(true)
                .setNextPageToken(request.getPageToken() + "-Next")
                .setTime(blockingCallTimeStamp)
                .addAllMessages(messageList)
                .build();

        responseObserver.onNext(listMessageResponse);
        responseObserver.onCompleted();

    }


    private Long makeBlockingCall(Integer blockingPeriod) {
        final Long[] blockingCallTimeStamp = {0L};

        if (blockingPeriod > 0 && blockingPeriod < 5001) {

            WebTarget target = BlockingServiceClientFactory.getTarget(blockingPeriod);
            Invocation.Builder invocationBuilder = target.request(MediaType.APPLICATION_JSON);
            invocationBuilder.async().get(new InvocationCallback<Response>() {
                @Override
                public void completed(Response response) {
                    blockingCallTimeStamp[0] = response.readEntity(Long.class);
                }

                @Override
                public void failed(Throwable throwable) {

                }
            });
        }

        return blockingCallTimeStamp[0];
    }
}
