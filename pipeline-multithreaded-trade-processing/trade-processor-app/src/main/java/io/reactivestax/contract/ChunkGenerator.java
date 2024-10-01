package io.reactivestax.contract;

import java.io.FileNotFoundException;

public interface ChunkGenerator {
    void generateChunk(String filePath) throws FileNotFoundException, RuntimeException;
}
