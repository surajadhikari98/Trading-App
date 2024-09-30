package io.reactivestax.components;

import java.io.FileNotFoundException;

public interface ChunkProcessor {
    Boolean processChunk(String chunkFilePath) throws FileNotFoundException;
}
