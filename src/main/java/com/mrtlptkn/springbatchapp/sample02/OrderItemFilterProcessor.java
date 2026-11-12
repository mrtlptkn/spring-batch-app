package com.mrtlptkn.springbatchapp.sample02;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Locale;

@Component
@StepScope
public class OrderItemFilterProcessor implements ItemProcessor<RawOrder,Order> {

    @Override
    public Order process(RawOrder item) throws Exception {

        if(item.orderId().equals("1007"))
            return null; // filtrelemek yani artık 1007 nolu kayıt process işleminde filtrelendi.

        return new Order(
                Long.parseLong(item.orderId().trim()),
                item.customer().toUpperCase(Locale.ROOT),
                BigDecimal.valueOf(item.amount().trim().isEmpty() ? 0 : Double.parseDouble(item.amount().trim())),
                item.orderDate().trim().isEmpty() ? null : LocalDate.parse(item.orderDate().trim()
        ));
    }
}
