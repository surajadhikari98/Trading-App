package io.reactivestax.contract;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public interface QueueLoader {
    void consumeMessage(String queueName) throws IOException, TimeoutException;
}
