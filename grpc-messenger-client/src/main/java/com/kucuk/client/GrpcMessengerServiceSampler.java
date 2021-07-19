package com.kucuk.client;

import com.kucuk.message.CreateMessageRequest;
import com.kucuk.message.CreateMessageResponse;
import com.kucuk.message.MessageServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.channel.ChannelOption;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

import javax.net.ssl.SSLException;
import java.io.File;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

public class GrpcMessengerServiceSampler extends AbstractJavaSamplerClient {

    MessageServiceGrpc.MessageServiceBlockingStub clientStub;
    ManagedChannel channel;

    @Override
    public void setupTest(JavaSamplerContext context) {
        String host = context.getParameter("host");
        int port = Integer.parseInt(context.getParameter("port"));
        File ca = new File(context.getParameter("ca"));

        try {

            channel = NettyChannelBuilder.forAddress(host, port)
                    .useTransportSecurity()
                    .sslContext(GrpcSslContexts.forClient().trustManager(ca).build())
                    .withOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) TimeUnit.SECONDS.toMillis(30))
                    .build();


            clientStub = MessageServiceGrpc.newBlockingStub(channel);
            super.setupTest(context);

        } catch (SSLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void teardownTest(JavaSamplerContext context) {
        if (channel != null) {
            channel.shutdown();
        }
    }

    @Override
    public Arguments getDefaultParameters() {
        Arguments defaultParameters = new Arguments();
        defaultParameters.addArgument("host", "127.0.0.1");
        defaultParameters.addArgument("port", "8080");
        defaultParameters.addArgument("ca", "ca.crt");
        defaultParameters.addArgument("requestId", "1000");
        defaultParameters.addArgument("author", "ikucuk@gmail.com");
        defaultParameters.addArgument("title", "Default Title");
        defaultParameters.addArgument("content", "Default Content");
        defaultParameters.addArgument("sleepPeriod", "0");

        return defaultParameters;
    }

    @Override
    public SampleResult runTest(JavaSamplerContext javaSamplerContext) {
        SampleResult result = new SampleResult();
        result.sampleStart();

        try {

            int requestId = Integer.parseInt(javaSamplerContext.getParameter("requestId"));
            String author = javaSamplerContext.getParameter("author");
            String title = javaSamplerContext.getParameter("title");
            String content = javaSamplerContext.getParameter("content");
            int sleepPeriod = Integer.parseInt(javaSamplerContext.getParameter("sleepPeriod"));

            CreateMessageRequest request = CreateMessageRequest.newBuilder()
                    .setRequestId(requestId)
                    .setAuthor(author)
                    .setTitle(title)
                    .setContent(content)
                    .setTime(Instant.now().toEpochMilli())
                    .setSleepPeriod(sleepPeriod)
                    .build();

            CreateMessageResponse response = this.clientStub.createMessage(request);
            result.sampleEnd();
            result.setSuccessful(true);
            result.setResponseData(response.getHash(), "UTF-8");
            result.setResponseCodeOK(); // 200 code

        } catch (Exception e) {
            result.sampleEnd(); // stop stopwatch
            result.setSuccessful(false);
            result.setResponseMessage("Exception: " + e);
            // get stack trace as a String to return as document data
            java.io.StringWriter stringWriter = new java.io.StringWriter();
            e.printStackTrace(new java.io.PrintWriter(stringWriter));
            result.setResponseData(stringWriter.toString().getBytes());
            result.setDataType(org.apache.jmeter.samplers.SampleResult.TEXT);
            result.setResponseCode("500");
        }
        return result;
    }

}
