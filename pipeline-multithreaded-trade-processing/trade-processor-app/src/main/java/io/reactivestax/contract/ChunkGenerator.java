package io.reactivestax.contract;

import java.io.FileNotFoundException;
import java.util.Map;

public interface ChunkGenerator {
    void generateAndSubmitChunks(String filePath, Integer numberOfChunks) throws FileNotFoundException;
}
