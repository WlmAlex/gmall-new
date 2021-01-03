package com.learn.gmall.manage.component;

import com.learn.gmall.api.bean.PmsSkuInfo;
import com.learn.gmall.api.bean.PmsSkuMessageLog;
import com.learn.gmall.manage.mapper.PmsSkuMessageLogMapper;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;

import static com.learn.gmall.manage.constant.ConstantsIfc.*;

@Component
public class PmsSkuInfoSender {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private PmsSkuMessageLogMapper pmsSkuMessageLogMapper;

    final RabbitTemplate.ConfirmCallback confirmCallback = (correlationData, ack, message) -> {
        String correlationDataId = correlationData.getId();
        PmsSkuMessageLog pmsSkuMessageLogFromDb = pmsSkuMessageLogMapper.selectOne(PmsSkuMessageLog.builder()
                .skuId(Integer.parseInt(correlationDataId))
                .build());

        LocalDateTime now = LocalDateTime.now();
        if (ack) {
            //成功发送消息到mq-server
            pmsSkuMessageLogFromDb.setStatus(SENT_SUCCESSFULLY);
            pmsSkuMessageLogFromDb.setUpdateTime(now);
        } else {
            //发送消息到server失败
            pmsSkuMessageLogFromDb.setStatus(SENT_FAIL);
            pmsSkuMessageLogFromDb.setRetryCount(pmsSkuMessageLogFromDb.getRetryCount() + 1);
            pmsSkuMessageLogFromDb.setNextRetryTime(now.plusMinutes(RETRY_INTERVAL));
            pmsSkuMessageLogFromDb.setUpdateTime(now);
        }
        pmsSkuMessageLogMapper.updateByPrimaryKeySelective(pmsSkuMessageLogFromDb);
    };

    @PostConstruct
    private void init() {
        rabbitTemplate.setConfirmCallback(confirmCallback);
    }

    public void sendPmsSkuInfo(PmsSkuInfo pmsSkuInfo) {
        CorrelationData correlationData = new CorrelationData();
        correlationData.setId(String.valueOf(pmsSkuInfo.getId()));
        rabbitTemplate.convertAndSend("gmall_exchange", "sync_sku_data", pmsSkuInfo, correlationData);
    }
}
