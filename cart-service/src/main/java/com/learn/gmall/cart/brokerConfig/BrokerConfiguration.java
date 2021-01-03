package com.learn.gmall.cart.brokerConfig;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BrokerConfiguration {

    @Bean
    public Queue queue() {
        return new AnonymousQueue();
    }

    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange("clear_cart_exchange");
    }

    @Bean
    public Binding binding(Queue queue, DirectExchange directExchange) {
        return BindingBuilder.bind(queue).to(directExchange).with("clear.cart");
    }
}