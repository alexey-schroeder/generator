package com.lottery.generator.theory;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TheoryUtils {
    public static List<Integer> calculateIntersection(List<Integer> list, List<Integer> otherList) {
        if(list == null || otherList == null){
            return Collections.emptyList();
        }

        return list.stream()
                .filter(otherList::contains)
                .collect(Collectors.toList());
    }
}
