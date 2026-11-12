package com.mrtlptkn.springbatchapp.sample04;

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

// Pipeline senaryosu:
// HIGH_REJECT -> yüksek öncelikli red edilmesi gereken sipariş işlemleri
// bu tarz durumlar için alert-2026-11-12.txt uyarı durumları


// HIGH_REJECT bir durumda bu step dosyaya alert işleyecek
@Component
@StepScope
public class AlertTasklet implements Tasklet {

    private static  final Logger log =  LoggerFactory.getLogger(AlertTasklet.class);

    @Value("${batch.output-dir}")
    private String outputDir;

    @Value("#{jobParameters['runDate']}")
    private String runDate;


    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

        Path alertFile = Path.of(outputDir + "alert-" + runDate + ".txt");
        Files.createDirectories(alertFile.getParent());
        Files.writeString(alertFile, "Yüksek red oranı: " + runDate + " için kredi kontrolü atlandı" + System.lineSeparator());

        log.warn("Uyarı dosyası yazıldı: {}", alertFile);

        return RepeatStatus.FINISHED;
    }
}
