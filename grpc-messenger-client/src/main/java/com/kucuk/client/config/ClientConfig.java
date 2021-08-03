package com.kucuk.client.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@ToString
@Setter
@Getter
public class ClientConfig {
    List<TestRunConfig> testRunConfigs;
}
