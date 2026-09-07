package com.lottery.generator.rarity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Stateless structural rules used by rarity analysis and future filters. */
public final class RarityRules {

    private RarityRules() {}

    public static int intersection(List<Integer> a, List<Integer> b) {
        Set<Integer> values = new HashSet<>(a);
        values.retainAll(b);
        return values.size();
    }

    public static int sortedL1Distance(List<Integer> a, List<Integer> b) {
        List<Integer> left = sorted(a);
        List<Integer> right = sorted(b);
        int result = 0;
        for (int i = 0; i < Math.min(left.size(), right.size()); i++) {
            result += Math.abs(left.get(i) - right.get(i));
        }
        return result;
    }

    public static int mirrorL1Distance(List<Integer> previous, List<Integer> current, int maxNumber) {
        List<Integer> mirror = new ArrayList<>();
        for (int value : previous) mirror.add(maxNumber + 1 - value);
        return sortedL1Distance(mirror, current);
    }

    public static int gapDistance(List<Integer> a, List<Integer> b) {
        List<Integer> ga = gaps(a);
        List<Integer> gb = gaps(b);
        int result = 0;
        for (int i = 0; i < Math.min(ga.size(), gb.size()); i++) {
            result += Math.abs(ga.get(i) - gb.get(i));
        }
        return result;
    }

    public static int maxEqualShiftCount(List<Integer> previous, List<Integer> current) {
        List<Integer> left = sorted(previous);
        List<Integer> right = sorted(current);
        Map<Integer, Integer> counts = new HashMap<>();
        for (int i = 0; i < Math.min(left.size(), right.size()); i++) {
            counts.merge(right.get(i) - left.get(i), 1, Integer::sum);
        }
        return counts.values().stream().mapToInt(Integer::intValue).max().orElse(0);
    }

    public static int sum(List<Integer> numbers) {
        return numbers.stream().mapToInt(Integer::intValue).sum();
    }

    public static int span(List<Integer> numbers) {
        List<Integer> values = sorted(numbers);
        return values.get(values.size() - 1) - values.get(0);
    }

    public static int oddCount(List<Integer> numbers) {
        return (int) numbers.stream().filter(n -> n % 2 != 0).count();
    }

    public static int highCount(List<Integer> numbers, int split) {
        return (int) numbers.stream().filter(n -> n > split).count();
    }

    public static int maxConsecutiveRun(List<Integer> numbers) {
        List<Integer> values = sorted(numbers);
        int best = 1;
        int current = 1;
        for (int i = 1; i < values.size(); i++) {
            if (values.get(i) == values.get(i - 1) + 1) {
                current++;
                best = Math.max(best, current);
            } else {
                current = 1;
            }
        }
        return best;
    }

    public static int consecutivePairCount(List<Integer> numbers) {
        List<Integer> values = sorted(numbers);
        int count = 0;
        for (int i = 1; i < values.size(); i++) {
            if (values.get(i) == values.get(i - 1) + 1) count++;
        }
        return count;
    }

    public static int gapRange(List<Integer> numbers) {
        List<Integer> values = gaps(numbers);
        return Collections.max(values) - Collections.min(values);
    }

    public static int maxGap(List<Integer> numbers) {
        return Collections.max(gaps(numbers));
    }

    public static int minWindow(List<Integer> numbers, int size) {
        List<Integer> values = sorted(numbers);
        int best = Integer.MAX_VALUE;
        for (int i = 0; i + size <= values.size(); i++) {
            best = Math.min(best, values.get(i + size - 1) - values.get(i));
        }
        return best;
    }

    public static int maxCountInDecade(List<Integer> numbers) {
        int[] bins = new int[5];
        for (int n : numbers) bins[Math.min(4, (n - 1) / 10)]++;
        int max = 0;
        for (int count : bins) max = Math.max(max, count);
        return max;
    }

    public static int maxSameLastDigitCount(List<Integer> numbers) {
        int[] counts = new int[10];
        for (int n : numbers) counts[n % 10]++;
        int max = 0;
        for (int count : counts) max = Math.max(max, count);
        return max;
    }

    public static int maxSameModuloCount(List<Integer> numbers, int modulo) {
        int[] counts = new int[modulo];
        for (int n : numbers) counts[Math.floorMod(n, modulo)]++;
        int max = 0;
        for (int count : counts) max = Math.max(max, count);
        return max;
    }

    public static String decadeSignature(List<Integer> numbers) {
        int[] bins = new int[5];
        for (int n : numbers) bins[Math.min(4, (n - 1) / 10)]++;
        return bins[0] + "-" + bins[1] + "-" + bins[2] + "-" + bins[3] + "-" + bins[4];
    }

    public static String paritySignature(List<Integer> numbers) {
        List<Integer> values = sorted(numbers);
        StringBuilder builder = new StringBuilder();
        for (int n : values) builder.append(n % 2 == 0 ? 'E' : 'O');
        return builder.toString();
    }

    private static List<Integer> gaps(List<Integer> numbers) {
        List<Integer> values = sorted(numbers);
        List<Integer> result = new ArrayList<>();
        for (int i = 1; i < values.size(); i++) result.add(values.get(i) - values.get(i - 1));
        return result;
    }

    private static List<Integer> sorted(List<Integer> numbers) {
        List<Integer> copy = new ArrayList<>(numbers);
        Collections.sort(copy);
        return copy;
    }
}
