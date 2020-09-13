package com.lottery.generator.category;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.temporal.ValueRange;
import java.util.List;

@Getter
@AllArgsConstructor(staticName = "from")
public class CategoryIndexValues {
    private ValueRange range;
    private List<Integer> excludes;
}
