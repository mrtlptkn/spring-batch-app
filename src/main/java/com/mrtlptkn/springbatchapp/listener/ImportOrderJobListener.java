package com.mrtlptkn.springbatchapp.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.stereotype.Component;

@Component
@JobScope
public class ImportOrderJobListener implements JobExecutionListener {
    private static final Logger log = LoggerFactory.getLogger(ImportOrderJobListener.class);

    @Override
    public void beforeJob(org.springframework.batch.core.JobExecution jobExecution) {
        log.info("Job başladı: name=" + jobExecution.getJobInstance().getJobName() +
                ", params=" + jobExecution.getJobParameters());
    }

    @Override
    public void afterJob(org.springframework.batch.core.JobExecution jobExecution) {
        log.warn("Job bitti: status=" + jobExecution.getStatus() +
                ", exitCode=" + jobExecution.getExitStatus().getExitCode());

        if (jobExecution.getStatus() == BatchStatus.FAILED) {
            jobExecution.getAllFailureExceptions()
                    .forEach(ex ->  log.error("Hata nedeni: " + ex.getMessage()));
        }
    }
}
