package com.lottery.generator;

import com.lottery.generator.rarity.RarityRules;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BroadRuleSearchSpaceTest {

    private static final int TOTAL_COMBINATIONS = 2_118_760;

    @Test
    void measuresRulesThatKeepAtLeastAboutNinetyPercentOfHistoricalDraws() throws Exception {
        Map<String, Predicate<List<Integer>>> rules = new LinkedHashMap<>();
        rules.put("HIGH_SUM_175", n -> RarityRules.sum(n) >= 175);
        rules.put("LOW_SUM_80", n -> RarityRules.sum(n) <= 80);
        rules.put("NARROW_SPAN_20", n -> RarityRules.span(n) <= 20);
        rules.put("SAME_HALF_ALL_5", BroadRuleSearchSpaceTest::allInSameHalf);
        rules.put("MAX_GAP_30", n -> RarityRules.maxGap(n) >= 30);
        rules.put("RUN_3", n -> RarityRules.maxConsecutiveRun(n) >= 3);
        rules.put("SAME_HALF_OR_RUN_3", n -> allInSameHalf(n) || RarityRules.maxConsecutiveRun(n) >= 3);
        rules.put("HIGH_OR_SAME_HALF", n -> RarityRules.sum(n) >= 175 || allInSameHalf(n));
        rules.put("NARROW_OR_MAX_GAP_30", n -> RarityRules.span(n) <= 20 || RarityRules.maxGap(n) >= 30);

        Map<String, Long> searchRejected = new LinkedHashMap<>();
        rules.keySet().forEach(k -> searchRejected.put(k, 0L));

        for (int a = 1; a <= 46; a++) {
            for (int b = a + 1; b <= 47; b++) {
                for (int c = b + 1; c <= 48; c++) {
                    for (int d = c + 1; d <= 49; d++) {
                        for (int e = d + 1; e <= 50; e++) {
                            List<Integer> candidate = List.of(a, b, c, d, e);
                            rules.forEach((name, rule) -> {
                                if (rule.test(candidate)) searchRejected.merge(name, 1L, Long::sum);
                            });
                        }
                    }
                }
            }
        }

        List<List<Integer>> history = loadDraws();
        assertEquals(987, history.size());
        for (Map.Entry<String, Predicate<List<Integer>>> entry : rules.entrySet()) {
            long historicalRejected = history.stream().filter(entry.getValue()).count();
            long spaceRejected = searchRejected.get(entry.getKey());
            System.out.printf("BROAD_RULE|%s|spaceRejected=%d|spaceRejectedPct=%.6f|historicalRejected=%d|historicalRejectedPct=%.6f|historicalKeptPct=%.6f%n",
                    entry.getKey(), spaceRejected, pct(spaceRejected, TOTAL_COMBINATIONS), historicalRejected,
                    pct(historicalRejected, history.size()), pct(history.size() - historicalRejected, history.size()));
        }
    }

    private static boolean allInSameHalf(List<Integer> numbers) {
        return numbers.stream().allMatch(n -> n <= 25) || numbers.stream().allMatch(n -> n >= 26);
    }

    private static double pct(long count, long total) {
        return 100.0 * count / total;
    }

    private static List<List<Integer>> loadDraws() throws Exception {
        InputStream stream = BroadRuleSearchSpaceTest.class.getResourceAsStream("/eurojackpot_archiv.csv");
        if (stream == null) throw new IllegalStateException("Missing /eurojackpot_archiv.csv");
        List<List<Integer>> draws = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] columns = line.split(",");
                draws.add(Arrays.stream(columns[1].split("-"))
                        .map(Integer::valueOf).sorted().toList());
            }
        }
        return draws;
    }
}
