package org.jetlinks.community.standalone.configuration;


import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {


    // 定义 Topic Exchange
    public static final String TOPIC_EXCHANGE_NAME = "topic.exchange";

    // 定义队列名称
    public static final String QUEUE_DEVICE = "device.queue";
    public static final String QUEUE_MESSAGE = "message.queue";
    public static final String QUEUE_STATUS = "status.queue";
    public static final String QUEUE_FIRMWARE = "firmware.queue";
    public static final String QUEUE_ALARM = "alarm.queue";
    public static final String QUEUE_OPT = "opt.queue";


    // 定义路由键模式
    public static final String ROUTING_KEY_DEVICE = "device";
    public static final String ROUTING_KEY_MESSAGE= "message";
    public static final String ROUTING_KEY_STATUS= "status";
    public static final String ROUTING_KEY_FIRMWARE = "firmware";
    public static final String ROUTING_KEY_ALARM= "alarm";
    public static final String ROUTING_KEY_OPT= "opt";
    public static final String ROUTING_KEY_ALL= "#";

    // 添加json格式序列化器
    @Bean
    public MessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }

    // 直连交换机示例
    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange("direct.exchange");
    }
    @Bean
    public Queue orderQueue() {
        return new Queue("order.queue", true); // true表示持久化
    }
    // 持久化队列
    @Bean
    public Queue deviceQueue() {
        return new Queue(QUEUE_DEVICE, true); // true表示持久化
    }
    @Bean
    public Queue messageQueue() {
        return new Queue(QUEUE_MESSAGE, true); // true表示持久化
    }
    @Bean
    public Queue statusQueue() {
        return new Queue(QUEUE_STATUS, false); // true表示持久化
    }
    @Bean
    public Queue firmwareQueue() {
        return new Queue(QUEUE_FIRMWARE, true); // true表示持久化
    }
    @Bean
    public Queue alarmQueue() {
        return new Queue(QUEUE_ALARM, true); // true表示持久化
    }
    @Bean
    public Queue optQueue() {
        return new Queue(QUEUE_OPT, true); // true表示持久化
    }
    // 绑定关系
    @Bean
    public Binding bindingOrder(Queue orderQueue, DirectExchange directExchange) {
        return BindingBuilder.bind(orderQueue)
                             .to(directExchange)
                             .with("order.routingKey");
    }
    @Bean
    public Binding bindingDevice(Queue deviceQueue, TopicExchange topicExchange) {
        return BindingBuilder.bind(deviceQueue)
                             .to(topicExchange)
                             .with(ROUTING_KEY_DEVICE);
    }
    @Bean
    public Binding bindingMessage(Queue messageQueue, TopicExchange topicExchange) {
        return BindingBuilder.bind(messageQueue)
                             .to(topicExchange)
                             .with(ROUTING_KEY_MESSAGE);
    }
    @Bean
    public Binding bindingStatus(Queue statusQueue,TopicExchange  topicExchange) {
        return BindingBuilder.bind(statusQueue)
                             .to(topicExchange)
                             .with(ROUTING_KEY_STATUS);
    }
    @Bean
    public Binding bindingFirmware(Queue firmwareQueue, TopicExchange topicExchange) {
        return BindingBuilder.bind(firmwareQueue)
                             .to(topicExchange)
                             .with(ROUTING_KEY_FIRMWARE);
    }
    @Bean
    public Binding bindingAlarm(Queue alarmQueue, TopicExchange topicExchange) {
        return BindingBuilder.bind(alarmQueue)
                             .to(topicExchange)
                             .with(ROUTING_KEY_ALARM);
    }
    @Bean
    public Binding bindingOpt(Queue optQueue, TopicExchange topicExchange) {
        return BindingBuilder.bind(optQueue)
                             .to(topicExchange)
                             .with(ROUTING_KEY_OPT);
    }
    // 主题交换机示例
    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(TOPIC_EXCHANGE_NAME);
    }
    // 扇型交换机示例
    @Bean
    public FanoutExchange fanoutExchange() {
        return new FanoutExchange("fanout.exchange");
    }
}