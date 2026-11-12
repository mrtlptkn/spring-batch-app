package com.mrtlptkn.springbatchapp.sample02;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Locale;

// Bug ->  CompositeItemProcessor delegate'leri sırayla zincirler. Bir delegate'in çıktısı bir sonrakinin girişine gider: RawOrder verdiğimiz için burada okuma hatası almıştık.
// RawOrder --[OrderItemProcessor]--> Order --[OrderItemFilterProcessor]--> ???

@Component
@StepScope
public class OrderItemFilterProcessor implements ItemProcessor<Order,Order> {

    @Override
    public Order process(Order item) throws Exception {

        if(item.orderId() == 1007L)
            return null; // filtrelemek yani artık 1007 nolu kayıt process işleminde filtrelendi.

        return item;
    }
}
