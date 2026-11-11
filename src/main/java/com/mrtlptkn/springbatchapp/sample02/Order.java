package com.mrtlptkn.springbatchapp.sample02;

import java.math.BigDecimal;
import java.time.LocalDate;

// veri tabanın orders tablosuna yazılacak entity nesnei
public record Order(long orderId, String customer, BigDecimal amount, LocalDate orderDate) {
}
