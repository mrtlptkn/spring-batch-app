package com.mrtlptkn.springbatchapp.sample04;

import com.mrtlptkn.springbatchapp.sample03.CreditCheck;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.Flow;
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
    private final  OrderImportDecider orderImportDecider;

    public PipelineJobConfig(AlertTasklet alertTasklet,ReportTasklet reportTasklet, OrderImportDecider orderImportDecider) {
        this.alertTasklet = alertTasklet;
        this.reportTasklet = reportTasklet;
        this.orderImportDecider = orderImportDecider;
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


//    @Bean("pipelineTestJob")
//    public Job pipelineTestJob(JobRepository jobRepository,Step alertStep,Step reportStep){
//
//        SimpleFlow alertFlow = new FlowBuilder<SimpleFlow>("alertFlow")
//                .start(alertStep)
//                .build();
//        SimpleFlow reportFlow = new FlowBuilder<SimpleFlow>("reportFlow")
//                .start(reportStep)
//                .build();
//
//        // İki akışı iki farklı step birbirinden bağımsız olarak çalıştırmak için split kullanıyoruz. Split ile iki farklı akışı aynı anda çalıştırabiliriz.
//        // Yukarıdaki split edilen step chunk-oriented step de olabilir.
//        SimpleFlow splitFlow = new FlowBuilder<SimpleFlow>("splitFlow")
//                .split(pipelineTaskExecutor())
//                .add(alertFlow,reportFlow).build();
//
//
//        return new JobBuilder("pipelineJob",jobRepository)
//                .start(splitFlow).end().build();
//    }


    @Bean
    public Job pipelineJob(JobRepository jobRepository,Step prepareStep,Step orderImportStep, Step creditCheckStep, Step alertStep,Step reportStep,ThreadPoolTaskExecutor pipelineTaskExecutor){

        SimpleFlow creditFlow = new FlowBuilder<SimpleFlow>("creditFlow").start(creditCheckStep).build();

        SimpleFlow reportFlow = new FlowBuilder<SimpleFlow>("reportFlow")
                .start(reportStep)
                .build();

        // eğer decider üzerinde takılmazsak aşağıdaki flow bağlayacağız
        SimpleFlow continueFlow = new FlowBuilder<SimpleFlow>("continueFlow").split(pipelineTaskExecutor)
                .add(creditFlow,reportFlow).build();

        SimpleFlow alertFlow = new FlowBuilder<SimpleFlow>("alertFlow").start(alertStep).build();


        // Not: ayrı bir flow olarak işaretlediğimiz için stepExecution Flow içerisinde kaybettik. Dikkat.
        // Bu sebeple bu kısmı iptal edip bunun yerine aşağıdaki şekilde bağladık
        // start(prepareStep)
        // .next(orderImportStep)
        // .next(orderImportDecider)
        // Job, bir decider'ı çağırırken StepExecution parametresini, bir önceki state doğrudan bir Step ise o step'ten alır. Önceki state bir Flow olduğunda (importFlow),
        //  içindeki step'ler flow'un kendi executor'ında çalışır ve son step execution job seviyesine aktarılmaz. Bu yüzden decider'a null gider.
        // Eğer flow akışını bozmadan decider erişmek istersek o zaman stepExecution nesnesine JobExecution üzerinden erişmemiz gerekir.
        SimpleFlow importFlow = new FlowBuilder<SimpleFlow>("importOrderFlow").start(prepareStep).next(orderImportStep).build();


        return  new JobBuilder("pipelineJob",jobRepository)
                .start(importFlow)
                .next(orderImportDecider)
                .on("HIGH_REJECT").to(alertFlow)
                .from(orderImportDecider) // decider yeniden dönmek için -> en son üst satırda alerflow'da kaldık
                .on("CONTINUE").to(continueFlow)
                .end().build();

        // Direk olarak Step Execution üzerinden erişmek istersek.

//        return  new JobBuilder("pipelineJob",jobRepository)
//                .start(prepareStep)
//                .next(orderImportStep)
//                .next(orderImportDecider)
//                .on("HIGH_REJECT").to(alertFlow)
//                .from(orderImportDecider)
//                .on("CONTINUE").to(continueFlow)
//                .end().build();

    }


}
