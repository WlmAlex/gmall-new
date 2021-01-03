package com.learn.gmall.search.service.listener;

import com.learn.gmall.api.bean.PmsSearchSkuInfo;
import com.learn.gmall.api.bean.PmsSkuInfo;
import com.rabbitmq.client.Channel;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.Random;

@Component
public class SyncPmsSkuInfoListener {

    @Autowired
    private JestClient jestClient;

    private final Random random = new Random();

    @RabbitListener(queues = "#{queue.name}")
    public void syncPmsSkuInfo(@Payload PmsSkuInfo pmsSkuInfo, @Headers Map<String, Object> headers, Channel channel) throws IOException {

        Long deliveryTag = (Long) headers.get(AmqpHeaders.DELIVERY_TAG);
        try {
            PmsSearchSkuInfo pmsSearchSkuInfo = PmsSearchSkuInfo.builder()
                    .id(pmsSkuInfo.getId())
                    .catalog3Id(String.valueOf(pmsSkuInfo.getCatalog3Id()))
                    .price(pmsSkuInfo.getPrice())
                    .productId(String.valueOf(pmsSkuInfo.getSpuId()))
                    .skuAttrValueList(pmsSkuInfo.getSkuAttrValueList())
                    .skuDefaultImg(pmsSkuInfo.getSkuDefaultImg())
                    .skuDesc(pmsSkuInfo.getSkuDesc())
                    .skuName(pmsSkuInfo.getSkuName())
                    .hotScore(Double.valueOf(random.nextInt(100) + 1))
                    .build();
            Index index = new Index.Builder(pmsSearchSkuInfo)
                    .index("gmall")
                    .type("PmsSkuInfo")
                    .id(String.valueOf(pmsSearchSkuInfo.getId()))
                    .build();
            jestClient.execute(index);
            channel.basicAck(deliveryTag, false);
        } catch (IOException e) {
            channel.basicNack(deliveryTag, false, true);
        }
    }
}