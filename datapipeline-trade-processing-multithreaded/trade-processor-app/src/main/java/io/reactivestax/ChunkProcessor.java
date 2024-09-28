package io.reactivestax;

import java.util.ArrayList;

public interface ChunkProcessor {
    void processChunks() throws Exception;
    void writeToTradeQueue(String[] trade) throws InterruptedException;
}
