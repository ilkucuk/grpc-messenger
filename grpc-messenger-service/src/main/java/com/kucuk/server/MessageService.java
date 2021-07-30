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
import org.apache.commons.text.RandomStringGenerator;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class MessageService extends MessageServiceGrpc.MessageServiceImplBase {

    private final static int NO_MESSAGES = 1000;
    private static final int MESSAGE_LEN = 1000;
    private static String[] MESSAGE_CONTENT_ARRAY;

    public MessageService() {
        MESSAGE_CONTENT_ARRAY = new String[NO_MESSAGES];
        RandomStringGenerator generator = new RandomStringGenerator.Builder()
                .withinRange('a', 'z').build();
        for (int i = 0; i < NO_MESSAGES; i++) {
            MESSAGE_CONTENT_ARRAY[i] = generator.generate(MESSAGE_LEN);
        }
    }

    @Override
    public void createMessage(CreateMessageRequest request,
                              io.grpc.stub.StreamObserver<CreateMessageResponse> responseObserver) {
        // System.out.println("Received a request for creating a message" + request);

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
                    .messageCount(request.getMessageCount())
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

            List<com.kucuk.message.Message> messages = new ArrayList<>(request.getMessageCount());
            for (int i = 0; i < request.getMessageCount(); i++) {
                messages.set(i,
                        com.kucuk.message.Message.newBuilder()
                                .setMessageId(Instant.now().toEpochMilli())
                                .setContent(MESSAGE_CONTENT_ARRAY[i % NO_MESSAGES])
                                .setTime(Instant.now().toEpochMilli())
                                .build());
            }

            CreateMessageResponse rsp = CreateMessageResponse.newBuilder()
                    .setResponseId(Instant.now().toEpochMilli())
                    .setHash(sha256hex)
                    .setSampleBooleanField(!request.getSampleBooleanField())
                    .setSampleDoubleField(request.getSampleDoubleField() + 1.234d)
                    .setSampleIntegerField(request.getSampleIntegerField() * 2)
                    .build();

            rsp.getMessagesList().addAll(messages);

            responseObserver.onNext(rsp);
            responseObserver.onCompleted();
        }
    }
}
