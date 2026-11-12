package com.mrtlptkn.springbatchapp.sample03;

import com.mrtlptkn.springbatchapp.sample02.Order;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

// Order okıuyup credit check döndürelim

@Component
@StepScope
public class CreditCheckProcessor implements ItemProcessor<Order, CreditCheck> {

    private final CustomerCreditService creditService;

    public CreditCheckProcessor(CustomerCreditService creditService) {
        this.creditService = creditService;
    }

    @Override
    public CreditCheck process(Order order) {
        // Geçici hata fırlarsa framework bu kaydı retry limitine kadar tekrar işler
        boolean approved = creditService.isApproved(order);
        return new CreditCheck(order.orderId(), order.customer(), approved);
    }
}
