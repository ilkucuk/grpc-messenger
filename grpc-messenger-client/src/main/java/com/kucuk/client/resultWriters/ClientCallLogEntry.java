package com.kucuk.client.resultWriters;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ClientCallLogEntry {
    private final long duration;
    private final boolean success;
    private final long timeStamp;
}
