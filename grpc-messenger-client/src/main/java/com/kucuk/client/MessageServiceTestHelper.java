package com.kucuk.client;


import com.kucuk.message.CreateMessageRequest;
import com.kucuk.message.ListMessageRequest;

import java.time.Instant;

public class MessageServiceTestHelper {

    private String author = "ikucuk@gmail.com";
    private String title = "Sample Message Title";
    private String content = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";

    private final int blockingCallPeriod;
    private final int pageSize;

    private long requestId;
    private boolean sampleBoolean = false;
    private double sampleDouble = 1.0d;
    private int sampleInteger = 0;

    MessageServiceTestHelper(int blockingCallPeriod, int pageSize) {
        this.blockingCallPeriod = blockingCallPeriod;
        this.pageSize = pageSize;
    }

    public CreateMessageRequest newCreateMessageRequest() {
        requestId++;
        sampleBoolean = !sampleBoolean;
        sampleDouble += 0.001;
        sampleInteger++;

        return com.kucuk.message.CreateMessageRequest.newBuilder()
                .setRequestId(requestId)
                .setAuthor(author)
                .setTitle(title)
                .setContent(content)
                .setTime(Instant.now().toEpochMilli())
                .setBlockingCallPeriod(blockingCallPeriod)
                .setSampleBooleanField(sampleBoolean)
                .setSampleDoubleField(sampleDouble)
                .setSampleIntegerField(sampleInteger)
                .build();
    }

    public ListMessageRequest newListMessageRequest() {
        return com.kucuk.message.ListMessageRequest.newBuilder()
                .setBlockingCallPeriod(blockingCallPeriod)
                .setAuthor(author)
                .setPageSize(pageSize)
                .setPageToken(Instant.now().toString())
                .build();
    }
}
