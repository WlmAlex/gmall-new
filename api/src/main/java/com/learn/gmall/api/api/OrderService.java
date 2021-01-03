package com.learn.gmall.api.api;

import com.learn.gmall.api.bean.OmsOrder;

import java.math.BigDecimal;

public interface OrderService {
    String generateTradeCode(String memberId);


    boolean checkTradeCode(String memberId, String tradeCode);


    void saveOrder(OmsOrder omsOrder);
}
