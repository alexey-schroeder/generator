package com.lottery.generator;

import com.lottery.generator.rarity.PositionalRangeRule;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Sweeps central positional coverage ranges and measures the trade-off between
 * exact search-space reduction and out-of-sample historical keep rate.
 *
 * A candidate is rejected when at least two sorted positions are outside their
 * independently learned central empirical ranges.
 */
class PositionalCoverageSweepTest {

    private static final int TOTAL_COMBINATIONS = 2_118_760;
    private static final int MIN_TRAINING_DRAWS = 50;
    private static final double[] TARGET_KEEP_RATES = {99.0, 97.5, 95.0, 90.0};

    private static final List<Double> COVERAGES = List.of(
            1.00, 0.99, 0.98, 0.975, 0.97, 0.96, 0.95, 0.94, 0.93, 0.925,
            0.92, 0.91, 0.90, 0.89, 0.88, 0.875, 0.87, 0.86, 0.85, 0.84,
            0.83, 0.825, 0.82, 0.81, 0.80
    );

    @Test
    void sweepCoverageAndFindBestTradeoffs() throws Exception {
        List<List<Integer>> newestFirst = loadDrawsNewestFirst();
        List<List<Integer>> chronological = new ArrayList<>(newestFirst);
        Collections.reverse(chronological);

        List<SweepResult> results = new ArrayList<>();
        for (double coverage : COVERAGES) {
            PositionalRangeRule fullHistoryRule = PositionalRangeRule.fromCentralCoverage(newestFirst, coverage, 2);
            long spaceRejected = countRejectedCombinations(fullHistoryRule);
            RollingResult rolling = rollingBacktest(chronological, coverage);

            SweepResult result = new SweepResult(
                    coverage,
                    fullHistoryRule.minimums(),
                    fullHistoryRule.maximums(),
                    spaceRejected,
                    pct(spaceRejected, TOTAL_COMBINATIONS),
                    rolling.checked,
                    rolling.rejected,
                    pct(rolling.checked - rolling.rejected, rolling.checked)
            );
            results.add(result);

            System.out.printf(Locale.ROOT,
                    "POSITION_SWEEP|coverage=%.3f|min=%s|max=%s|spaceRejected=%d|spaceRejectedPct=%.6f|rollingChecked=%d|rollingRejected=%d|rollingKeptPct=%.6f%n",
                    result.coverage, result.minimums, result.maximums, result.spaceRejected,
                    result.spaceRejectedPct, result.rollingChecked, result.rollingRejected,
                    result.rollingKeptPct);
        }

        assertEquals(COVERAGES.size(), results.size());
        assertEquals(987 - MIN_TRAINING_DRAWS, results.get(0).rollingChecked);

        for (double targetKeep : TARGET_KEEP_RATES) {
            List<SweepResult> eligible = results.stream()
                    .filter(r -> r.rollingKeptPct >= targetKeep)
                    .toList();
            assertFalse(eligible.isEmpty(), "No sweep point satisfies keep >= " + targetKeep);

            SweepResult best = eligible.stream()
                    .max(Comparator.comparingLong(SweepResult::spaceRejected))
                    .orElseThrow();

            System.out.printf(Locale.ROOT,
                    "POSITION_SWEEP_BEST|targetKeepPct=%.1f|coverage=%.3f|spaceRejected=%d|spaceRejectedPct=%.6f|rollingRejected=%d|rollingKeptPct=%.6f|min=%s|max=%s%n",
                    targetKeep, best.coverage, best.spaceRejected, best.spaceRejectedPct,
                    best.rollingRejected, best.rollingKeptPct, best.minimums, best.maximums);
        }
    }

    private static long countRejectedCombinations(PositionalRangeRule rule) {
        long rejected = 0;
        for (int a = 1; a <= 46; a++) {
            for (int b = a + 1; b <= 47; b++) {
                for (int c = b + 1; c <= 48; c++) {
                    for (int d = c + 1; d <= 49; d++) {
                        for (int e = d + 1; e <= 50; e++) {
                            if (rule.rejects(List.of(a, b, c, d, e))) rejected++;
                        }
                    }
                }
            }
        }
        return rejected;
    }

    private static RollingResult rollingBacktest(List<List<Integer>> chronological, double coverage) {
        int checked = 0;
        int rejected = 0;
        for (int i = MIN_TRAINING_DRAWS; i < chronological.size(); i++) {
            PositionalRangeRule rule = PositionalRangeRule.fromCentralCoverage(chronological.subList(0, i), coverage, 2);
            if (rule.rejects(chronological.get(i))) rejected++;
            checked++;
        }
        return new RollingResult(checked, rejected);
    }

    private static double pct(long count, long total) {
        return 100.0 * count / total;
    }

    private static List<List<Integer>> loadDrawsNewestFirst() throws Exception {
        InputStream stream = PositionalCoverageSweepTest.class.getResourceAsStream("/eurojackpot_archiv.csv");
        if (stream == null) throw new IllegalStateException("Missing /eurojackpot_archiv.csv");
        List<List<Integer>> draws = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] columns = line.split(",");
                draws.add(Arrays.stream(columns[1].split("-"))
                        .map(Integer::valueOf)
                        .sorted()
                        .toList());
            }
        }
        return draws;
    }

    private record RollingResult(int checked, int rejected) {}

    private record SweepResult(
            double coverage,
            List<Integer> minimums,
            List<Integer> maximums,
            long spaceRejected,
            double spaceRejectedPct,
            int rollingChecked,
            int rollingRejected,
            double rollingKeptPct
    ) {}
}
