package com.learn.gmall.order.serivce;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.learn.gmall.api.bean.OmsOrderItem;
import com.learn.gmall.api.bean.OmsOrderMessageSendLog;
import com.learn.gmall.order.mapper.OmsOrderMsgMapper;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;

import static com.learn.gmall.order.constant.ConstantIfc.*;

@Component
public class OrderMsgSender {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private OmsOrderMsgMapper omsOrderMsgMapper;

    private final Gson gson = new Gson();

    final RabbitTemplate.ConfirmCallback confirmCallback = (correlationData, ack, message) -> {
        String correlationDataId = correlationData.getId();
        List<OmsOrderItem> omsOrderItemList = gson.fromJson(message, new TypeToken<List<OmsOrderItem>>() {
        }.getType());
        Example example = new Example(OmsOrderMessageSendLog.class);
        example.createCriteria()
                .andEqualTo("memberId", correlationDataId)
                .andEqualTo("messageContent", omsOrderItemList);
        if (ack) {
            //更新订单消息发送表
            omsOrderMsgMapper.updateByExampleSelective(OmsOrderMessageSendLog.builder()
                    .status(SUCCESSFULLY_SENT)
                    .updateTime(LocalDateTime.now())
                    .build(), example);
        } else {
            //更新重试次数, 以及下次发送的时间
            omsOrderMsgMapper.updateByExampleSelective(OmsOrderMessageSendLog.builder()
                    .status(FAIL_TO_SEND)
                    .nextRetryTime(LocalDateTime.now().plusMinutes(RETRY_INTERVAL))
                    .updateTime(LocalDateTime.now())
                    .build(), example);
        }
    };

    @PostConstruct
    private void init() {
        rabbitTemplate.setConfirmCallback(confirmCallback);
    }

    public void sendMessage(List<OmsOrderItem> omsOrderItemList, String memberId) {
        CorrelationData correlationData = new CorrelationData();
        correlationData.setId(memberId);
        rabbitTemplate.convertAndSend("clear_cart_exchange", "clear.cart", omsOrderItemList, correlationData);
    }
}