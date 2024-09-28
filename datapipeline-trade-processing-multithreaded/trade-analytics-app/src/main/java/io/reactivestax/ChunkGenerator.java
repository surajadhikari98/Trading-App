package problems.thread.distributedtrade;

import java.io.FileNotFoundException;

public interface ChunkGenerator {
    void generateChunk(String filePath) throws FileNotFoundException;
}
