package com.kucuk.client.callers.messageServiceCaller;

import com.kucuk.client.callers.CallResult;

import java.util.concurrent.Callable;

public interface MessageServiceCaller extends Callable<CallResult> {
    @Override
    CallResult call() throws Exception;
}


