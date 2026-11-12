package com.mrtlptkn.springbatchapp.listener;

import com.mrtlptkn.springbatchapp.sample02.RawOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ItemReadListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.stereotype.Component;

@Component
@StepScope
public class ImportJobItemReaderListener implements ItemReadListener<RawOrder> {

    private static final Logger log = LoggerFactory.getLogger(ImportJobItemReaderListener.class);

    @Override
    public void onReadError(Exception ex) {
        log.error("Okuma sırasında hata oluştu: {}", ex.getMessage(), ex);
        ItemReadListener.super.onReadError(ex);
    }
}
