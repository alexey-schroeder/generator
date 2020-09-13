package com.lottery.generator.category;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@ToString
@AllArgsConstructor
public class Category {
    private String name;
    private int indexInBasisNumbers;
    private Map<Integer, CategoryIndexValues> indexes;

    public int getIndexForNumber(int number) {
        List<Integer> passibleResults = new ArrayList<>();
        for (Map.Entry<Integer, CategoryIndexValues> entry : indexes.entrySet()) {
            CategoryIndexValues value = entry.getValue();
            if (value.getRange().isValidValue(number) && !value.getExcludes().contains(number)) {
                passibleResults.add(entry.getKey());
            }
        }

        if (passibleResults.isEmpty()) {
            throw new IllegalArgumentException(MessageFormat.format("number {0} is not in category {1}!", number, name));
        }
        if (passibleResults.size() > 1) {
            throw new IllegalArgumentException(MessageFormat.format("number {0} is multiple times{1} in category {2}!", number, passibleResults, name));
        }
        return passibleResults.get(0);
    }
}
