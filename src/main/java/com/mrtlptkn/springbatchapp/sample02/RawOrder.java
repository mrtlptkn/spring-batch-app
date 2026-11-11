package com.mrtlptkn.springbatchapp.sample02;

public record RawOrder(String orderId,String customer,String amount,String orderDate) {

    public String toCsv(){
        return String.join(",",orderId,customer,amount,orderDate);
    }
}
