package io.reactivestax.dlq;

import com.rabbitmq.client.CancelCallback;

import java.io.IOException;

public class CustomCancelCallback implements CancelCallback {

    @Override
    public void handle(String consumerTag) throws IOException {
        System.out.println(" [x] Consumer '" + consumerTag + "' cancelled.");
    }
}
