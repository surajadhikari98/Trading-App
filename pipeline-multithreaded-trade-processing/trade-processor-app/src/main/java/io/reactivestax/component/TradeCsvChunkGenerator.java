package io.reactivestax.component;

import io.reactivestax.contract.ChunkGenerator;
import io.reactivestax.infra.Infra;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class TradeCsvChunkGenerator implements ChunkGenerator {

    @Override
    public void generateAndSubmitChunks(String filePath, Integer numberOfChunks) throws FileNotFoundException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        int totalLines = lines.size();
        int linesPerChunk = (totalLines - 1) / numberOfChunks; //excluding the header here so doing -1

        String header = lines.get(0);

        ExecutorService executorService = Executors.newSingleThreadExecutor();

        //creating the chunks and submitting to the executorService
        AtomicInteger startLine = new AtomicInteger(1);
        for (int i = 0; i < numberOfChunks; i++) {
            String outputFile = "src/main/java/io/reactivestax/files/" + "trades_chunk_" + (i + 1) + ".csv";
            executorService.submit(() -> {

                try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
                    writer.write(header);
                    writer.newLine();
                    int endLine = Math.min(startLine.get() + linesPerChunk, totalLines); // this ensures that line doesn't read beyond the last-line.
                    for (int j = startLine.get(); j < endLine; j++) {
                        writer.write(lines.get(j));
                        writer.newLine();
                    }
                    startLine.set(endLine);
                    //adding to queue for making the chunk generator and chunk processor decoupled
                    Infra.setChunksFileMappingQueue(outputFile);

                    System.out.println("Created: " + outputFile);

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        executorService.shutdown();
    }

}
