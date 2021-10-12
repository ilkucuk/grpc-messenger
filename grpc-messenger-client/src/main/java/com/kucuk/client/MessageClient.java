package com.kucuk.client;

import ch.qos.logback.classic.LoggerContext;
import com.kucuk.client.callers.CallResult;
import com.kucuk.client.callers.messageServiceCaller.CreateMessageCaller;
import com.kucuk.client.callers.messageServiceCaller.CreateMessageStreamingCaller;
import com.kucuk.client.callers.messageServiceCaller.ListMessageCaller;
import com.kucuk.client.callers.messageServiceCaller.MessageServiceCallerBase;
import com.kucuk.client.config.ClientConfig;
import com.kucuk.client.config.ConfigReader;
import com.kucuk.client.config.TestRunConfig;
import com.kucuk.client.resultWriters.CustomCallLogWriter;
import com.kucuk.client.resultWriters.ResultWriter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;


@Slf4j
public class MessageClient {

    public static void main(String[] args) throws Exception {

        // 1. Input validation
        if (args.length < 1) {
            System.out.println("Missing Client Config File!");
            return;
        }

        Path configFilePath = Path.of(args[0]);
        if (!configFilePath.toFile().exists()) {
            System.out.println("Invalid Config File Path");
            return;
        }

        ClientConfig clientConfig = ConfigReader.readServiceConfig(configFilePath);

        if (clientConfig == null || clientConfig.getTestRunConfigs() == null || clientConfig.getTestRunConfigs().size() == 0) {
            System.out.println("No Test Run Config found, Exiting");
            return;
        }

        File ca = new File("../cert/ca.crt");
        if (!ca.exists()) {
            System.out.println("Cert chain error!");
            return;
        }

        // 2. Create a directory to save the test results.
        final Path testResultRootDirectory = Files.createDirectory(Paths.get("TestRun" + Instant.now().toString()));
        final ResultWriter resultSummaryWriter = new ResultWriter(testResultRootDirectory.resolve( "Summary.txt").toString());

        for (TestRunConfig testRunConfig : clientConfig.getTestRunConfigs()) {

            System.out.println("blockingCallPeriod: " + testRunConfig.getBlockingCallPeriod() +
                    " ConcurrentClientThreadCount: " + testRunConfig.getConcurrentClientThreadCount() +
                    " CallCountForASingleClient: " + testRunConfig.getCallCountForASingleClient() +
                    " Caller" + testRunConfig.getCaller() +
                    " NumberOfRuns: " + testRunConfig.getNumberOfRuns() +
                    " PageSize: " + testRunConfig.getPageSize());

            MessageServiceTestHelper testHelper = new MessageServiceTestHelper(testRunConfig.getBlockingCallPeriod(), testRunConfig.getPageSize());
            resultSummaryWriter.write("---Test--Run---");
            resultSummaryWriter.write("Config: " + testRunConfig);

            final LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            final CustomCallLogWriter customCallLogWriter = new CustomCallLogWriter(testResultRootDirectory.resolve(testRunConfig.getCaller() + "-" + Instant.now().toString() + ".csv").toString(), loggerContext);
            for (int j = 0; j < testRunConfig.getNumberOfRuns(); j++) {
                ExecutorService executor = Executors.newFixedThreadPool(testRunConfig.getConcurrentClientThreadCount());
                List<Future<CallResult>> results = new ArrayList<>(testRunConfig.getConcurrentClientThreadCount());

                for (int i = 0; i < testRunConfig.getConcurrentClientThreadCount(); i++) {
                    MessageServiceCallerBase caller;

                    switch (testRunConfig.getCaller()) {
                        case "CreateMessageCaller":
                            caller = new CreateMessageCaller(testRunConfig.getCallCountForASingleClient(), testHelper, customCallLogWriter);
                            break;
                        case "ListMessageCaller":
                            caller = new ListMessageCaller(testRunConfig.getCallCountForASingleClient(), testHelper, customCallLogWriter);
                            break;
                        case "CreateMessageStreamingCaller":
                            caller = new CreateMessageStreamingCaller(testRunConfig.getCallCountForASingleClient(), testHelper, customCallLogWriter);
                            break;
                        default:
                            throw new IllegalArgumentException("Invalid caller" + testRunConfig.getCaller());
                    }

                    Future<CallResult> callResultFuture = executor.submit(caller);
                    results.add(callResultFuture);
                }

                executor.shutdown();
                if (executor.awaitTermination(90, TimeUnit.MINUTES)) {
                    executor.shutdownNow();
                }
                double total = 0;

                int totalAccumulator = 0;
                int totalSuccess = 0;
                int totalFailure = 0;
                for (Future<CallResult> resultFuture : results) {
                    CallResult result = resultFuture.get();
                    total += result.getDuration();
                    totalAccumulator += result.getAccumulator();
                    totalSuccess += result.getSuccessCount();
                    totalFailure += result.getFailureCount();
                }

                double avg = (total / (double) testRunConfig.getCallCountForASingleClient()) / (double) results.size();

                String resString = "Average Call Duration: " + avg +
                        " Success: " + totalSuccess +
                        " Failure: " + totalFailure +
                        " AC: " + totalAccumulator;
                System.out.println(resString);
                resultSummaryWriter.write(resString);
            }
            customCallLogWriter.stop();
        }

        resultSummaryWriter.close();
    }
}
