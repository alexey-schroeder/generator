package com.lottery.generator;

import com.lottery.generator.rarity.AdaptiveRarityThresholdService;
import com.lottery.generator.rarity.AdaptiveRarityThresholdService.AdaptiveRule;
import com.lottery.generator.rarity.AdaptiveRarityThresholdService.Snapshot;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class AdaptiveRarityThresholdServiceTest {

    @Test
    void recalculatesAfterEveryDrawWithoutFutureLeakage() throws Exception {
        List<List<Integer>> chronological = loadChronological();
        AdaptiveRarityThresholdService service = new AdaptiveRarityThresholdService();

        int warmup = 120;
        EnumMap<AdaptiveRule,Integer> rejected = new EnumMap<>(AdaptiveRule.class);
        for (AdaptiveRule rule : AdaptiveRule.values()) rejected.put(rule, 0);

        int checked = 0;
        for (int i = warmup; i < chronological.size(); i++) {
            List<List<Integer>> pastOnly = chronological.subList(0, i);
            Snapshot snapshot = service.recalculate(pastOnly, 0.90);
            var decision = snapshot.evaluate(chronological.get(i));
            for (AdaptiveRule rule : decision.rejectedBy()) rejected.merge(rule, 1, Integer::sum);
            assertEquals(i, snapshot.historySize());
            assertEquals(pastOnly.get(pastOnly.size()-1), snapshot.latestDraw());
            checked++;
        }

        System.out.printf("ADAPTIVE_ROLLING|checked=%d|targetKeepPct=90.0%n", checked);
        for (AdaptiveRule rule : AdaptiveRule.values()) {
            int r = rejected.get(rule);
            System.out.printf("ADAPTIVE_RULE|%s|rejected=%d|keptPct=%.6f%n", rule, r, 100.0 * (checked-r) / checked);
        }
        assertEquals(987 - warmup, checked);
    }

    @Test
    void latestSnapshotCanEvaluateEveryCandidate() throws Exception {
        List<List<Integer>> history = loadChronological();
        Snapshot snapshot = new AdaptiveRarityThresholdService().recalculate(history, 0.90);
        EnumMap<AdaptiveRule,Long> counts = new EnumMap<>(AdaptiveRule.class);
        for (AdaptiveRule rule : AdaptiveRule.values()) counts.put(rule, 0L);
        long union = 0;

        for (int a=1;a<=46;a++) for (int b=a+1;b<=47;b++) for (int c=b+1;c<=48;c++)
            for (int d=c+1;d<=49;d++) for (int e=d+1;e<=50;e++) {
                var decision = snapshot.evaluate(List.of(a,b,c,d,e));
                if (decision.rejectedByAny()) union++;
                for (AdaptiveRule rule : decision.rejectedBy()) counts.merge(rule, 1L, Long::sum);
            }

        System.out.printf("ADAPTIVE_SPACE|history=%d|unionRejected=%d|unionRejectedPct=%.6f%n",
                snapshot.historySize(), union, 100.0 * union / 2_118_760.0);
        for (AdaptiveRule rule : AdaptiveRule.values()) {
            long count = counts.get(rule);
            System.out.printf("ADAPTIVE_SPACE_RULE|%s|rejected=%d|pct=%.6f%n", rule, count, 100.0 * count / 2_118_760.0);
        }
        assertTrue(union > 0);
    }

    private static List<List<Integer>> loadChronological() throws Exception {
        InputStream stream = AdaptiveRarityThresholdServiceTest.class.getResourceAsStream("/eurojackpot_archiv.csv");
        if (stream == null) throw new IllegalStateException("Missing archive");
        List<List<Integer>> draws = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] columns = line.split(",");
                draws.add(Arrays.stream(columns[1].split("-")).map(Integer::valueOf).sorted().toList());
            }
        }
        Collections.reverse(draws);
        return draws;
    }
}
