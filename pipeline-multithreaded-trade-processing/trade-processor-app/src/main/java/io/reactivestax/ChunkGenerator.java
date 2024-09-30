package io.reactivestax;

import java.io.FileNotFoundException;

public interface ChunkGenerator {
    void generateChunk(String filePath) throws FileNotFoundException;
}
