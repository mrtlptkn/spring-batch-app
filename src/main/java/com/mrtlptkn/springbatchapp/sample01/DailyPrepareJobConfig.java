package com.mrtlptkn.springbatchapp.sample01;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class DailyPrepareJobConfig {

    // Step ve Joblar buraya yazılır

    @Bean
    public Step prepareStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,DailyPrepareTasklet taklet){
        return new StepBuilder("prepareDailyStep",jobRepository)
                .tasklet(taklet,transactionManager).build();
    }

    // Job step çalıştırıcı
    @Bean
    public Job dailyPrepareJob(JobRepository jobRepository, Step prepareStep){
        return new JobBuilder("dailyPrepareJob",jobRepository)
                .start(prepareStep).build();
    }
    

}
