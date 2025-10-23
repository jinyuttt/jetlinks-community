package cn.iot.process.eventbus;


import org.jetlinks.core.event.Subscription;
import org.reactivestreams.Publisher;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import org.jetlinks.core.event.EventBus;

@Component
public class MessageService  implements CommandLineRunner{
    @Autowired
    private
    RabbitTemplate rabbitTemplate;
    @Autowired
    EventBus eventBus;

    public void doSubscribe(){
        eventBus
            //调用subscribe方法
            .subscribe(Subscription
                           //构建订阅者消息
                           .builder()
                           //订阅者标识
                           .subscriberId("network-config-manager")
                           //订阅topic
                           .topics("/device/**","/online","/message/**")
                           //订阅特性为shared
                           .shared()
                           .build())
            //拿到消息总线中的数据进行后续处理
            .flatMap(payload->{
                //根据业务场景编写需要对消息进行处理的逻辑
                System.out.println(payload);
                rabbitTemplate.convertAndSend("device",payload.bodyToString());
                return doSomething();
            })
            .subscribe();
    }

    private Publisher<?> doSomething() {
        return Flux.just();
    }
    @Override
    public void run(String... args) throws Exception {}
}
