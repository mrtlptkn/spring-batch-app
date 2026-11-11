package com.mrtlptkn.springbatchapp.sample01;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

// Bizim senaryoda önceki günkü sipariş dosyasının var olup olmadığını kontrol eder doğrulama
// arşiv dosyasına eski kayıt archievle

// Not: Taskletler bizim için bir step.

@Component
@StepScope // her çağırıda kendi scope'unu aç
public class DailyPrepareTasklet implements Tasklet {

    private static final Logger log = LoggerFactory.getLogger(DailyPrepareTasklet.class);

    @Value("${batch.input-dir}")
    private String inputDir;

    @Value("#{jobParameters['runDate']}")
    private String runDate;

    @Value("${batch.output-dir}")
    private String outputDir;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

        // Bugünün dosyası sipariş dosyası dizinde var mı kontrol

        Path expectedInput = Path.of(inputDir, "orders-"+ runDate +".csv");

        if(Files.exists(expectedInput)){
            System.out.println("Girdi dosyası doğrulandı: " + expectedInput);
        }else{
            // Exception üretince Step Failed Job da Failed olur.
            // Yeniden restart edebilmek için bunu spring batch otomatik anlaması exception fırlattık.
            throw new IllegalStateException("Girdi dosyası bulunamadı: " + expectedInput.toAbsolutePath());
        }

        // Eski dosyası arşive taşı
        Path output = Path.of(outputDir);
        Path dayArchive = Path.of(outputDir, runDate); // output/2026-11-11
        Files.createDirectories(output);
        Files.createDirectories(dayArchive);

        List<Path> oldFiles;

        try(Stream<Path> files = Files.list(output)){
            oldFiles = files.filter(Files::isRegularFile).toList();
        }

        for (Path oldFile : oldFiles) {
            Path target = dayArchive.resolve(oldFile.getFileName()); // orders-2026-11-11.csv
            Files.move(oldFile, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            log.info("Eski dosya arşive taşındı: {} -> {}", oldFile, target);
        }

        return RepeatStatus.FINISHED; // Job completed olur.
    }
}
