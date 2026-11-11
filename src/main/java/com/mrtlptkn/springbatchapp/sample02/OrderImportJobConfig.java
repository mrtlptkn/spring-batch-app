package com.mrtlptkn.springbatchapp.sample02;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.nio.file.Path;

@Configuration
public class OrderImportJobConfig {

    @Bean
    @StepScope
    // 1.aşama reader tanımı
    public FlatFileItemReader<RawOrder> orderReader(
            @Value("${batch.input-dir}") String inputDir,
            @Value("#{jobParameters['runDate']}") String runDate){

        return new FlatFileItemReaderBuilder<RawOrder>()
                .name("orderReader") // restart adımları için name vermek önemli step hata alabilir yeniden çalıştığında bu name bakıca

                .resource(new FileSystemResource(Path.of(inputDir, "orders-" + runDate + ".csv")))
                .linesToSkip(1) // ilk satırı atlat
                .delimited()
                .names("order_id", "customer", "amount", "order_date")
                .fieldSetMapper(fs -> new RawOrder(
                        fs.readString("order_id"),
                        fs.readString("customer"),
                        fs.readString("amount"),
                        fs.readString("order_date")))
                .build();
    }


    @Bean
    CompositeItemProcessor<RawOrder, Order> orderProcessor(){
        CompositeItemProcessor<RawOrder, Order> processor = new CompositeItemProcessor<>();
        processor.setDelegates(
                java.util.List.of(
                        new OrderItemProcessor()
                        //new OrderValidationProcessor()
                )
        );
        return processor;
    }

    // 2. processor

    // 3.writer tanımı
    // Okunan RawOrder tipinde
    // Yazılan ise Order veritabanına uygun.
    //
    @Bean
    public JdbcBatchItemWriter<Order> orderWriter(DataSource dataSource){
        return  new JdbcBatchItemWriterBuilder<Order>()
                .dataSource(dataSource)
                .sql("INSERT INTO orders (order_id, customer, amount, order_date) " +
                        "VALUES (:orderId, :customer, :amount, :orderDate)")
                .itemSqlParameterSourceProvider(o -> new MapSqlParameterSource()
                        .addValue("orderId", o.orderId())
                        .addValue("customer", o.customer())
                        .addValue("amount", o.amount())
                        .addValue("orderDate", o.orderDate())).build();
    }

    @Bean
    public ItemWriter<Order> orderWriter2(DataSource dataSource){
        return chunk ->  {
            for (Order order : chunk) {
                System.out.println("Writing order: " + order);
            }
        };
    }


    // 4. step tanımı

    @Bean
    public Step orderImportStep(JobRepository jobRepository,
                                PlatformTransactionManager transactionManager,
                                FlatFileItemReader<RawOrder> orderReader,
                                OrderItemProcessor orderItemProcessor,
                                JdbcBatchItemWriter<Order> orderWriter
                                ){
        return new StepBuilder("orderImportStep", jobRepository)
                .<RawOrder, Order>chunk(2, transactionManager)
                .reader(orderReader)
                .processor(orderItemProcessor)
                .writer(orderWriter)
                .build();
    }



    // 5.job tanım
    @Bean
    public Job orderImportJob(JobRepository jobRepository, Step orderImportStep){
        return  new JobBuilder("orderImportJob",jobRepository)
                .start(orderImportStep)
                .build();
    }


}
