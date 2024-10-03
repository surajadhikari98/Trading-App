package io.reactivestax.contract;

import java.io.FileNotFoundException;

public interface ChunkProcessor {
    void processChunk() throws FileNotFoundException;
}
