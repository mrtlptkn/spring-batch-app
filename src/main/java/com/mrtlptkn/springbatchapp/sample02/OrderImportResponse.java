package com.mrtlptkn.springbatchapp.sample02;


// ExecutionContext üzerinden kaç okuma yazma, atlatma vs yaptık

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;

/**
 * Job çalıştırma sonucunun HTTP yanıtı. Csv dosyasında veri tabanına veriyi bulk process yapınca dönen result.
 */

public record OrderImportResponse(
        long jobExecutionId,
        String runDate,
        String status,
        String exitCode,
        long readCount,
        long writeCount,
        long skipCount, // Atalatılan hatalı kayıt sayısı
        long failAtId) { // uygulama csv içerisinde custom exception fırlatacağız orderId değeri

    public static OrderImportResponse  from(JobExecution jobExecution,String runDate,String failAtId){
        long read = 0;
        long write=0;
        long skip = 0;

        for (StepExecution stepExecution : jobExecution.getStepExecutions()) {
            // okuma işlemlerinin balşarılı olması yada atlatılması yada hatalı yazma işlemleri çalışma anında stepExecutionContext'den gelir.
            read += stepExecution.getReadCount();
            write += stepExecution.getWriteCount();
            skip += stepExecution.getReadSkipCount() + stepExecution.getProcessSkipCount() + stepExecution.getWriteSkipCount();
        }

        return  new OrderImportResponse(
                jobExecution.getId(),
                runDate,
                jobExecution.getStatus().toString(),
                jobExecution.getExitStatus().getExitCode(),
                read,
                write,
                skip,
                Long.parseLong(failAtId)
        );

    }

}
