package com.learn.gmall.cart.messageListener;

import com.learn.gmall.api.api.CartService;
import com.learn.gmall.api.bean.OmsOrderItem;
import com.rabbitmq.client.Channel;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
public class MessageListener {

    @DubboReference
    private CartService cartService;

    @RabbitListener(queues = "#{queue.name}")
    public void clearCart(@Payload List<OmsOrderItem> omsOrderItemList, @Headers Map<String, Object> headers, Channel channel) throws IOException {
        Long deliveryTag = (Long) headers.get(AmqpHeaders.DELIVERY_TAG);
        try {
            omsOrderItemList.stream().forEach(omsOrderItem -> {
                cartService.clearCart(omsOrderItem);
            });
            channel.basicAck(deliveryTag, false);
        } catch (IOException e) {
            channel.basicNack(deliveryTag, false, true);
        }
    }
}
