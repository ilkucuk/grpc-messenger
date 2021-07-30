package com.kucuk.client;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Slf4j
public class GrpcClient {

    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {

        int sleepPeriod = 10;
        int threadCount = 10;
        int callCount = 200;
        int loop = 3;
        int messageCount = 100;


        if (args.length > 0) {
            sleepPeriod = Integer.parseInt(args[0]);
        }
        if (args.length > 1) {
            threadCount = Integer.parseInt(args[1]);
        }
        if (args.length > 2) {
            callCount = Integer.parseInt(args[2]);
        }
        if (args.length > 3) {
            loop = Integer.parseInt(args[3]);
        }
        if (args.length > 4) {
            messageCount = Integer.parseInt(args[4]);
        }

        System.out.println(" MessageCount: " + messageCount + " SleepPeriod: " + sleepPeriod + " ThreadCount: " + threadCount + " CallCount: " + callCount+ " loop: " + loop);

        File ca = new File("../cert/ca.crt");
        if (!ca.exists()) {
            System.out.println("Cert chain error!");
            return;
        }

        String author = "ikucuk@gmail.com";
        String title = "Sample Message Title";
        String content = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";
        ResultWriter resultWriter = new ResultWriter("testRun_" + Instant.now().toEpochMilli() +".txt");
        resultWriter.write("---New Test Run----");
        resultWriter.write(" MessageCount: " + messageCount + "SleepPeriod: " + sleepPeriod + " ThreadCount: " + threadCount + " CallCount: " + callCount+ " loop: " + loop);
        for(int j=0; j<loop; j++) {
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            List<Future<MessageServiceCaller.CallResult>> results = new ArrayList<>(threadCount);

            for (int i = 0; i < threadCount; i++) {
                MessageServiceCaller caller = new MessageServiceCaller(callCount, "kucuk.com", 443, 12345L, author, title, content, sleepPeriod, ca, messageCount);
                Future<MessageServiceCaller.CallResult> callResultFuture = executor.submit(caller);
                results.add(callResultFuture);
            }
            executor.shutdown();
            if (executor.awaitTermination(1800, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }

            double total = 0;
            int totalAccumulator = 0;
            int totalSuccess = 0;
            int totalFailure = 0;
            for (Future<MessageServiceCaller.CallResult> resultFuture : results) {
                MessageServiceCaller.CallResult result = resultFuture.get();
                total += ((double) result.getDuration()) / callCount;
                totalAccumulator += result.getAccumulator();
                totalSuccess+= result.getSuccessCount();
                totalFailure+= result.getFailureCount();
            }
            String resString = "Average Call Duration: " + total / results.size() + " Success: " + totalSuccess + " Failure: " + totalFailure + " AC: "+ totalAccumulator;
            System.out.println(resString);
            resultWriter.write(resString);
        }

        resultWriter.close();
    }
}
