package io.reactivestax;

import io.reactivestax.component.TradeCsvChunkGenerator;
import io.reactivestax.infra.Infra;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.util.Optional;

import static org.junit.Assert.assertEquals;

public class ChunkGeneratorTest {
   @Test
    public void testGenerateAndSubmitChunks() throws FileNotFoundException {
       String filePath =  Infra.readFromApplicationPropertiesStringFormat("tradeFilePath");
       int numberOfChunks = Infra.readFromApplicationPropertiesIntegerFormat("numberOfChunks");
      Integer fileCount = new TradeCsvChunkGenerator().generateAndSubmitChunks(filePath, numberOfChunks);
      assertEquals(Optional.ofNullable(fileCount), Optional.of(numberOfChunks));
   }
}
