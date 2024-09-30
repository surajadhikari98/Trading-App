package io.reactivestax.components;

import java.io.FileNotFoundException;
import java.util.Map;

public interface ChunkGenerator {
    Map<String, Boolean> generateAndSubmitChunks (String filePath, Integer numberOfChunks) throws FileNotFoundException;
}
