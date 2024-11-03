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

    public static boolean checkValidity(String[] split) {
        return (split[0] != null && split[1] != null && split[2] != null && split[3] != null && split[4] != null && split[5] != null);
    }

}
