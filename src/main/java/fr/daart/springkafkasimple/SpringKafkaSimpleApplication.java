package fr.daart.springkafkasimple;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication
public class SpringKafkaSimpleApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringKafkaSimpleApplication.class, args);
    }

}
