package com.mrtlptkn.springbatchapp.sample01;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;


// batch.run-date=2026-11-07 -> Job o günkü parametreyi vermek için kullanıcağım
// mvn spring-boot:run -Dspring-boot.run.arguments="--batch.run=daily-prepare --batch.run-date=2026-11-11"
// mvn spring-boot:run -Dspring-boot.run.arguments="--batch.run=daily-prepare --batch.run-date=2026-11-11"
// mvn spring-boot:run -Dspring-boot.run.arguments="--batch.run=daily-prepare --batch.run-date=2026-11-11"
@Component
@ConditionalOnProperty(name = "batch.run", havingValue = "daily-prepare")
public class DailyPrepareCommandRunner implements CommandLineRunner {

    private static  final Logger log = LoggerFactory.getLogger(DailyPrepareCommandRunner.class);
    private static  final ZoneId ISTANBUL = ZoneId.of("Europe/Istanbul");


    private final JobLauncher jobLauncher;
    private final Job dailyPrepareJob;
    private final String runDateProp;

    public DailyPrepareCommandRunner(JobLauncher jobLauncher,
                                     @Qualifier("dailyPrepareJob")  Job dailyPrepareJob,
                                     @Value("${batch.run-date:}") String runDateProp) {
        this.jobLauncher = jobLauncher;
        this.dailyPrepareJob = dailyPrepareJob;
        this.runDateProp = runDateProp;
    }

    @Override
    public void run(String... args) throws Exception {
        String runDate = runDateProp.isBlank() ? LocalDate.now(ISTANBUL).toString()  : runDateProp;


        var params = new JobParametersBuilder().addString("runDate", runDate).toJobParameters();

        JobExecution execution =  jobLauncher.run(dailyPrepareJob, params);
        log.info("Job çalıştırıldı: runDate={}", runDate, execution.getStatus());

    }
}
