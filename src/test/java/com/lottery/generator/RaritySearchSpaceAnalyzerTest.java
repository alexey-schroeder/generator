package com.lottery.generator;

import com.lottery.generator.rarity.RarityRules;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Exact search-space impact analysis for the historically VERY_RARE rule set.
 *
 * Normal tests enumerate all C(50,5)=2,118,760 candidates for the latest state and
 * backtest every real historical transition. Set -Drarity.fullBacktest=true to also
 * enumerate the full candidate space for every historical state.
 */
class RaritySearchSpaceAnalyzerTest {

    private static final int TOTAL_COMBINATIONS = 2_118_760;
    private static final List<String> RULE_ORDER = List.of(
            "R01_OVERLAP_3",
            "R02_RECENT_UNION_4",
            "R06_MULTI_STREAK",
            "R07_SORTED_L1_5",
            "R08_EQUAL_SHIFTS_4",
            "R09_GAP_L1_5",
            "R10_NEAR_PROGRESSION",
            "R12_RUN_4",
            "R13_HIGH_HIGH",
            "R17_MIRROR_L1_5",
            "R20_STRUCTURAL_STATE_REPEAT",
            "R24_ONE_DECADE",
            "R30_EXACT_REPEAT",
            "R32_HIGH_WIDE_REPEAT"
    );

    @Test
    void latestState_exactlyMeasuresVeryRareRulesAgainstWholeSearchSpace() throws Exception {
        List<Draw> draws = loadDrawsNewestFirst();
        State state = State.forNextDraw(draws, 0);
        SpaceResult result = enumerate(state);

        assertEquals(TOTAL_COMBINATIONS, result.total);
        assertTrue(result.remaining > 0);
        assertTrue(result.rejected > 0);

        System.out.printf("SPACE|latest=%s|total=%d|rejected=%d|rejectedPct=%.6f|remaining=%d|remainingPct=%.6f%n",
                draws.get(0).date, result.total, result.rejected, pct(result.rejected, result.total),
                result.remaining, pct(result.remaining, result.total));
        for (RuleImpact impact : result.impacts.values()) {
            System.out.printf("SPACE_RULE|%s|matched=%d|totalPct=%.6f|incremental=%d|incrementalPct=%.6f%n",
                    impact.rule, impact.matched, pct(impact.matched, result.total), impact.incremental,
                    pct(impact.incremental, result.total));
        }
    }

    @Test
    void allHistoricalNextDraws_areBacktestedAgainstVeryRareRules() throws Exception {
        List<Draw> newest = loadDrawsNewestFirst();
        List<Draw> chronological = new ArrayList<>(newest);
        Collections.reverse(chronological);

        int rejected = 0;
        Map<String, Integer> byRule = new LinkedHashMap<>();
        RULE_ORDER.forEach(rule -> byRule.put(rule, 0));

        for (int i = 2; i < chronological.size(); i++) {
            Draw candidate = chronological.get(i);
            State state = State.fromChronological(chronological, i);
            List<String> matched = matchedRules(candidate.main, state);
            if (!matched.isEmpty()) rejected++;
            for (String rule : matched) byRule.merge(rule, 1, Integer::sum);
        }

        int transitions = chronological.size() - 2;
        System.out.printf("BACKTEST|transitions=%d|actualRejected=%d|actualRejectedPct=%.6f|actualKept=%d|actualKeptPct=%.6f%n",
                transitions, rejected, pct(rejected, transitions), transitions - rejected,
                pct(transitions - rejected, transitions));
        byRule.forEach((rule, count) -> System.out.printf("BACKTEST_RULE|%s|actualRejected=%d|pct=%.6f%n",
                rule, count, pct(count, transitions)));

        // This is an observational backtest, not an assertion that the hypothesis must succeed.
        assertEquals(985, transitions);
    }

    @Test
    void optionalFullHistoricalSpaceBacktest() throws Exception {
        if (!Boolean.getBoolean("rarity.fullBacktest")) {
            System.out.println("FULL_SPACE_BACKTEST|skipped=true|enable=-Drarity.fullBacktest=true");
            return;
        }

        List<Draw> chronological = new ArrayList<>(loadDrawsNewestFirst());
        Collections.reverse(chronological);
        List<Double> remainingPcts = new ArrayList<>();
        int actualRejected = 0;

        for (int i = 2; i < chronological.size(); i++) {
            State state = State.fromChronological(chronological, i);
            SpaceResult result = enumerate(state);
            double remainingPct = pct(result.remaining, result.total);
            remainingPcts.add(remainingPct);
            if (!matchedRules(chronological.get(i).main, state).isEmpty()) actualRejected++;
            System.out.printf("FULL_SPACE_STATE|date=%s|remainingPct=%.6f|rejectedPct=%.6f%n",
                    chronological.get(i - 1).date, remainingPct, 100.0 - remainingPct);
        }

        Collections.sort(remainingPcts);
        double min = remainingPcts.get(0);
        double median = remainingPcts.get(remainingPcts.size() / 2);
        double max = remainingPcts.get(remainingPcts.size() - 1);
        double avg = remainingPcts.stream().mapToDouble(Double::doubleValue).average().orElseThrow();
        System.out.printf("FULL_SPACE_SUMMARY|states=%d|minRemainingPct=%.6f|medianRemainingPct=%.6f|avgRemainingPct=%.6f|maxRemainingPct=%.6f|actualRejected=%d%n",
                remainingPcts.size(), min, median, avg, max, actualRejected);
    }

    private SpaceResult enumerate(State state) {
        Map<String, MutableImpact> mutable = new LinkedHashMap<>();
        RULE_ORDER.forEach(rule -> mutable.put(rule, new MutableImpact()));
        int rejected = 0;

        int[] v = new int[5];
        for (int a = 1; a <= 46; a++) {
            v[0] = a;
            for (int b = a + 1; b <= 47; b++) {
                v[1] = b;
                for (int c = b + 1; c <= 48; c++) {
                    v[2] = c;
                    for (int d = c + 1; d <= 49; d++) {
                        v[3] = d;
                        for (int e = d + 1; e <= 50; e++) {
                            v[4] = e;
                            List<Integer> candidate = List.of(v[0], v[1], v[2], v[3], v[4]);
                            List<String> matched = matchedRules(candidate, state);
                            boolean alreadyRejected = false;
                            for (String rule : RULE_ORDER) {
                                if (matched.contains(rule)) {
                                    mutable.get(rule).matched++;
                                    if (!alreadyRejected) {
                                        mutable.get(rule).incremental++;
                                        alreadyRejected = true;
                                    }
                                }
                            }
                            if (alreadyRejected) rejected++;
                        }
                    }
                }
            }
        }

        Map<String, RuleImpact> impacts = new LinkedHashMap<>();
        mutable.forEach((rule, x) -> impacts.put(rule, new RuleImpact(rule, x.matched, x.incremental)));
        return new SpaceResult(TOTAL_COMBINATIONS, rejected, TOTAL_COMBINATIONS - rejected, impacts);
    }

    private static List<String> matchedRules(List<Integer> candidate, State s) {
        List<String> result = new ArrayList<>();
        if (RarityRules.intersection(candidate, s.previous) >= 3) result.add("R01_OVERLAP_3");

        Set<Integer> recentUnion = new HashSet<>(s.previous);
        recentUnion.addAll(s.previous2);
        long fromRecent = candidate.stream().filter(recentUnion::contains).count();
        if (fromRecent >= 4) result.add("R02_RECENT_UNION_4");

        Set<Integer> streak = new HashSet<>(s.previous);
        streak.retainAll(s.previous2);
        long continuingStreak = candidate.stream().filter(streak::contains).count();
        if (continuingStreak >= 2) result.add("R06_MULTI_STREAK");

        if (RarityRules.sortedL1Distance(s.previous, candidate) <= 5) result.add("R07_SORTED_L1_5");
        if (RarityRules.maxEqualShiftCount(s.previous, candidate) >= 4) result.add("R08_EQUAL_SHIFTS_4");
        if (RarityRules.gapDistance(s.previous, candidate) <= 5) result.add("R09_GAP_L1_5");
        if (RarityRules.gapRange(candidate) <= 2) result.add("R10_NEAR_PROGRESSION");
        if (RarityRules.maxConsecutiveRun(candidate) >= 4) result.add("R12_RUN_4");

        if (RarityRules.sum(s.previous) >= 175 && RarityRules.sum(candidate) >= 175) result.add("R13_HIGH_HIGH");
        if (RarityRules.mirrorL1Distance(s.previous, candidate, 50) <= 5) result.add("R17_MIRROR_L1_5");

        if (structuralState(s.previous).equals(structuralState(candidate))) result.add("R20_STRUCTURAL_STATE_REPEAT");
        if (RarityRules.maxCountInDecade(candidate) == 5) result.add("R24_ONE_DECADE");
        if (candidate.equals(s.previous)) result.add("R30_EXACT_REPEAT");

        boolean previousHighWide = RarityRules.sum(s.previous) >= 175 && RarityRules.span(s.previous) >= 45;
        boolean candidateHighWide = RarityRules.sum(candidate) >= 175 && RarityRules.span(candidate) >= 45;
        if (previousHighWide && candidateHighWide) result.add("R32_HIGH_WIDE_REPEAT");
        return result;
    }

    private static String structuralState(List<Integer> n) {
        return bucket(RarityRules.sum(n), 25) + "|" + bucket(RarityRules.span(n), 5) + "|" +
                RarityRules.oddCount(n) + "|" + RarityRules.decadeSignature(n) + "|" +
                RarityRules.maxConsecutiveRun(n);
    }

    private static int bucket(int value, int width) { return value / width; }
    private static double pct(long count, long total) { return 100.0 * count / total; }

    private static List<Draw> loadDrawsNewestFirst() throws Exception {
        InputStream stream = RaritySearchSpaceAnalyzerTest.class.getResourceAsStream("/eurojackpot_archiv.csv");
        if (stream == null) throw new IllegalStateException("Missing /eurojackpot_archiv.csv");
        List<Draw> draws = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] c = line.split(",");
                draws.add(new Draw(c[0].substring(0, 10), parseNumbers(c[1])));
            }
        }
        return draws;
    }

    private static List<Integer> parseNumbers(String value) {
        return Arrays.stream(value.split("-")).map(Integer::valueOf).sorted().toList();
    }

    private record Draw(String date, List<Integer> main) {}

    private record State(List<Integer> previous, List<Integer> previous2) {
        static State forNextDraw(List<Draw> newestFirst, int previousIndex) {
            return new State(newestFirst.get(previousIndex).main, newestFirst.get(previousIndex + 1).main);
        }
        static State fromChronological(List<Draw> chronological, int candidateIndex) {
            return new State(chronological.get(candidateIndex - 1).main, chronological.get(candidateIndex - 2).main);
        }
    }

    private static final class MutableImpact { int matched; int incremental; }
    private record RuleImpact(String rule, int matched, int incremental) {}
    private record SpaceResult(int total, int rejected, int remaining, Map<String, RuleImpact> impacts) {}
}
