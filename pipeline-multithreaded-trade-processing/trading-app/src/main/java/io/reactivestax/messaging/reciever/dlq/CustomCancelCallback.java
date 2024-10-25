package io.reactivestax.messaging.reciever.dlq;

import com.rabbitmq.client.CancelCallback;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class CustomCancelCallback implements CancelCallback {

    @Override
    public void handle(String consumerTag) throws IOException {
        log.info(" [x] Consumer ' {} cancelled." , consumerTag);
    }
}
