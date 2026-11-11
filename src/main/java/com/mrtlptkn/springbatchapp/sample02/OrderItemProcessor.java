package com.mrtlptkn.springbatchapp.sample02;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Locale;

// Processor RawOrder dan ordera dönüşümü kontrolü yaptığım sınıf.ItemProcessor implemente olmalı.

@Component
@StepScope
public class OrderItemProcessor implements ItemProcessor<RawOrder, Order> {



    @Value("#{jobParameters['failAtId']}") // apiKey JobKey -> securityKey
    private Long failAtId;

    // burası item -> item çalışır

    @Override
    public Order process(RawOrder rawOrder) throws Exception {

        long id;

        try {
            id = Long.parseLong(rawOrder.orderId().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("order_id sayı değil");
        }

        if(rawOrder.customer() == null || rawOrder.customer().isBlank()){
            throw new IllegalArgumentException("müşteri boş");
        }

        BigDecimal amount;

        try{
            amount = BigDecimal.valueOf(Double.parseDouble(rawOrder.amount().trim()));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(e);
        }

        LocalDate orderDate;

        try{
            orderDate = LocalDate.parse(rawOrder.orderDate().trim());
        } catch (Exception e){
            throw new IllegalArgumentException(e);
        }

        // kendimiz bir logic duruma exception fırlatmayı bir jobParametersdan okunan bir değere bağladık.
        if(failAtId != null && failAtId == id){
            throw new IllegalStateException("Kontrollü hata: order_id=" + id);
        }

        return new Order(id, rawOrder.customer().trim(), amount, orderDate);
    }
}
