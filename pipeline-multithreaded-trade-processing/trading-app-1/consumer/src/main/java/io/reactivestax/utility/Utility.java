package io.reactivestax.utility;

import io.reactivestax.types.dto.Trade;

import java.util.concurrent.atomic.AtomicInteger;

public class Utility {

    public static AtomicInteger roundRobinIndex = new AtomicInteger(0);

    public static boolean checkValidity(String[] split) {
        return (split[0] != null && split[1] != null && split[2] != null && split[3] != null && split[4] != null && split[5] != null);
    }

    public static Trade prepareTrade(String payload) {
        String[] payloads = payload.split(",");
        return new Trade(payloads[0],
                payloads[1],
                payloads[2],
                payloads[3],
                payloads[4],
                Integer.parseInt(payloads[5]),
                Double.parseDouble(payloads[6]),
                Integer.parseInt(payloads[5]));
    }


}
