package com.kucuk.client.resultWriters;

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.FileAppender;


// Logback based customLogWriter to capture the results for each call made by the grpc-client
public class CustomCallLogWriter {

    // the column names separated by comma.
    private final static String CALL_LOG_HEADER = "timeStamp,elapsed,label,responseCode,responseMessage,threadName,dataType,success,failureMessage,bytes,sentBytes,grpThreads,allThreads,Latency,IdleTime,Connect";
    private final String LOG_ENTRY_FORMATTER = "%s,%s,label,responseCode,responseMsg,threadName,dataType,%s,failureMsg,0,0,0,0,0,0,0";

    private Logger logger;
    private FileAppender<ch.qos.logback.classic.spi.ILoggingEvent> fileAppender;
    private AsyncAppender asyncAppender;
    private PatternLayoutEncoder encoder;

    public CustomCallLogWriter(String fileName, LoggerContext loggerContext) {
        fileAppender = new FileAppender<>();
        fileAppender.setContext(loggerContext);
        fileAppender.setName("FILE");
        // set the file name
        fileAppender.setFile(fileName);

        encoder = new PatternLayoutEncoder();
        encoder.setContext(loggerContext);
        encoder.setPattern("%msg%n");
        encoder.start();

        fileAppender.setEncoder(encoder);
        fileAppender.start();

        asyncAppender = new AsyncAppender();
        asyncAppender.setContext(loggerContext);
        asyncAppender.setName("ASYNC");
        asyncAppender.addAppender(fileAppender);
        asyncAppender.start();

        // attach the AsyncAppender to the logger of your choice
        logger = loggerContext.getLogger("grpc-messenger-client");
        logger.addAppender(asyncAppender);
        // Skip printing logs to console.
        logger.setAdditive(false);
        logger.info(CALL_LOG_HEADER);
    }

    public void write(ClientCallLogEntry logEntry) {
        String logLine = String.format(LOG_ENTRY_FORMATTER, logEntry.getTimeStamp(), logEntry.getDuration(), logEntry.isSuccess());
        logger.info(logLine);
    }

    public void stop() {
        asyncAppender.stop();
        fileAppender.stop();
        encoder.stop();
    }


}
