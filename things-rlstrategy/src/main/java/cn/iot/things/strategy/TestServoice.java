package cn.iot.things.strategy;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class TestServoice implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {
        System.out.println("TestServoice");
    }
}
