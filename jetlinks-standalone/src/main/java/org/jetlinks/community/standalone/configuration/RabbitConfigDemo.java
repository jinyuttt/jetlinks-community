//package org.jetlinks.community.standalone.configuration;
//
//import org.springframework.amqp.core.Binding;
//import org.springframework.amqp.core.BindingBuilder;
//import org.springframework.amqp.core.DirectExchange;
//import org.springframework.amqp.core.Queue;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class RabbitConfigDemo {
//    /**
//     * 示例交换机
//     *
//     * @return
//     */
//    @Bean
//    public DirectExchange demoExchange() {
//        return new DirectExchange("demo.direct.exchange", true, false);
//    }
//
//    /**
//     * 示例队列
//     *
//     * @return
//     */
//    @Bean
//    public Queue demoQueue() {
//        return new Queue("demo.queue", true, false, false);
//    }
//
//    /**
//     * 交换机与队列的绑定关系
//     *
//     * @param demoQueue
//     * @param demoExchange
//     * @return
//     */
//    @Bean
//    public Binding bindingDemoQueue(@Qualifier("demo.queue") Queue demoQueue,
//                                    @Qualifier("demoExchange") DirectExchange demoExchange) {
//        return BindingBuilder.bind(demoQueue).to(demoExchange).with("demo.direct");
//    }
//}