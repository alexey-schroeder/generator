package com.lottery.generator.rarity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Historical positional-range rule for sorted 5-number EuroJackpot main combinations.
 *
 * For every sorted position (0..4), the rule learns the historical minimum and maximum.
 * A candidate is rejected when at least {@code maxOutsidePositions} positions fall outside
 * their respective historical ranges.
 */
public final class PositionalRangeRule {

    private final List<Integer> minimums;
    private final List<Integer> maximums;
    private final int rejectAtOrAboveOutsidePositions;

    private PositionalRangeRule(List<Integer> minimums, List<Integer> maximums,
                                int rejectAtOrAboveOutsidePositions) {
        this.minimums = List.copyOf(minimums);
        this.maximums = List.copyOf(maximums);
        this.rejectAtOrAboveOutsidePositions = rejectAtOrAboveOutsidePositions;
    }

    public static PositionalRangeRule fromHistory(List<List<Integer>> historicalDraws,
                                                   int rejectAtOrAboveOutsidePositions) {
        if (historicalDraws.isEmpty()) {
            throw new IllegalArgumentException("Historical draws must not be empty");
        }
        if (rejectAtOrAboveOutsidePositions < 1 || rejectAtOrAboveOutsidePositions > 5) {
            throw new IllegalArgumentException("Outside-position threshold must be in 1..5");
        }

        List<Integer> minimums = new ArrayList<>(Collections.nCopies(5, Integer.MAX_VALUE));
        List<Integer> maximums = new ArrayList<>(Collections.nCopies(5, Integer.MIN_VALUE));

        for (List<Integer> draw : historicalDraws) {
            List<Integer> sorted = draw.stream().sorted().toList();
            if (sorted.size() != 5) {
                throw new IllegalArgumentException("Expected exactly 5 main numbers");
            }
            for (int i = 0; i < 5; i++) {
                minimums.set(i, Math.min(minimums.get(i), sorted.get(i)));
                maximums.set(i, Math.max(maximums.get(i), sorted.get(i)));
            }
        }

        return new PositionalRangeRule(minimums, maximums, rejectAtOrAboveOutsidePositions);
    }

    public int positionsOutsideRange(List<Integer> candidate) {
        List<Integer> sorted = candidate.stream().sorted().toList();
        if (sorted.size() != 5) {
            throw new IllegalArgumentException("Expected exactly 5 main numbers");
        }
        int outside = 0;
        for (int i = 0; i < 5; i++) {
            int value = sorted.get(i);
            if (value < minimums.get(i) || value > maximums.get(i)) {
                outside++;
            }
        }
        return outside;
    }

    public boolean rejects(List<Integer> candidate) {
        return positionsOutsideRange(candidate) >= rejectAtOrAboveOutsidePositions;
    }

    public List<Integer> minimums() {
        return minimums;
    }

    public List<Integer> maximums() {
        return maximums;
    }
}
