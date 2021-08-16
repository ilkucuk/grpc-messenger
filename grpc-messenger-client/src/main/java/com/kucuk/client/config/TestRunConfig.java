package com.kucuk.client.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TestRunConfig {
    String caller;
    int blockingCallPeriod;
    int concurrentClientThreadCount;
    int callCountForASingleClient;
    int numberOfRuns;
    int pageSize;

}
