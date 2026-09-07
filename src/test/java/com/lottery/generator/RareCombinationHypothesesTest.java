package com.lottery.generator;

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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Empirical checks for the hypotheses documented in RARE_COMBINATIONS.md.
 *
 * The tests intentionally do not fail when a hypothesis turns out to be common.
 * They print stable machine-readable REPORT lines so historical rarity can be
 * inspected without turning an empirical result into a build failure.
 */
class RareCombinationHypothesesTest {

    private static List<Draw> draws;

    @BeforeAll
    static void loadHistory() throws Exception {
        InputStream stream = RareCombinationHypothesesTest.class.getClassLoader()
                .getResourceAsStream("eurojackpot_archiv.csv");
        assertNotNull(stream, "eurojackpot_archiv.csv must be on the test classpath");

        List<Draw> loaded = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                String[] columns = line.split(",");
                List<Integer> numbers = Arrays.stream(columns[1].split("-"))
                        .map(Integer::valueOf)
                        .sorted()
                        .collect(Collectors.toList());
                loaded.add(new Draw(Instant.parse(columns[0]), numbers));
            }
        }

        // The resource is newest-first. All transition tests are easier to read oldest-first.
        Collections.reverse(loaded);
        draws = Collections.unmodifiableList(loaded);

        assertTrue(draws.size() > 100, "history should contain enough draws for empirical checks");
        assertTrue(draws.get(0).date.isBefore(draws.get(draws.size() - 1).date));
        System.out.println("HISTORY|draws=" + draws.size() + "|from=" + draws.get(0).date + "|to=" + draws.get(draws.size() - 1).date);
    }

    @Test
    void rule01_largeOverlapWithPreviousDraw() {
        reportTransitions("R01 overlap>=2 previous", 1, i -> intersection(draws.get(i), draws.get(i - 1)) >= 2);
        reportTransitions("R01 overlap>=3 previous", 1, i -> intersection(draws.get(i), draws.get(i - 1)) >= 3);
        reportTransitions("R01 overlap>=4 previous", 1, i -> intersection(draws.get(i), draws.get(i - 1)) >= 4);
    }

    @Test
    void rule02_largeOverlapWithRecentUnion() {
        reportTransitions("R02 >=4 numbers from union previous 2", 2, i -> overlapWithUnion(draws.get(i), i - 2, i) >= 4);
        reportTransitions("R02 5 numbers from union previous 2", 2, i -> overlapWithUnion(draws.get(i), i - 2, i) == 5);
    }

    @Test
    void rule03_repeatedPairFromRecentDraws() {
        reportTransitions("R03 pair repeated from previous draw", 1, i -> hasRepeatedSubset(draws.get(i), i - 1, i, 2));
        reportTransitions("R03 pair repeated within previous 5", 5, i -> hasRepeatedSubset(draws.get(i), i - 5, i, 2));
    }

    @Test
    void rule04_repeatedTripleFromRecentDraws() {
        reportTransitions("R04 triple repeated from previous draw", 1, i -> hasRepeatedSubset(draws.get(i), i - 1, i, 3));
        reportTransitions("R04 triple repeated within previous 13", 13, i -> hasRepeatedSubset(draws.get(i), i - 13, i, 3));
    }

    @Test
    void rule05_numberAppearsThreeDrawsInRow() {
        reportTransitions("R05 any number appears 3 draws in row", 2, i -> commonAcross(i - 2, i) >= 1);
        reportTransitions("R05 any number appears 4 draws in row", 3, i -> commonAcross(i - 3, i) >= 1);
    }

    @Test
    void rule06_severalNumbersContinueStreakTogether() {
        reportTransitions("R06 >=2 numbers appear 3 draws in row", 2, i -> commonAcross(i - 2, i) >= 2);
    }

    @Test
    void rule07_candidateTooSimilarToPreviousSortedDraw() {
        reportTransitions("R07 sorted L1 distance <=5", 1, i -> sortedDistance(draws.get(i), draws.get(i - 1)) <= 5);
        reportTransitions("R07 sorted L1 distance <=10", 1, i -> sortedDistance(draws.get(i), draws.get(i - 1)) <= 10);
    }

    @Test
    void rule08_almostConstantShift() {
        reportTransitions("R08 >=4 equal shifts", 1, i -> maxEqualShiftCount(draws.get(i - 1), draws.get(i)) >= 4);
        reportTransitions("R08 5 equal shifts", 1, i -> maxEqualShiftCount(draws.get(i - 1), draws.get(i)) == 5);
    }

    @Test
    void rule09_similarGapPattern() {
        reportTransitions("R09 gap L1 distance <=3", 1, i -> gapDistance(draws.get(i - 1), draws.get(i)) <= 3);
        reportTransitions("R09 gap L1 distance <=5", 1, i -> gapDistance(draws.get(i - 1), draws.get(i)) <= 5);
    }

    @Test
    void rule10_arithmeticOrNearArithmeticProgression() {
        reportDraws("R10 exact arithmetic progression", d -> gapRange(d) == 0);
        reportDraws("R10 near arithmetic progression gap range<=2", d -> gapRange(d) <= 2);
    }

    @Test
    void rule11_denseLocalCluster() {
        reportDraws("R11 total span<=10", d -> span(d) <= 10);
        reportDraws("R11 4 numbers inside width<=8", d -> minWindow(d, 4) <= 8);
        reportDraws("R11 3 numbers inside width<=4", d -> minWindow(d, 3) <= 4);
    }

    @Test
    void rule12_tooManyConsecutiveNumbers() {
        reportDraws("R12 consecutive run>=3", d -> maxConsecutiveRun(d) >= 3);
        reportDraws("R12 consecutive run>=4", d -> maxConsecutiveRun(d) >= 4);
    }

    @Test
    void rule13_repeatingExtremeSumState() {
        reportDraws("R13 single low sum<=80", d -> sum(d) <= 80);
        reportDraws("R13 single high sum>=175", d -> sum(d) >= 175);
        reportTransitions("R13 low sum repeated", 1, i -> sum(draws.get(i - 1)) <= 80 && sum(draws.get(i)) <= 80);
        reportTransitions("R13 high sum repeated", 1, i -> sum(draws.get(i - 1)) >= 175 && sum(draws.get(i)) >= 175);
    }

    @Test
    void rule14_repeatingExtremeSpanState() {
        reportDraws("R14 single narrow span<=20", d -> span(d) <= 20);
        reportDraws("R14 single wide span>=45", d -> span(d) >= 45);
        reportTransitions("R14 narrow span repeated", 1, i -> span(draws.get(i - 1)) <= 20 && span(draws.get(i)) <= 20);
        reportTransitions("R14 wide span repeated", 1, i -> span(draws.get(i - 1)) >= 45 && span(draws.get(i)) >= 45);
    }

    @Test
    void rule15_repeatingExtremeParityPattern() {
        reportDraws("R15 single extreme parity 0/1/4/5 odd", d -> isExtremeParity(d));
        reportTransitions("R15 same extreme odd count repeated", 1, i -> {
            int previous = oddCount(draws.get(i - 1));
            int current = oddCount(draws.get(i));
            return previous == current && (current <= 1 || current >= 4);
        });
    }

    @Test
    void rule16_repeatingSameRangeDistribution() {
        reportTransitions("R16 exact decade distribution repeated", 1,
                i -> rangeSignature(draws.get(i - 1)).equals(rangeSignature(draws.get(i))));
    }

    @Test
    void rule17_mirrorLikeTransformation() {
        reportTransitions("R17 mirror L1 distance<=5", 1, i -> mirrorDistance(draws.get(i - 1), draws.get(i)) <= 5);
        reportTransitions("R17 mirror L1 distance<=10", 1, i -> mirrorDistance(draws.get(i - 1), draws.get(i)) <= 10);
    }

    @Test
    void rule18_repeatedAppearanceInterval() {
        reportTransitions("R18 any number completes equal pause pattern", 1, this::anyNumberCompletesEqualPause);
    }

    @Test
    void rule19_repeatedPairAppearanceInterval() {
        reportTransitions("R19 any pair completes equal pause pattern", 1, this::anyPairCompletesEqualPause);
    }

    @Test
    void rule20_rareTransitionBetweenStructuralStates() {
        reportTransitions("R20 full structural state repeated", 1,
                i -> structuralState(draws.get(i - 1)).equals(structuralState(draws.get(i))));
        reportTransitions("R20 same sum+span state repeated", 1,
                i -> sumState(draws.get(i - 1)).equals(sumState(draws.get(i)))
                        && spanState(draws.get(i - 1)).equals(spanState(draws.get(i))));
    }

    @Test
    void rule21_generalHistoricalRarityScore() {
        Map<String, Integer> states = new HashMap<>();
        for (Draw draw : draws) {
            states.merge(structuralState(draw), 1, Integer::sum);
        }
        long rare = states.values().stream().filter(count -> count * 100.0 / draws.size() < 0.5).count();
        long strong = states.values().stream().filter(count -> count * 100.0 / draws.size() < 2.0).count();
        long moderate = states.values().stream().filter(count -> count * 100.0 / draws.size() < 5.0).count();
        System.out.printf("REPORT|R21 structural-state rarity buckets|unique=%d|lt0.5pct=%d|lt2pct=%d|lt5pct=%d%n",
                states.size(), rare, strong, moderate);
        assertTrue(states.size() > 1);
    }

    @Test
    void rule22_conditionalRarity() {
        reportConditional("R22 P(overlap>=2 | previous sum<=80)", 1,
                i -> sum(draws.get(i - 1)) <= 80,
                i -> intersection(draws.get(i - 1), draws.get(i)) >= 2);
        reportConditional("R22 P(narrow span | previous narrow span)", 1,
                i -> span(draws.get(i - 1)) <= 20,
                i -> span(draws.get(i)) <= 20);
        reportConditional("R22 P(>=4 odd | previous >=4 odd)", 1,
                i -> oddCount(draws.get(i - 1)) >= 4,
                i -> oddCount(draws.get(i)) >= 4);
    }

    private void reportDraws(String name, Predicate<Draw> predicate) {
        long count = draws.stream().filter(predicate).count();
        report(name, count, draws.size());
    }

    private void reportTransitions(String name, int historyRequired, IndexPredicate predicate) {
        long count = 0;
        long total = 0;
        for (int i = historyRequired; i < draws.size(); i++) {
            total++;
            if (predicate.test(i)) {
                count++;
            }
        }
        report(name, count, total);
    }

    private void reportConditional(String name, int historyRequired, IndexPredicate condition, IndexPredicate event) {
        long denominator = 0;
        long numerator = 0;
        for (int i = historyRequired; i < draws.size(); i++) {
            if (condition.test(i)) {
                denominator++;
                if (event.test(i)) {
                    numerator++;
                }
            }
        }
        report(name, numerator, denominator);
    }

    private static void report(String name, long count, long total) {
        double percent = total == 0 ? 0.0 : count * 100.0 / total;
        System.out.printf("REPORT|%s|count=%d|total=%d|percent=%.6f|severity=%s%n",
                name, count, total, percent, severity(percent));
    }

    private static String severity(double percent) {
        if (percent < 0.5) return "VERY_RARE";
        if (percent < 2.0) return "RARE";
        if (percent < 5.0) return "UNCOMMON";
        return "COMMON";
    }

    private static int intersection(Draw a, Draw b) {
        Set<Integer> set = new HashSet<>(a.numbers);
        set.retainAll(b.numbers);
        return set.size();
    }

    private static int overlapWithUnion(Draw candidate, int fromInclusive, int toExclusive) {
        Set<Integer> union = new HashSet<>();
        for (int i = fromInclusive; i < toExclusive; i++) {
            union.addAll(draws.get(i).numbers);
        }
        return (int) candidate.numbers.stream().filter(union::contains).count();
    }

    private static boolean hasRepeatedSubset(Draw candidate, int fromInclusive, int toExclusive, int subsetSize) {
        for (int i = fromInclusive; i < toExclusive; i++) {
            if (intersection(candidate, draws.get(i)) >= subsetSize) {
                return true;
            }
        }
        return false;
    }

    private static int commonAcross(int fromInclusive, int toInclusive) {
        Set<Integer> common = new HashSet<>(draws.get(fromInclusive).numbers);
        for (int i = fromInclusive + 1; i <= toInclusive; i++) {
            common.retainAll(draws.get(i).numbers);
        }
        return common.size();
    }

    private static int sortedDistance(Draw a, Draw b) {
        int result = 0;
        for (int i = 0; i < 5; i++) {
            result += Math.abs(a.numbers.get(i) - b.numbers.get(i));
        }
        return result;
    }

    private static int maxEqualShiftCount(Draw previous, Draw current) {
        Map<Integer, Integer> frequencies = new HashMap<>();
        for (int i = 0; i < 5; i++) {
            int shift = current.numbers.get(i) - previous.numbers.get(i);
            frequencies.merge(shift, 1, Integer::sum);
        }
        return frequencies.values().stream().mapToInt(Integer::intValue).max().orElse(0);
    }

    private static List<Integer> gaps(Draw draw) {
        List<Integer> result = new ArrayList<>(4);
        for (int i = 1; i < draw.numbers.size(); i++) {
            result.add(draw.numbers.get(i) - draw.numbers.get(i - 1));
        }
        return result;
    }

    private static int gapDistance(Draw a, Draw b) {
        List<Integer> ga = gaps(a);
        List<Integer> gb = gaps(b);
        int result = 0;
        for (int i = 0; i < ga.size(); i++) {
            result += Math.abs(ga.get(i) - gb.get(i));
        }
        return result;
    }

    private static int gapRange(Draw draw) {
        List<Integer> gaps = gaps(draw);
        return Collections.max(gaps) - Collections.min(gaps);
    }

    private static int span(Draw draw) {
        return draw.numbers.get(4) - draw.numbers.get(0);
    }

    private static int minWindow(Draw draw, int amount) {
        int min = Integer.MAX_VALUE;
        for (int i = 0; i + amount <= draw.numbers.size(); i++) {
            min = Math.min(min, draw.numbers.get(i + amount - 1) - draw.numbers.get(i));
        }
        return min;
    }

    private static int maxConsecutiveRun(Draw draw) {
        int max = 1;
        int current = 1;
        for (int i = 1; i < draw.numbers.size(); i++) {
            if (draw.numbers.get(i) == draw.numbers.get(i - 1) + 1) {
                current++;
                max = Math.max(max, current);
            } else {
                current = 1;
            }
        }
        return max;
    }

    private static int sum(Draw draw) {
        return draw.numbers.stream().mapToInt(Integer::intValue).sum();
    }

    private static int oddCount(Draw draw) {
        return (int) draw.numbers.stream().filter(n -> n % 2 != 0).count();
    }

    private static boolean isExtremeParity(Draw draw) {
        int odd = oddCount(draw);
        return odd <= 1 || odd >= 4;
    }

    private static String rangeSignature(Draw draw) {
        int[] bins = new int[5];
        for (int n : draw.numbers) {
            bins[(n - 1) / 10]++;
        }
        return Arrays.toString(bins);
    }

    private static int mirrorDistance(Draw previous, Draw current) {
        List<Integer> mirror = previous.numbers.stream()
                .map(n -> 51 - n)
                .sorted()
                .collect(Collectors.toList());
        int result = 0;
        for (int i = 0; i < 5; i++) {
            result += Math.abs(mirror.get(i) - current.numbers.get(i));
        }
        return result;
    }

    private boolean anyNumberCompletesEqualPause(int currentIndex) {
        Draw current = draws.get(currentIndex);
        for (int number : current.numbers) {
            List<Integer> appearances = new ArrayList<>();
            for (int i = 0; i <= currentIndex; i++) {
                if (draws.get(i).numbers.contains(number)) {
                    appearances.add(i);
                }
            }
            if (appearances.size() >= 3) {
                int n = appearances.size();
                int lastGap = appearances.get(n - 1) - appearances.get(n - 2);
                int previousGap = appearances.get(n - 2) - appearances.get(n - 3);
                if (lastGap == previousGap) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean anyPairCompletesEqualPause(int currentIndex) {
        List<String> currentPairs = pairs(draws.get(currentIndex));
        for (String pair : currentPairs) {
            List<Integer> appearances = new ArrayList<>();
            for (int i = 0; i <= currentIndex; i++) {
                if (pairs(draws.get(i)).contains(pair)) {
                    appearances.add(i);
                }
            }
            if (appearances.size() >= 3) {
                int n = appearances.size();
                int lastGap = appearances.get(n - 1) - appearances.get(n - 2);
                int previousGap = appearances.get(n - 2) - appearances.get(n - 3);
                if (lastGap == previousGap) {
                    return true;
                }
            }
        }
        return false;
    }

    private static List<String> pairs(Draw draw) {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < draw.numbers.size(); i++) {
            for (int j = i + 1; j < draw.numbers.size(); j++) {
                result.add(draw.numbers.get(i) + "-" + draw.numbers.get(j));
            }
        }
        return result;
    }

    private static String structuralState(Draw draw) {
        return sumState(draw) + "|" + spanState(draw) + "|odd=" + oddCount(draw)
                + "|run=" + Math.min(maxConsecutiveRun(draw), 3)
                + "|ranges=" + rangeSignature(draw);
    }

    private static String sumState(Draw draw) {
        int sum = sum(draw);
        if (sum <= 80) return "SUM_LOW";
        if (sum >= 175) return "SUM_HIGH";
        return "SUM_NORMAL";
    }

    private static String spanState(Draw draw) {
        int span = span(draw);
        if (span <= 20) return "SPAN_NARROW";
        if (span >= 45) return "SPAN_WIDE";
        return "SPAN_NORMAL";
    }

    @FunctionalInterface
    private interface IndexPredicate {
        boolean test(int index);
    }

    private static final class Draw {
        private final Instant date;
        private final List<Integer> numbers;

        private Draw(Instant date, List<Integer> numbers) {
            this.date = date;
            this.numbers = Collections.unmodifiableList(new ArrayList<>(numbers));
            assertEquals(5, this.numbers.size());
        }
    }
}
