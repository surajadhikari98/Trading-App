package io.reactivestax.utils;

import io.reactivestax.infra.Infra;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class Utility {

    public static AtomicInteger roundRobinIndex = new AtomicInteger(0);
      static int numberOfQueues = Infra.readFromApplicationPropertiesIntegerFormat("numberOfQueues");

      public static int roundRobin(){
       return roundRobinIndex.incrementAndGet() % numberOfQueues + 1;
    }

    public synchronized static int random(){
       return ThreadLocalRandom.current().nextInt(1, numberOfQueues + 1);
    }
}
