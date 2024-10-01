package io.reactivestax.contract;


import java.io.FileNotFoundException;

public interface ChunkProcessor {
    void processChunks() throws Exception;
    void writeToTradeQueue(String[] trade) throws InterruptedException, FileNotFoundException;
}
