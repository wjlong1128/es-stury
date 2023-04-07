package cn.itcast.hotel.config;

import cn.itcast.hotel.consts.MQConstants;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*
 * @author Wang Jianlong
 * @version 1.0.0
 * @description
 * @date 2023/4/7
 */
@Configuration
public class MQConfig {

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(MQConstants.HOTEL_EXCHANGE, true, false);
    }
    //
    @Bean
    public Queue insetQueue() {
        return QueueBuilder.durable(MQConstants.HOTEL_INSERT_QUEUE).build();
    }


    @Bean
    public Queue deleteQueue() {
        return QueueBuilder.durable(MQConstants.HOTEL_DELETE_QUEUE).build();
    }

    @Bean
    public Binding bindingInsetQueue(){
        return BindingBuilder.bind(insetQueue()).to(topicExchange()).with(MQConstants.HOTEL_INSERT_KEY);
    }


    @Bean
    public Binding bindingDeleteQueue(){
        return BindingBuilder.bind(deleteQueue()).to(topicExchange()).with(MQConstants.HOTEL_DELETE_KEY);
    }
}
