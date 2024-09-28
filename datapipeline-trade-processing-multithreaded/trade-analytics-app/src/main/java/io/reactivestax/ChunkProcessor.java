package problems.thread.distributedtrade;

public interface ChunkProcessor {
    void processChunks() throws Exception;
    void writeToTradeQueue(String[] trade) throws InterruptedException;
}
