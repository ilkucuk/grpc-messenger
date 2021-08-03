package com.kucuk.client.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TestRunConfig {
    int blockingCallPeriod;
    int concurrentClientThreadCount;
    int callCountForASingleClient;
    int numberOfRuns;
    int pageSize;
    String caller;

}
