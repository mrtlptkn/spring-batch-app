package com.mrtlptkn.springbatchapp.sample04;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.support.SimpleFlow;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class PipelineJobConfig {

    private final AlertTasklet alertTasklet;
    private final ReportTasklet reportTasklet;

    public PipelineJobConfig(AlertTasklet alertTasklet,ReportTasklet reportTasklet) {
        this.alertTasklet = alertTasklet;
        this.reportTasklet = reportTasklet;
    }


    // iki farklı işi birbirinden split edip ayırcağız -> step 1 ayrı step 2 ayrı logic ayrı işler bu işler normal mantıkta sıralı olarak yapar. SyncTask
    @Bean
    public ThreadPoolTaskExecutor pipelineTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2); // Thread pool üzerinde işlemi 2 ayrı thread olarak işlemek
        executor.setMaxPoolSize(2); // Thread pool üzeirden maksimum kullanabileceği kapasite
        executor.setThreadNamePrefix("pipeline-");
        executor.initialize();
        return executor;
    }

    // Karar verirken belirli eşik değerleri geçilence alert olarak kayıt atalım.
    @Bean
    public Step alertStep(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager){
        return new StepBuilder("alertStep",jobRepository).tasklet(alertTasklet,platformTransactionManager).build();
    }

    @Bean
    public Step reportStep(JobRepository jobRepository,PlatformTransactionManager platformTransactionManager){
        return new StepBuilder("reportStep",jobRepository).tasklet(reportTasklet,platformTransactionManager).build();
    }


    @Bean
    public Job pipelineJob(JobRepository jobRepository,Step alertStep,Step reportStep){

        SimpleFlow alertFlow = new FlowBuilder<SimpleFlow>("alertFlow")
                .start(alertStep)
                .build();
        SimpleFlow reportFlow = new FlowBuilder<SimpleFlow>("reportFlow")
                .start(reportStep)
                .build();

        // İki akışı iki farklı step birbirinden bağımsız olarak çalıştırmak için split kullanıyoruz. Split ile iki farklı akışı aynı anda çalıştırabiliriz.
        // Yukarıdaki split edilen step chunk-oriented step de olabilir.
        SimpleFlow splitFlow = new FlowBuilder<SimpleFlow>("splitFlow")
                .split(pipelineTaskExecutor())
                .add(alertFlow,reportFlow).build();


        return new JobBuilder("pipelineJob",jobRepository)
                .start(splitFlow).end().build();
    }


}
