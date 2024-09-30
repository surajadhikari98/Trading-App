package io.reactivestax;


public interface ChunkProcessor {
    void processChunks() throws Exception;
    void writeToTradeQueue(String[] trade) throws InterruptedException;
}
