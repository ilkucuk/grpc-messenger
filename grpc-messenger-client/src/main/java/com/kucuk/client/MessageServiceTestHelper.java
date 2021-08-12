package com.kucuk.client;

import com.kucuk.message.CreateMessageRequest;
import com.kucuk.message.CreateMessageResponse;
import com.kucuk.message.ListMessageRequest;
import com.kucuk.message.ListMessageResponse;
import io.grpc.ManagedChannel;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.channel.ChannelOption;

import javax.net.ssl.SSLException;
import java.io.File;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class MessageServiceTestHelper {

    private static final String AUTHOR = "ikucuk@gmail.com";
    private static final String TITLE = "Sample Message Title";
    private static final String LOREM_IPSUM = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum." +
            "Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque laudantium, totam rem aperiam, eaque ipsa quae ab illo inventore veritatis et quasi architecto beatae vitae dicta sunt explicabo. Nemo enim ipsam voluptatem quia voluptas sit aspernatur aut odit aut fugit, sed quia consequuntur magni dolores eos qui ratione voluptatem sequi nesciunt. Neque porro quisquam est, qui dolorem ipsum quia dolor sit amet, consectetur, adipisci velit, sed quia non numquam eius modi tempora incidunt ut labore et dolore magnam aliquam quaerat voluptatem. Ut enim ad minima veniam, quis nostrum exercitationem ullam corporis suscipit laboriosam, nisi ut aliquid ex ea commodi consequatur? Quis autem vel eum iure reprehenderit qui in ea voluptate velit esse quam nihil molestiae consequatur, vel illum qui dolorem eum fugiat quo voluptas nulla pariatur?" +
            "At vero eos et accusamus et iusto odio dignissimos ducimus qui blanditiis praesentium voluptatum deleniti atque corrupti quos dolores et quas molestias excepturi sint occaecati cupiditate non provident, similique sunt in culpa qui officia deserunt mollitia animi, id est laborum et dolorum fuga. Et harum quidem rerum facilis est et expedita distinctio. Nam libero tempore, cum soluta nobis est eligendi optio cumque nihil impedit quo minus id quod maxime placeat facere possimus, omnis voluptas assumenda est, omnis dolor repellendus. Temporibus autem quibusdam et aut officiis debitis aut rerum necessitatibus saepe eveniet ut et voluptates repudiandae sint et molestiae non recusandae. Itaque earum rerum hic tenetur a sapiente delectus, ut aut reiciendis voluptatibus maiores alias consequatur aut perferendis doloribus asperiores repellat.";

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

        return CreateMessageRequest.newBuilder()
                .setRequestId(requestId)
                .setAuthor(AUTHOR)
                .setTitle(TITLE)
                .setContent(LOREM_IPSUM)
                .setTime(Instant.now().toEpochMilli())
                .setBlockingCallPeriod(blockingCallPeriod)
                .setSampleBooleanField(sampleBoolean)
                .setSampleDoubleField(sampleDouble)
                .setSampleIntegerField(sampleInteger)
                .build();
    }

    public ListMessageRequest newListMessageRequest() {
        return ListMessageRequest.newBuilder()
                .setBlockingCallPeriod(blockingCallPeriod)
                .setAuthor(AUTHOR)
                .setPageSize(pageSize)
                .setPageToken(UUID.randomUUID().toString())
                .build();
    }


    public ManagedChannel getChannel() throws SSLException {
        String serverEndpoint = "kucuk.com";
        int serverPort = 443;
        String caPath = "../cert/ca.crt";

        return NettyChannelBuilder.forAddress(serverEndpoint, serverPort)
                .useTransportSecurity()
                .sslContext(GrpcSslContexts.forClient().trustManager(new File(caPath)).build())
                .withOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) TimeUnit.SECONDS.toMillis(10))
                .keepAliveWithoutCalls(true)
                .keepAliveTime(120, TimeUnit.SECONDS)
                .keepAliveTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    public boolean isValidCreateMessageResponse(CreateMessageResponse response) {
        return response.getResponseId() > 0;
    }

    public boolean isValidListMessageResponse(ListMessageResponse response) {
        return response.getMessagesCount() > 0;
    }

}
