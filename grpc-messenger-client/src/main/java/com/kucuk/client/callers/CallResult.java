package com.kucuk.client.callers;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class CallResult {
    private final int successCount;
    private final int failureCount;
    private final long duration;
    private final long accumulator;
}
