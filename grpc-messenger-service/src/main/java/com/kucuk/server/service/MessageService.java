package com.kucuk.server.service;

import com.kucuk.message.CreateMessageRequest;
import com.kucuk.message.CreateMessageResponse;
import com.kucuk.message.ListMessageRequest;
import com.kucuk.message.ListMessageResponse;
import com.kucuk.message.MessageServiceGrpc;
import com.kucuk.server.BlockingServiceClientFactory;
import io.grpc.stub.StreamObserver;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.InvocationCallback;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

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
                    responseObserver.onNext(MessageServiceHelper.newCreateMessageResponse(request, blockingCallTimeStamp));
                    responseObserver.onCompleted();
                }

                @Override
                public void failed(Throwable throwable) {

                }
            });

        } else {
            responseObserver.onNext(MessageServiceHelper.newCreateMessageResponse(request, 0L));
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

                    responseObserver.onNext(MessageServiceHelper.newListMessageResponse(request, blockingCallTimeStamp));
                    responseObserver.onCompleted();
                }

                @Override
                public void failed(Throwable throwable) {

                }
            });

        } else {
            responseObserver.onNext(MessageServiceHelper.newListMessageResponse(request, 0L));
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
                            responseObserver.onNext(MessageServiceHelper.newCreateMessageResponse(request, blockingCallTimeStamp));
                        }

                        @Override
                        public void failed(Throwable throwable) {

                        }
                    });

                } else {
                    responseObserver.onNext(MessageServiceHelper.newCreateMessageResponse(request, 0L));
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
