package cn.iot.process;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TopicConsumer {
    //注册一个队列
    @Bean
    public Queue topicQueue(){
        return   QueueBuilder.durable("Topic_Q01").maxLength(100).build();
    }
    @Bean
    public Queue topicQueue2(){
        return   QueueBuilder.durable("Topic_Q02").maxLength(100).build();
    }
    //注册交换机
    @Bean
    public TopicExchange topicExchange(){
        return  ExchangeBuilder.topicExchange("Topic_E01").build();
    }

    //绑定交换机与队列关系
    @Bean
    public Binding topicBinding(Queue topicQueue, TopicExchange topicExchange){
        return BindingBuilder.bind(topicQueue).to(topicExchange).with("#");
    }
    @Bean
    public Binding topicBinding2(Queue topicQueue2,TopicExchange topicExchange){
        return BindingBuilder.bind(topicQueue2).to(topicExchange).with("1.8.*");
    }

    //启动一个消费者
    @RabbitListener(queues = "Topic_Q01")
    public void receiveMessage(String msg){
        System.out.println("Topic_Q01收到消息："+msg);
    }
    //启动一个消费者
    @RabbitListener(queues = "Topic_Q02")
    public void receiveMessage2(String msg){
        System.out.println("Topic_Q02收到消息："+msg);
    }

}
