package com.kucuk.server.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class MessageServiceConfig {
    String certFilePath;
    String keyFilePath;
    String trustStorePath;
    int port;
}
