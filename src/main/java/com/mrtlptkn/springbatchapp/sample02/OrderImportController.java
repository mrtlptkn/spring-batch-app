package com.mrtlptkn.springbatchapp.sample02;

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
@RequestMapping("/api/v1/order-import")
public class OrderImportController {

    private final Job orderImportJob;
    private final JobLauncher jobLauncher;

    public OrderImportController(Job orderImportJob,JobLauncher jobLauncher) {
        this.orderImportJob = orderImportJob;
        this.jobLauncher = jobLauncher;
    }

    @PostMapping("/run")
    public ResponseEntity<OrderImportResponse> runJob(
            @RequestParam String runDate,
            @RequestParam(defaultValue = "0") long failAtId
    ) throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        // Burada Spring Batch job'unu başlatmak için gerekli kodu ekleyebilirsiniz.
        // Örneğin, JobLauncher kullanarak job'u çalıştırabilirsiniz.

        var params = new JobParametersBuilder()
                .addString("runDate", runDate)
                .addLong("failAtId", failAtId, false)
                .toJobParameters();

        JobExecution jobExecution =  jobLauncher.run(orderImportJob,params);

        var response =  OrderImportResponse.from(jobExecution, runDate, String.valueOf(failAtId));

        return ResponseEntity.ok(response);
    }


}
