package com.mrtlptkn.springbatchapp.sample04;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OrderImportDecider implements JobExecutionDecider
{
    @Value("${batch.reject-threshold:2}")
    private long rejectThreshold;

    // StepExecution Null geliyor atlatılan kayıtları bulamıyoruz -> Flow devam edemiyor.

    @Override
    public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {

        // Bazı veritabanı servisleri yada api servislerin üzerinden kararları alıacak çağırılar burada yapılarbilir. Biz şuan Spring Batch sağladığı bilgiler ile bir karar aldık.

        // toplamda rejected edilen kayıt sayısı
        long rejected =
                stepExecution.getReadSkipCount() +
                        stepExecution.getProcessSkipCount() +
                        stepExecution.getWriteSkipCount();

        if(rejected > rejectThreshold) {
            return new FlowExecutionStatus("HIGH_REJECT");
        }

        return new FlowExecutionStatus("CONTINUE");
    }
}
