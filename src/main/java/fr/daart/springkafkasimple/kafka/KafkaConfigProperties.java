package fr.daart.springkafkasimple.kafka;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties("our-kafka-config")
public class KafkaConfigProperties {

    @NotNull
    private In in;

    @NotNull
    private Out out;

    @NotNull
    private Store store;

    @NotBlank
    private String dlq;

    public record In(String itemA, String itemB) {}

    public record Out(String itemC) {}

    public record Store(String itemA, String itemB, String itemC) {}

}
