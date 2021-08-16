package com.kucuk.server.service;

import com.kucuk.message.CreateMessageRequest;
import com.kucuk.message.CreateMessageResponse;
import com.kucuk.message.ListMessageRequest;
import com.kucuk.message.ListMessageResponse;
import com.kucuk.message.Message;
import com.kucuk.message.MessageServiceGrpc;
import com.kucuk.server.BlockingServiceClientFactory;
import com.kucuk.server.dao.MessageDao;
import io.grpc.stub.StreamObserver;
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

    @Override
    public void createMessage(CreateMessageRequest request,
                             StreamObserver<CreateMessageResponse> responseObserver) {


        if (request.getBlockingCallPeriod() > 0 && request.getBlockingCallPeriod() < 5001) {

            WebTarget target = BlockingServiceClientFactory.getTarget(request.getBlockingCallPeriod());
            Invocation.Builder invocationBuilder = target.request(MediaType.APPLICATION_JSON);
            invocationBuilder.async().get(new InvocationCallback<Response>() {
                @Override
                public void completed(Response response) {
                    Long blockingCallTimeStamp = response.readEntity(Long.class);
                    responseObserver.onNext(newCreateMessageResponse(request, blockingCallTimeStamp));
                    responseObserver.onCompleted();
                }

                @Override
                public void failed(Throwable throwable) {

                }
            });

        } else {
            responseObserver.onNext(newCreateMessageResponse(request, 0L));
            responseObserver.onCompleted();
        }
    }

    @Override
    public void listMessage(ListMessageRequest request,
                            StreamObserver<ListMessageResponse> responseObserver) {

        if (request.getBlockingCallPeriod() > 0 && request.getBlockingCallPeriod() < 5001) {

            WebTarget target = BlockingServiceClientFactory.getTarget(request.getBlockingCallPeriod());
            Invocation.Builder invocationBuilder = target.request(MediaType.APPLICATION_JSON);
            invocationBuilder.async().get(new InvocationCallback<Response>() {
                @Override
                public void completed(Response response) {
                    Long blockingCallTimeStamp = response.readEntity(Long.class);

                    responseObserver.onNext(newListMessageResponse(request, blockingCallTimeStamp));
                    responseObserver.onCompleted();
                }

                @Override
                public void failed(Throwable throwable) {

                }
            });

        } else {
            responseObserver.onNext(newListMessageResponse(request, 0L));
            responseObserver.onCompleted();
        }
    }

    @Override
    public StreamObserver<CreateMessageRequest> createMessageStreaming(
            StreamObserver<CreateMessageResponse> responseObserver) {
        return new StreamObserver<>() {
            @Override
            public void onNext(CreateMessageRequest request) {
                if (request.getBlockingCallPeriod() > 0 && request.getBlockingCallPeriod() < 5001) {

                    WebTarget target = BlockingServiceClientFactory.getTarget(request.getBlockingCallPeriod());
                    Invocation.Builder invocationBuilder = target.request(MediaType.APPLICATION_JSON);
                    invocationBuilder.async().get(new InvocationCallback<Response>() {
                        @Override
                        public void completed(Response response) {
                            Long blockingCallTimeStamp = response.readEntity(Long.class);
                            responseObserver.onNext(newCreateMessageResponse(request, blockingCallTimeStamp));
                        }

                        @Override
                        public void failed(Throwable throwable) {

                        }
                    });

                } else {
                    responseObserver.onNext(newCreateMessageResponse(request, 0L));
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

    private CreateMessageResponse newCreateMessageResponse(final CreateMessageRequest request, final long timeStamp) {
        final String sha256hex = DigestUtils.sha256Hex(request.getRequestId() + request.getTitle() + request.getContent() + request.getAuthor());

        return CreateMessageResponse.newBuilder()
                .setResponseId(Instant.now().toEpochMilli())
                .setHash(sha256hex)
                .setTime(timeStamp)
                .setSampleBooleanField(!request.getSampleBooleanField())
                .setSampleDoubleField(request.getSampleDoubleField() + 0.1)
                .setSampleIntegerField(request.getSampleIntegerField() + 1)
                .build();
    }

    static ListMessageResponse newListMessageResponse (final ListMessageRequest request, final long timeStamp) {
        final List<Message> messageList = new ArrayList<>(MessageDao.getMessages(request.getPageSize()));

        return ListMessageResponse.newBuilder()
                .setHasNext(true)
                .setNextPageToken(request.getPageToken() + "-Next")
                .setTime(timeStamp)
                .addAllMessages(messageList)
                .build();
    }

}
