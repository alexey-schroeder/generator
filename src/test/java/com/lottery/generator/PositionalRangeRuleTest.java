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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PositionalRangeRuleTest {

    private static final int TOTAL_COMBINATIONS = 2_118_760;

    @Test
    void positionalRangesAndExactSearchSpaceImpact() throws Exception {
        List<List<Integer>> history = loadDrawsNewestFirst();
        PositionalRangeRule rule = PositionalRangeRule.fromHistory(history, 2);

        long[] outsideDistribution = new long[6];
        long rejected = 0;

        for (int a = 1; a <= 46; a++) {
            for (int b = a + 1; b <= 47; b++) {
                for (int c = b + 1; c <= 48; c++) {
                    for (int d = c + 1; d <= 49; d++) {
                        for (int e = d + 1; e <= 50; e++) {
                            List<Integer> candidate = List.of(a, b, c, d, e);
                            int outside = rule.positionsOutsideRange(candidate);
                            outsideDistribution[outside]++;
                            if (outside >= 2) rejected++;
                        }
                    }
                }
            }
        }

        assertEquals(TOTAL_COMBINATIONS, Arrays.stream(outsideDistribution).sum());
        assertTrue(rejected > 0);

        System.out.printf("POSITION_RANGES|min=%s|max=%s%n", rule.minimums(), rule.maximums());
        for (int i = 0; i < outsideDistribution.length; i++) {
            System.out.printf("POSITION_SPACE|outside=%d|count=%d|pct=%.6f%n",
                    i, outsideDistribution[i], pct(outsideDistribution[i], TOTAL_COMBINATIONS));
        }
        System.out.printf("POSITION_REJECT|threshold=2|rejected=%d|rejectedPct=%.6f|remaining=%d|remainingPct=%.6f%n",
                rejected, pct(rejected, TOTAL_COMBINATIONS), TOTAL_COMBINATIONS - rejected,
                pct(TOTAL_COMBINATIONS - rejected, TOTAL_COMBINATIONS));
    }

    @Test
    void rollingHistoricalBacktestUsesOnlyPriorDraws() throws Exception {
        List<List<Integer>> chronological = loadDrawsNewestFirst();
        Collections.reverse(chronological);

        int minTrainingDraws = 50;
        int checked = 0;
        int rejected = 0;
        int[] outsideDistribution = new int[6];

        for (int i = minTrainingDraws; i < chronological.size(); i++) {
            List<List<Integer>> pastOnly = chronological.subList(0, i);
            PositionalRangeRule rule = PositionalRangeRule.fromHistory(pastOnly, 2);
            int outside = rule.positionsOutsideRange(chronological.get(i));
            outsideDistribution[outside]++;
            if (outside >= 2) rejected++;
            checked++;
        }

        System.out.printf("POSITION_ROLLING|trainingMin=%d|checked=%d|rejected=%d|rejectedPct=%.6f|kept=%d|keptPct=%.6f%n",
                minTrainingDraws, checked, rejected, pct(rejected, checked), checked - rejected,
                pct(checked - rejected, checked));
        for (int i = 0; i < outsideDistribution.length; i++) {
            System.out.printf("POSITION_ROLLING_DIST|outside=%d|count=%d|pct=%.6f%n",
                    i, outsideDistribution[i], pct(outsideDistribution[i], checked));
        }

        assertEquals(987 - minTrainingDraws, checked);
    }

    private static double pct(long count, long total) {
        return 100.0 * count / total;
    }

    private static List<List<Integer>> loadDrawsNewestFirst() throws Exception {
        InputStream stream = PositionalRangeRuleTest.class.getResourceAsStream("/eurojackpot_archiv.csv");
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
}
