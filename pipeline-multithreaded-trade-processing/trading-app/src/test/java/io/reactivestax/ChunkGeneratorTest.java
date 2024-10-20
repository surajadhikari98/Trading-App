package io.reactivestax;

import io.reactivestax.service.TradeCsvChunkGenerator;
import io.reactivestax.infra.Infra;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.util.Optional;

import static org.junit.Assert.assertEquals;

public class ChunkGeneratorTest {
   @Test
    public void testGenerateAndSubmitChunks() throws FileNotFoundException {
       String filePath =  Infra.readFromApplicationPropertiesStringFormat("trade.file.path");
       int numberOfChunks = Infra.readFromApplicationPropertiesIntegerFormat("number.chunks");
      Integer fileCount = new TradeCsvChunkGenerator().generateAndSubmitChunks(filePath, numberOfChunks);
      assertEquals(Optional.ofNullable(fileCount), Optional.of(numberOfChunks));
   }
}
