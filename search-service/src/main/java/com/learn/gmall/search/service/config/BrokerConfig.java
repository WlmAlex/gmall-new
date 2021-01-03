package com.learn.gmall.search.service.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BrokerConfig {

    @Bean
    public Queue queue() {
        return new AnonymousQueue();
    }

    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange("gmall_exchange");
    }

    @Bean
    public Binding binding(Queue queue, DirectExchange directExchange) {
        return BindingBuilder.bind(queue).to(directExchange).with("sync_sku_data");
    }

    /*@Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }*/
}
