package com.mrtlptkn.springbatchapp.sample03;

import com.mrtlptkn.springbatchapp.sample02.Order;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Map;

@Configuration
public class CreditCheckJobConfig {

    // 1000 -> pageSize(100) -> 10 kez okuma yapar. orderDbReader 10 kez tetiklenir.

    private final CreditCheckProcessor creditCheckProcessor;

    public CreditCheckJobConfig(CreditCheckProcessor creditCheckProcessor) {
        this.creditCheckProcessor = creditCheckProcessor;
    }

    @Bean
    @StepScope
    public JdbcPagingItemReader<Order> orderDbReader(DataSource dataSource){

        return  new JdbcPagingItemReaderBuilder<Order>()
                .name("orderDbReader")
                .dataSource(dataSource)
                .selectClause("SELECT order_id, customer, amount, order_date")
                .fromClause("FROM orders")
                .sortKeys(Map.of("order_id", org.springframework.batch.item.database.Order.ASCENDING))
                .pageSize(5) // her istekde 2 sayfa veritabanından okuyacam
                .rowMapper((rs, rowNum) -> new Order(
                        rs.getLong("order_id"),
                        rs.getString("customer"),
                        rs.getBigDecimal("amount"),
                        rs.getDate("order_date").toLocalDate()))
                .build();
    }

    @Bean
    public ItemWriter<CreditCheck> creditCheckDbWriter(DataSource dataSource){
        return chunk -> {
          System.out.println("creditCheckDbWriter: " + chunk);
        };
    }

    @Bean
    public JdbcBatchItemWriter<CreditCheck> creditCheckWriter(DataSource dataSource){
        return  new JdbcBatchItemWriterBuilder<CreditCheck>().dataSource(dataSource)
                .sql("INSERT INTO order_credit_checks (order_id, customer, approved) " +
                        "VALUES (:orderId, :customer, :approved)")
                .itemSqlParameterSourceProvider(c -> new org.springframework.jdbc.core.namedparam.MapSqlParameterSource()
                        .addValue("orderId", c.orderId())
                        .addValue("customer", c.customer())
                        .addValue("approved", c.approved()))
                .build();
    }


    // 100 kayıt için dbden 10'arlı çekip her bir onluk veriyi 2'li chunklar halinde yaz.
    // her bir 10 luk veri için 5 write tetiklenecek atılacak.

    @Bean
    public Step creditCheckStep(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager,JdbcPagingItemReader<Order> orderDbReader, ItemWriter<CreditCheck> creditCheckDbWriter){

        FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(200); // 200ms retry edecek


        return new StepBuilder("creditCheckStep", jobRepository)
                .<Order, CreditCheck>chunk(2, platformTransactionManager)
                .reader(orderDbReader)
                .processor(creditCheckProcessor)
                .writer(creditCheckDbWriter)
                .faultTolerant()
                .backOffPolicy(backOffPolicy)
                .retry(TransientServiceException.class) // bu gelirse retry et
                .retryLimit(3)
                .build();
    }

    @Bean
    public Job creditCheckJob(JobRepository jobRepository, Step creditCheckStep){
        return new JobBuilder("creditCheckJob",jobRepository)
                .start(creditCheckStep)
                .build();
    }

}
