package io.reactivestax.contract;


import io.reactivestax.domain.Trade;

import java.io.FileNotFoundException;

public interface ChunkProcessor {
    void processChunk() throws Exception;
}
