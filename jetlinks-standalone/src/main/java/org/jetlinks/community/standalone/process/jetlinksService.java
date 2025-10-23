package org.jetlinks.community.standalone.process;


import org.jetlinks.community.standalone.configuration.RabbitConfig;
import org.jetlinks.core.event.EventBus;
import org.jetlinks.core.event.Subscription;
import org.jetlinks.core.event.TopicPayload;
import org.reactivestreams.Publisher;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class jetlinksService implements CommandLineRunner {
    @Autowired
    private
    RabbitTemplate rabbitTemplate;
    @Autowired
    EventBus  eventBus;
    public void doSubscribe(){
        eventBus
            //调用subscribe方法
            .subscribe(Subscription
                           //构建订阅者消息
                           .builder()
                           //订阅者标识
                           .subscriberId("network-config-manager")
                           //订阅topic
                           .topics("/device/**","/online","/offline","/message/**","/disconnect","/firmware/**","/alarm/**")
                           .topics("/register/**","/unregister/**")
                           //订阅特性为shared
                           .shared()
                           .build())
            //拿到消息总线中的数据进行后续处理
            .flatMap(payload->{
                //根据业务场景编写需要对消息进行处理的逻辑
                System.out.println(payload);
              //  rabbitTemplate.convertAndSend("direct.exchange","order.routingKey",payload.bodyToString());
               return doSomething(payload);
            })
            .subscribe();
    }

    private Publisher<?> doSomething(TopicPayload payload) {
            String topic= payload.getTopic();
            String  body=payload.bodyToString();
            if (topic.startsWith("/device")) {
               sendMessage(RabbitConfig.ROUTING_KEY_DEVICE, body);
            }
            else if (topic.startsWith("/online")||topic.startsWith("/offline")) {
               sendMessage(RabbitConfig.ROUTING_KEY_STATUS,body);
            }
            else if (topic.startsWith("/message")) {
               sendMessage(RabbitConfig.ROUTING_KEY_MESSAGE,body);
            }

            else if (topic.startsWith("/firmware")) {
               sendMessage(RabbitConfig.ROUTING_KEY_FIRMWARE,body);
            }
            else if (topic.startsWith("/alarm")) {
               sendMessage(RabbitConfig.ROUTING_KEY_ALARM,body);
            }
            else {
                sendMessage(RabbitConfig.ROUTING_KEY_OPT,body);
            }
        return Flux.just();
    }
private void  sendMessage(String key,String message) {
    rabbitTemplate.convertAndSend(RabbitConfig.TOPIC_EXCHANGE_NAME, key, message);
}
    @Override
    public void run(String... args) throws Exception {
          doSubscribe();
    }
}
