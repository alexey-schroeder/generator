package com.lottery.generator.rarity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Historical positional-range rule for sorted 5-number EuroJackpot main combinations. */
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
        validate(historicalDraws, rejectAtOrAboveOutsidePositions);
        List<Integer> minimums = new ArrayList<>(Collections.nCopies(5, Integer.MAX_VALUE));
        List<Integer> maximums = new ArrayList<>(Collections.nCopies(5, Integer.MIN_VALUE));
        for (List<Integer> draw : historicalDraws) {
            List<Integer> sorted = sortedFive(draw);
            for (int i = 0; i < 5; i++) {
                minimums.set(i, Math.min(minimums.get(i), sorted.get(i)));
                maximums.set(i, Math.max(maximums.get(i), sorted.get(i)));
            }
        }
        return new PositionalRangeRule(minimums, maximums, rejectAtOrAboveOutsidePositions);
    }

    /**
     * Learns a central empirical coverage interval independently for each sorted position.
     * For coverage=0.95, 2.5% is trimmed from each tail. Bounds use nearest-rank empirical
     * quantiles, making the rule deterministic and dependency-free.
     */
    public static PositionalRangeRule fromCentralCoverage(List<List<Integer>> historicalDraws,
                                                           double coverage,
                                                           int rejectAtOrAboveOutsidePositions) {
        validate(historicalDraws, rejectAtOrAboveOutsidePositions);
        if (!(coverage > 0.0 && coverage <= 1.0)) {
            throw new IllegalArgumentException("Coverage must be in (0,1]");
        }
        if (coverage == 1.0) return fromHistory(historicalDraws, rejectAtOrAboveOutsidePositions);

        List<Integer> minimums = new ArrayList<>(5);
        List<Integer> maximums = new ArrayList<>(5);
        double lowerQ = (1.0 - coverage) / 2.0;
        double upperQ = 1.0 - lowerQ;
        for (int position = 0; position < 5; position++) {
            List<Integer> values = new ArrayList<>(historicalDraws.size());
            for (List<Integer> draw : historicalDraws) values.add(sortedFive(draw).get(position));
            Collections.sort(values);
            minimums.add(nearestRank(values, lowerQ));
            maximums.add(nearestRank(values, upperQ));
        }
        return new PositionalRangeRule(minimums, maximums, rejectAtOrAboveOutsidePositions);
    }

    private static int nearestRank(List<Integer> sorted, double quantile) {
        int rank = (int) Math.ceil(quantile * sorted.size());
        rank = Math.max(1, Math.min(sorted.size(), rank));
        return sorted.get(rank - 1);
    }

    private static void validate(List<List<Integer>> historicalDraws, int threshold) {
        if (historicalDraws.isEmpty()) throw new IllegalArgumentException("Historical draws must not be empty");
        if (threshold < 1 || threshold > 5) throw new IllegalArgumentException("Outside-position threshold must be in 1..5");
    }

    private static List<Integer> sortedFive(List<Integer> draw) {
        List<Integer> sorted = draw.stream().sorted().toList();
        if (sorted.size() != 5) throw new IllegalArgumentException("Expected exactly 5 main numbers");
        return sorted;
    }

    public int positionsOutsideRange(List<Integer> candidate) {
        List<Integer> sorted = sortedFive(candidate);
        int outside = 0;
        for (int i = 0; i < 5; i++) {
            int value = sorted.get(i);
            if (value < minimums.get(i) || value > maximums.get(i)) outside++;
        }
        return outside;
    }

    public boolean rejects(List<Integer> candidate) {
        return positionsOutsideRange(candidate) >= rejectAtOrAboveOutsidePositions;
    }

    public List<Integer> minimums() { return minimums; }
    public List<Integer> maximums() { return maximums; }
}
