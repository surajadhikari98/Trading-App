package io.reactivestax.contract;


public interface ChunkProcessor {
    void processChunks() throws Exception;
    void writeToTradeQueue(String[] trade) throws InterruptedException;
}
