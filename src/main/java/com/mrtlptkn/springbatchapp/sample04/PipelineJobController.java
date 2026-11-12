package com.mrtlptkn.springbatchapp.sample04;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pipeline-job")
public class PipelineJobController {


    private final Job pipelineJob;
    private final JobLauncher jobLauncher;

    public PipelineJobController(Job pipelineJob, JobLauncher jobLauncher) {
        this.pipelineJob = pipelineJob;
        this.jobLauncher = jobLauncher;
    }

    @PostMapping("/run")
    public ResponseEntity<String> runJob(
            @RequestParam String runDate
    ) throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        // Burada Spring Batch job'unu başlatmak için gerekli kodu ekleyebilirsiniz.
        // Örneğin, JobLauncher kullanarak job'u çalıştırabilirsiniz.

        var params = new JobParametersBuilder()
                .addString("runDate", runDate)
                .toJobParameters();

        JobExecution jobExecution = jobLauncher.run(pipelineJob, params);


        return ResponseEntity.ok("Ok");
    }


}
