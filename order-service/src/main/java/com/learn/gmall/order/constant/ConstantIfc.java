package com.learn.gmall.order.constant;

public interface ConstantIfc {

    String trade_code_key = "user:%s:tradeCode";

    String WAITING_TO_SEND = "0";

    String SUCCESSFULLY_SENT = "1";

    String FAIL_TO_SEND = "2";

    Integer RETRY_INTERVAL = 1;
}
