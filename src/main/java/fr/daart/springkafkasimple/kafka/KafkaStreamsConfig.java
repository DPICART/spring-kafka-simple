package fr.daart.springkafkasimple.kafka;

import fr.daart.springkafkasimple.kafka.in.ItemA;
import fr.daart.springkafkasimple.kafka.in.ItemB;
import fr.daart.springkafkasimple.kafka.out.ItemC;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.support.serializer.JsonSerde;

@EnableKafkaStreams
@Configuration
@RequiredArgsConstructor
@Slf4j
public class KafkaStreamsConfig {

    private final KafkaConfigProperties kafkaConfigProperties;
    public static final Serde<String> STRING_SERDE = Serdes.String();
    public static final Serde<ItemA> ITEM_A_SERDE = new JsonSerde<>(ItemA.class);
    public static final Serde<ItemB> ITEM_B_SERDE = new JsonSerde<>(ItemB.class);
    public static final Serde<ItemC> ITEM_C_SERDE = new JsonSerde<>(ItemC.class);

    @Bean
    public KStream<String, ItemC> kStream(StreamsBuilder streamsBuilder) {

        KTable<String, ItemA> itemAKtable = streamsBuilder
                .table(
                        kafkaConfigProperties.getIn().itemA(),
                        Consumed.with(STRING_SERDE, ITEM_A_SERDE).withName("itemA-processor"),
                        Materialized
                                .<String, ItemA, KeyValueStore<Bytes, byte[]>>as(kafkaConfigProperties.getStore().itemA())
                                .withKeySerde(STRING_SERDE)
                                .withValueSerde(ITEM_A_SERDE)
                );

        KTable<String, ItemB> itemBKtable = streamsBuilder
                .table(
                        kafkaConfigProperties.getIn().itemB(),
                        Consumed.with(STRING_SERDE, ITEM_B_SERDE).withName("itemB-processor"),
                        Materialized
                                .<String, ItemB, KeyValueStore<Bytes, byte[]>>as(kafkaConfigProperties.getStore().itemB())
                                .withKeySerde(STRING_SERDE)
                                .withValueSerde(ITEM_B_SERDE)
                );

        ValueJoiner<ItemA, ItemB, ItemC> valueJoiner = (itemA, itemB) -> ItemC
                .builder()
                .itemA(itemA)
                .itemB(itemB)
                .aComputedValue(itemA.getValue().hashCode() + itemB.getValue().hashCode()) // dummy business
                .build();

        KStream<String, ItemC> outputStream = itemAKtable
                .join(
                        itemBKtable,
                        valueJoiner
                )
                .toStream(Named.as("itemA-itemB-joined-stream"));

        outputStream.to(
                kafkaConfigProperties.getOut().itemC(),
                Produced.with(STRING_SERDE, ITEM_C_SERDE)
        );

        log.info("Application topology will be:\n{}", streamsBuilder.build().describe());

        return outputStream;
    }

}
