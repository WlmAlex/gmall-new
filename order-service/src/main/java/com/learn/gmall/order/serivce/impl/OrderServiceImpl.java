package com.learn.gmall.order.serivce.impl;

import com.google.gson.Gson;
import com.learn.gmall.api.api.OrderService;
import com.learn.gmall.api.bean.OmsOrder;
import com.learn.gmall.api.bean.OmsOrderMessageSendLog;
import com.learn.gmall.order.mapper.OmsOrderItemMapper;
import com.learn.gmall.order.mapper.OmsOrderMapper;
import com.learn.gmall.order.mapper.OmsOrderMsgMapper;
import com.learn.gmall.order.serivce.OrderMsgSender;
import com.learn.gmall.util.RedisUtil;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

import static com.learn.gmall.order.constant.ConstantIfc.WAITING_TO_SEND;
import static com.learn.gmall.order.constant.ConstantIfc.trade_code_key;

@DubboService
public class OrderServiceImpl implements OrderService {

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private OmsOrderMapper omsOrderMapper;

    @Autowired
    private OmsOrderItemMapper omsOrderItemMapper;

    @Autowired
    private OmsOrderMsgMapper omsOrderMsgMapper;

    @Autowired
    private OrderMsgSender orderMsgSender;

    @Override
    public String generateTradeCode(String memberId) {
        try (Jedis jedis = redisUtil.getJedis()) {
            String tradeCode = UUID.randomUUID().toString();
            String tradeCodeKey = String.format(trade_code_key, memberId);
            jedis.setex(tradeCodeKey, 60 * 15, tradeCode);
            return tradeCode;
        }
    }

    @Override
    public boolean checkTradeCode(String memberId, String tradeCode) {
        try (Jedis jedis = redisUtil.getJedis()) {
            String tradeCodeKey = String.format(trade_code_key, memberId);
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then redis.call('del',KEYS[1]) else return 0 end";
            String result = String.valueOf(jedis.eval(script, Collections.singletonList(tradeCodeKey), Collections.singletonList(tradeCode)));
            if ("0".equals(result)) {
                return false;
            }
            return true;
        }
    }

    @Override
    @Transactional
    public void saveOrder(OmsOrder omsOrder) {

        omsOrderMapper.insertSelective(omsOrder);
        omsOrderItemMapper.saveOrderItemList(omsOrder);

        omsOrderMsgMapper.insertSelective(OmsOrderMessageSendLog.builder()
                .messageContent(new Gson().toJson(omsOrder.getOmsOrderItems()))
                .createTime(LocalDateTime.now())
                .isAlive(true)
                .memberId(omsOrder.getMemberId())
                .status(WAITING_TO_SEND)
                .build());

        //send messages to clear cart
        orderMsgSender.sendMessage(omsOrder.getOmsOrderItems(), omsOrder.getMemberId());
    }
}