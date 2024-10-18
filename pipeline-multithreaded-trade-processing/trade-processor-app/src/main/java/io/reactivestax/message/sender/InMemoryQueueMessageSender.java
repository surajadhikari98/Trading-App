package io.reactivestax.message.sender;

import io.reactivestax.contract.MessageSender;
import io.reactivestax.infra.Infra;

import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;

public class InMemoryQueueMessageSender implements MessageSender {
    List<LinkedBlockingDeque<String>> queueTracker;

    private static InMemoryQueueMessageSender instance;

    public static synchronized InMemoryQueueMessageSender getInstance(){
        if(instance == null) {
            instance = new InMemoryQueueMessageSender();
        }
        return  instance;
    }

    @Override
    public Boolean sendMessageToQueue(String queueName, String message) throws InterruptedException {
        int queueIndex = Integer.parseInt(queueName.split("(?<=\\D)(?=\\d)")[1]);
        Infra.getQUEUE_LIST().get(queueIndex).put(message);
        return true;
    }
}
