package com.lottery.generator;

import com.lottery.generator.rarity.RarityRules;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Extra hypotheses gathered from public lottery statistics sites and formalized locally. */
class ExtendedRareCombinationHypothesesTest {

    private static List<Draw> draws;

    @BeforeAll
    static void loadHistory() throws Exception {
        InputStream stream = ExtendedRareCombinationHypothesesTest.class.getClassLoader()
                .getResourceAsStream("eurojackpot_archiv.csv");
        assertNotNull(stream, "local eurojackpot_archiv.csv must be committed to the repository");

        List<Draw> loaded = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] columns = line.split(",");
                List<Integer> main = Arrays.stream(columns[1].split("-"))
                        .map(Integer::valueOf).sorted().collect(Collectors.toList());
                List<Integer> euro = Arrays.stream(columns[2].split("-"))
                        .map(Integer::valueOf).sorted().collect(Collectors.toList());
                loaded.add(new Draw(Instant.parse(columns[0]), main, euro));
            }
        }
        Collections.reverse(loaded);
        draws = Collections.unmodifiableList(loaded);
        assertTrue(draws.size() >= 987, "committed archive should be current through September 2026");
    }

    @Test
    void rule23_extremeHighLowSplit() {
        reportDraws("R23 all 5 numbers in same half", d -> {
            int high = RarityRules.highCount(d.main, 25);
            return high == 0 || high == 5;
        });
        reportDraws("R23 4+ numbers in same half", d -> {
            int high = RarityRules.highCount(d.main, 25);
            return high <= 1 || high >= 4;
        });
    }

    @Test
    void rule24_decadeConcentration() {
        reportDraws("R24 >=4 numbers in same decade", d -> RarityRules.maxCountInDecade(d.main) >= 4);
        reportDraws("R24 all 5 in same decade", d -> RarityRules.maxCountInDecade(d.main) == 5);
    }

    @Test
    void rule25_repeatedDecadeAndParitySignatures() {
        reportTransitions("R25 exact decade signature repeated", 1,
                i -> RarityRules.decadeSignature(draws.get(i - 1).main)
                        .equals(RarityRules.decadeSignature(draws.get(i).main)));
        reportTransitions("R25 exact positional parity signature repeated", 1,
                i -> RarityRules.paritySignature(draws.get(i - 1).main)
                        .equals(RarityRules.paritySignature(draws.get(i).main)));
    }

    @Test
    void rule26_consecutiveStructures() {
        reportDraws("R26 at least two consecutive adjacencies", d -> RarityRules.consecutivePairCount(d.main) >= 2);
        reportDraws("R26 consecutive run>=3", d -> RarityRules.maxConsecutiveRun(d.main) >= 3);
    }

    @Test
    void rule27_lastDigitRegularity() {
        reportDraws("R27 >=3 numbers share last digit", d -> RarityRules.maxSameLastDigitCount(d.main) >= 3);
    }

    @Test
    void rule28_moduloRegularity() {
        reportDraws("R28 >=4 numbers same mod3 class", d -> RarityRules.maxSameModuloCount(d.main, 3) >= 4);
        reportDraws("R28 all 5 numbers same mod3 class", d -> RarityRules.maxSameModuloCount(d.main, 3) == 5);
    }

    @Test
    void rule29_extremeMaximumGap() {
        reportDraws("R29 max adjacent gap>=25", d -> RarityRules.maxGap(d.main) >= 25);
        reportDraws("R29 max adjacent gap>=30", d -> RarityRules.maxGap(d.main) >= 30);
    }

    @Test
    void rule30_exactMainCombinationRepeatedHistorically() {
        long repeats = 0;
        Set<String> seen = new HashSet<>();
        for (Draw draw : draws) {
            String key = draw.main.toString();
            if (!seen.add(key)) repeats++;
        }
        report("R30 exact 5-number main combination repeated", repeats, draws.size());
    }

    @Test
    void rule31_euroNumbersRepeatImmediately() {
        reportTransitions("R31 both Euro numbers repeated from previous draw", 1,
                i -> RarityRules.intersection(draws.get(i - 1).euro, draws.get(i).euro) == 2);
        reportTransitions("R31 at least one Euro number repeated", 1,
                i -> RarityRules.intersection(draws.get(i - 1).euro, draws.get(i).euro) >= 1);
    }

    @Test
    void rule32_compoundExtremeTransitions() {
        reportTransitions("R32 low-sum -> low-sum", 1,
                i -> RarityRules.sum(draws.get(i - 1).main) <= 80 && RarityRules.sum(draws.get(i).main) <= 80);
        reportTransitions("R32 high-sum -> high-sum", 1,
                i -> RarityRules.sum(draws.get(i - 1).main) >= 175 && RarityRules.sum(draws.get(i).main) >= 175);
        reportTransitions("R32 narrow-span -> narrow-span", 1,
                i -> RarityRules.span(draws.get(i - 1).main) <= 20 && RarityRules.span(draws.get(i).main) <= 20);
        reportTransitions("R32 high-sum + wide-span repeated", 1,
                i -> RarityRules.sum(draws.get(i - 1).main) >= 175
                        && RarityRules.span(draws.get(i - 1).main) >= 40
                        && RarityRules.sum(draws.get(i).main) >= 175
                        && RarityRules.span(draws.get(i).main) >= 40);
    }

    private static void reportDraws(String name, Predicate<Draw> predicate) {
        long count = draws.stream().filter(predicate).count();
        report(name, count, draws.size());
    }

    private static void reportTransitions(String name, int historyRequired, IndexPredicate predicate) {
        long count = 0;
        long total = 0;
        for (int i = historyRequired; i < draws.size(); i++) {
            total++;
            if (predicate.test(i)) count++;
        }
        report(name, count, total);
    }

    private static void report(String name, long count, long total) {
        double percent = total == 0 ? 0 : count * 100.0 / total;
        System.out.printf("REPORT|%s|count=%d|total=%d|percent=%.6f|severity=%s%n",
                name, count, total, percent, severity(percent));
    }

    private static String severity(double percent) {
        if (percent < 0.5) return "VERY_RARE";
        if (percent < 2.0) return "RARE";
        if (percent < 5.0) return "UNCOMMON";
        return "COMMON";
    }

    private interface IndexPredicate { boolean test(int index); }

    private static final class Draw {
        final Instant date;
        final List<Integer> main;
        final List<Integer> euro;

        Draw(Instant date, List<Integer> main, List<Integer> euro) {
            this.date = date;
            this.main = main;
            this.euro = euro;
        }
    }
}
