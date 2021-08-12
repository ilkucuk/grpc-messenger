package com.kucuk.server.service;

import com.kucuk.message.CreateMessageRequest;
import com.kucuk.message.CreateMessageResponse;
import com.kucuk.message.ListMessageRequest;
import com.kucuk.message.ListMessageResponse;
import com.kucuk.message.Message;
import com.kucuk.server.dao.MessageDao;
import org.apache.commons.codec.digest.DigestUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class MessageServiceHelper {

    static CreateMessageResponse newCreateMessageResponse(final CreateMessageRequest request, final long timeStamp) {
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
