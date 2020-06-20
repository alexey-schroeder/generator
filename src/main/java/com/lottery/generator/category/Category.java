package com.lottery.generator.category;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.text.MessageFormat;
import java.time.temporal.ValueRange;
import java.util.Map;

@AllArgsConstructor
public class Category {
    @Getter
    private String name;
    private Map<Integer, ValueRange> indexes;

    public void addIndex(Integer index, ValueRange range){
        indexes.put(index, range);
    }

    public int getIndexForNumber(int number) {
        for (Map.Entry<Integer, ValueRange> entry : indexes.entrySet()) {
            if (entry.getValue().isValidValue(number)) {
                return entry.getKey();
            }
        }

        throw new IllegalArgumentException(MessageFormat.format("number {0} is not in category {1}!", number, name));
    }
}
