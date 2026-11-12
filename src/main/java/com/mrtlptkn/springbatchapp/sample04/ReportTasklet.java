package com.mrtlptkn.springbatchapp.sample04;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@StepScope
@Component
public class ReportTasklet implements Tasklet{

    private final JdbcTemplate jdbcTemplate;

    public ReportTasklet(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        System.out.println("ReportTasklet çalıştı");

        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM orders", Long.class);
        BigDecimal total = jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(amount), 0) FROM orders", BigDecimal.class);
        System.out.println("Rapor: " + count + " sipariş, toplam tutar " + total);


        // return RepeatStatus.valueOf("FINISHED");

        return RepeatStatus.FINISHED;
    }
}
