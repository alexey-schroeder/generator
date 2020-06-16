package com.lottery.generator.theory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

public class TheoryUtils {
    public static List<Integer> calculateIntersection(List<Integer> list, List<Integer> otherList) {
        if (list == null || otherList == null) {
            return Collections.emptyList();
        }

        return list.stream()
                .filter(otherList::contains)
                .collect(Collectors.toList());
    }

    public static int getMax(int[] array) {
        return Arrays.stream(array)
                .max()
                .getAsInt();
    }

    public static int getMax(List<Integer> integers) {
        return integers.stream().mapToInt(v -> v).max().orElseThrow(NoSuchElementException::new);
    }

    public static int[] scalToMaximum(int[] array, int newMax) {
        int[] result = new int[array.length];
        double newMaxDouble = newMax * 1.0;
        int maxInArray = getMax(array);
        double factor = newMaxDouble / maxInArray;

        for (int i = 0; i < array.length; i++) {
            int oldValue = array[i];
            double newValue = oldValue * factor;
            result[i] = (int) Math.round(newValue);
        }
        return result;
    }
}
