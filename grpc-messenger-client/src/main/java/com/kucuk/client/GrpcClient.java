package com.kucuk.client;

import com.kucuk.client.callers.CallResult;
import com.kucuk.client.callers.MessageServiceCaller;
import com.kucuk.client.callers.MessageServiceCreateCaller;
import com.kucuk.client.callers.MessageServiceListCaller;
import com.kucuk.client.config.ClientConfig;
import com.kucuk.client.config.ConfigReader;
import com.kucuk.client.config.TestRunConfig;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Slf4j
public class GrpcClient {

    public static void main(String[] args) throws Exception {

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

        ResultWriter resultWriter = new ResultWriter("TestRun-" + Instant.now().toString()+ ".txt");


        for (TestRunConfig testRunConfig : clientConfig.getTestRunConfigs()) {
            System.out.println("blockingCallPeriod: " + testRunConfig.getBlockingCallPeriod() +
                    " ConcurrentClientThreadCount: " + testRunConfig.getConcurrentClientThreadCount() +
                    " CallCountForASingleClient: " + testRunConfig.getCallCountForASingleClient() +
                    " Caller" + testRunConfig.getCaller() +
                    " NumberOfRuns: " + testRunConfig.getNumberOfRuns());
            MessageServiceTestHelper testHelper = new MessageServiceTestHelper(testRunConfig.getBlockingCallPeriod(), testRunConfig.getPageSize());
            resultWriter.write("---Test--Run---");
            resultWriter.write("Config: " + testRunConfig);

            for (int j = 0; j < testRunConfig.getNumberOfRuns(); j++) {

                ExecutorService executor = Executors.newFixedThreadPool(testRunConfig.getConcurrentClientThreadCount());
                List<Future<CallResult>> results = new ArrayList<>(testRunConfig.getConcurrentClientThreadCount());

                for (int i = 0; i < testRunConfig.getConcurrentClientThreadCount(); i++) {
                    MessageServiceCaller caller = null;

                    switch (testRunConfig.getCaller()) {
                        case "MessageServiceCreateCaller":
                            caller = new MessageServiceCreateCaller(testRunConfig.getConcurrentClientThreadCount(), testHelper);
                            break;
                        case "MessageServiceListCaller":
                            caller = new MessageServiceListCaller(testRunConfig.getConcurrentClientThreadCount(), testHelper);
                            break;
                    }

                    assert caller != null;
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
                    total += ((double) result.getDuration()) / testRunConfig.getConcurrentClientThreadCount();
                    totalAccumulator += result.getAccumulator();
                    totalSuccess += result.getSuccessCount();
                    totalFailure += result.getFailureCount();
                }

                String resString = "Average Call Duration: " + total / results.size() +
                        " Success: " + totalSuccess +
                        " Failure: " + totalFailure +
                        " AC: " + totalAccumulator;
                System.out.println(resString);
                resultWriter.write(resString);
            }
        }

        resultWriter.close();
    }
}
