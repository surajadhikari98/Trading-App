package io.reactivestax;

import io.reactivestax.service.ChunkGeneratorService;
import io.reactivestax.factory.BeanFactory;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.util.Optional;

import static org.junit.Assert.assertEquals;

public class ChunkGeneratorTest {
   @Test
    public void testGenerateAndSubmitChunks() throws FileNotFoundException {
       String filePath =  BeanFactory.readFromApplicationPropertiesStringFormat("trade.file.path");
       int numberOfChunks = BeanFactory.readFromApplicationPropertiesIntegerFormat("number.chunks");
      Integer fileCount = new ChunkGeneratorService().generateAndSubmitChunks(filePath, numberOfChunks);
      assertEquals(Optional.ofNullable(fileCount), Optional.of(numberOfChunks));
   }
}
