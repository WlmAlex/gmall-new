package com.learn.gmall.order.task;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.learn.gmall.api.bean.OmsOrderItem;
import com.learn.gmall.api.bean.OmsOrderMessageSendLog;
import com.learn.gmall.order.mapper.OmsOrderMsgMapper;
import com.learn.gmall.order.serivce.OrderMsgSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@EnableScheduling
public class OrderMsgSenderTask {

    @Autowired
    private OrderMsgSender orderMsgSender;

    @Autowired
    private OmsOrderMsgMapper omsOrderMsgMapper;

    private final Gson gson = new Gson();

    @Scheduled(fixedDelay = 5000, initialDelay = 10000)
    public void resendOmsOrderMessage() {
        System.err.println("開始omsOrder定時發送任務: " + LocalDateTime.now());
        //find messages that need to resend from db
        List<OmsOrderMessageSendLog> omsOrderMessageSendLogList = omsOrderMsgMapper.selectStatus2AndIsAliveMsg();
        Optional.of(omsOrderMessageSendLogList)
                .ifPresent(omsOrderMessageLogs -> omsOrderMessageLogs.stream()
                        .forEach(omsOrderMessageSendLog -> {
                            Integer retryCount = omsOrderMessageSendLog.getRetryCount();
                            if (retryCount >= 3) {
                                //mark message status to dead, waiting to handle by manual.
                                omsOrderMessageSendLog.setIsAlive(false);
                                omsOrderMessageSendLog.setUpdateTime(LocalDateTime.now());
                                omsOrderMsgMapper.updateByPrimaryKey(omsOrderMessageSendLog);
                            } else {
                                //resend message
                                List<OmsOrderItem> omsOrderItemList = gson.fromJson(omsOrderMessageSendLog.getMessageContent(), new TypeToken<List<OmsOrderItem>>() {
                                }.getType());
                                orderMsgSender.sendMessage(omsOrderItemList, omsOrderMessageSendLog.getMemberId());
                            }
                        }));
    }
}