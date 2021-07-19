package com.kucuk.server;

import com.kucuk.message.CreateMessageRequest;
import com.kucuk.message.CreateMessageResponse;
import com.kucuk.message.MessageServiceGrpc;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.InvocationCallback;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.commons.codec.digest.DigestUtils;

import java.time.Instant;

public class MessageService extends MessageServiceGrpc.MessageServiceImplBase {


    @Override
    public void createMessage(CreateMessageRequest request,
                              io.grpc.stub.StreamObserver<CreateMessageResponse> responseObserver) {

        String sha256hex = DigestUtils.sha256Hex(request.getRequestId() + request.getTitle() + request.getContent() + request.getAuthor());

        if (request.getSleepPeriod() > 0) {
            WebTarget target = OtherMessageServiceClientFactory.getClient();
            Invocation.Builder invocationBuilder = target.request(MediaType.APPLICATION_JSON);

            com.kucuk.server.api.CreateMessageRequest otherRequest = com.kucuk.server.api.CreateMessageRequest.builder()
                    .requestId(Instant.now().toEpochMilli())
                    .author("ikucuk@gmail.com")
                    .title("Title")
                    .content("Content")
                    .time(Instant.now().toEpochMilli())
                    .sleepPeriod(request.getSleepPeriod())
                    .sampleBooleanField(true)
                    .sampleDoubleField(1d)
                    .sampleIntegerField(1)
                    .build();

            invocationBuilder.async().post(Entity.entity(otherRequest, MediaType.APPLICATION_JSON), new InvocationCallback<Response>() {
                @Override
                public void completed(Response response) {
                    com.kucuk.server.api.CreateMessageResponse entity = response.readEntity(com.kucuk.server.api.CreateMessageResponse.class);

                    CreateMessageResponse rsp = CreateMessageResponse.newBuilder()
                            .setResponseId(Instant.now().toEpochMilli())
                            .setHash(sha256hex)
                            .setSampleBooleanField(!request.getSampleBooleanField())
                            .setSampleDoubleField(entity.getSampleDoubleField() + 1.234d)
                            .setSampleIntegerField(request.getSampleIntegerField() * 2)
                            .build();

                    responseObserver.onNext(rsp);
                    responseObserver.onCompleted();
                }

                @Override
                public void failed(Throwable throwable) {

                }
            });

        } else {

            CreateMessageResponse rsp = CreateMessageResponse.newBuilder()
                    .setResponseId(Instant.now().toEpochMilli())
                    .setHash(sha256hex)
                    .setSampleBooleanField(!request.getSampleBooleanField())
                    .setSampleDoubleField(request.getSampleDoubleField() + 1.234d)
                    .setSampleIntegerField(request.getSampleIntegerField() * 2)
                    .build();

            responseObserver.onNext(rsp);
            responseObserver.onCompleted();
        }
    }
}
