package io.reactivestax;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class TradeCsvChunkGenerator implements ChunkGenerator {

    static int numberOfChunks = 10;

    public void generateChunk(String filePath) throws FileNotFoundException {
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
            String outputFile = "/Users/Suraj.Adhikari/sources/student-mode-programs/suad-bootcamp-2024/datapipeline-trade-processing-multithreaded/trade-processor-app/src/main/java/io/reactivestax/files/" + "trades_chunk_" + (i + 1) + ".csv";
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
                    System.out.println("Created: " + outputFile);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        executorService.shutdown();
    }
}
