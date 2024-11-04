package io.reactivestax.service;

import io.reactivestax.types.contract.ChunkGenerator;
import io.reactivestax.factory.BeanFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static io.reactivestax.factory.BeanFactory.readFromApplicationPropertiesStringFormat;

@Slf4j
public class ChunkGeneratorService implements ChunkGenerator {

    @Override
    public Integer generateAndSubmitChunks(String filePath, Integer numberOfChunks) throws FileNotFoundException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            log.info("**chunks** {}", e.getMessage());
        }
        int totalLines = lines.size();
        int linesPerChunk = (totalLines - 1) / numberOfChunks; //excluding the header here so doing -1

        String header = lines.get(0);
        generateChunks(numberOfChunks, header, linesPerChunk, totalLines, lines);
        return numberOfChunks;
    }


    private static void generateChunks(Integer numberOfChunks, String header, int linesPerChunk, int totalLines, List<String> lines) throws FileNotFoundException {
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        //creating the chunks and submitting to the executorService
        AtomicInteger startLine = new AtomicInteger(1);
        for (int i = 0; i < numberOfChunks; i++) {
            String outputFile = readFromApplicationPropertiesStringFormat("chunks.file.path") + "trades_chunk_" + (i + 1) + ".csv";
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
                    BeanFactory.setChunksFileMappingQueue(outputFile);
                    log.info("Created  {}", outputFile);
                } catch (IOException e) {
                    log.info("Error in chunks generation {}", e.getMessage());
                }
            });
        }
        executorService.shutdown();
    }
}
