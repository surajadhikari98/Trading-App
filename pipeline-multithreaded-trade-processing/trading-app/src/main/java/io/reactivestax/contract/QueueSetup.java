package io.reactivestax.contract;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public interface QueueSetup {
    void publishMessage(String queueName) throws IOException, TimeoutException;
}
