package com.mrtlptkn.springbatchapp.sample03;

import com.mrtlptkn.springbatchapp.sample02.Order;
import org.springframework.batch.core.ItemReadListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.stereotype.Component;

@Component
@StepScope
public class CheckCreditItemReaderListener implements ItemReadListener<Order> {

    @Override
    public void beforeRead() {
        ItemReadListener.super.beforeRead();
    }
}
