package com.mrtlptkn.springbatchapp.sample03;

import com.mrtlptkn.springbatchapp.sample02.Order;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@StepScope
public class CustomerCreditService { // RestTemplate ile bir servisten ilgili kayıtlar için kredi veriliyor mu check yaptığımız düşünelim.

    // kaç kez retry ediyor bilgisi
    private final Map<String,Integer> attemps = new HashMap<>();


    public boolean isApproved(Order order){

        int call = attemps.merge(order.customer(),1,Integer::sum);

        if(order.customer().equals("Ayse Kaya")){
            if(call <=2){ // http isteği ile istek atma api
                throw new TransientServiceException("Kredi servisi zaman aşımı, deneme " + call);
            }
            return  true;
        }

        return  true;

    }

}
