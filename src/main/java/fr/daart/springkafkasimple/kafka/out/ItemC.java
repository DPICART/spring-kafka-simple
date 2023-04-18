package fr.daart.springkafkasimple.kafka.out;

import fr.daart.springkafkasimple.kafka.in.ItemA;
import fr.daart.springkafkasimple.kafka.in.ItemB;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemC {

    private ItemA itemA;
    private ItemB itemB;
    private long aComputedValue;

}
