package com.lottery.generator.rarity;

import java.util.*;
import java.util.function.ToDoubleFunction;

/**
 * Recalculates empirical rarity thresholds after every completed draw.
 *
 * The service is deliberately history-only: callers pass the draws that are known at the
 * recalculation point. The returned snapshot is then used to filter/rank candidates for the
 * NEXT draw. No future draw is required or inspected.
 */
public final class AdaptiveRarityThresholdService {

    public static final double DEFAULT_TARGET_KEEP = 0.90;
    public static final int DEFAULT_HOT_COLD_WINDOW = 20;

    public Snapshot recalculate(List<List<Integer>> chronologicalHistory) {
        return recalculate(chronologicalHistory, DEFAULT_TARGET_KEEP);
    }

    public Snapshot recalculate(List<List<Integer>> chronologicalHistory, double targetKeepRate) {
        if (chronologicalHistory.size() < 60) {
            throw new IllegalArgumentException("At least 60 historical draws are required");
        }
        if (!(targetKeepRate > 0.5 && targetKeepRate < 1.0)) {
            throw new IllegalArgumentException("targetKeepRate must be between 0.5 and 1.0");
        }

        List<List<Integer>> history = chronologicalHistory.stream().map(AdaptiveRarityThresholdService::sortedFive).toList();
        List<Integer> latest = history.get(history.size() - 1);

        RobustPositionModel robust = RobustPositionModel.fit(history);
        double robustCut = upperCut(history, robust::score, targetKeepRate);

        double[] entropy = history.stream().mapToDouble(AdaptiveRarityThresholdService::gapEntropy).toArray();
        TailCut entropyCut = centralTailCut(entropy, targetKeepRate);

        NumberRegularity regularity = NumberRegularity.fit(history);
        double regularityCut = lowerCut(history, regularity::score, targetKeepRate);

        AgeModel age = AgeModel.fit(history);
        double[] ageHistorical = age.historicalScores();
        TailCut ageCut = centralTailCut(ageHistorical, targetKeepRate);

        HighLowTransitionModel highLow = HighLowTransitionModel.fit(history);
        int highLowRareCountCut = highLow.cutoffForKeep(targetKeepRate);

        return new Snapshot(history.size(), targetKeepRate, latest,
                robust, robustCut,
                entropyCut,
                regularity, regularityCut,
                age, ageCut,
                highLow, highLowRareCountCut);
    }

    public record Snapshot(
            int historySize,
            double targetKeepRate,
            List<Integer> latestDraw,
            RobustPositionModel robustPositionModel,
            double robustPositionUpperCut,
            TailCut gapEntropyTailCut,
            NumberRegularity interArrivalRegularity,
            double interArrivalLowerCut,
            AgeModel ageModel,
            TailCut ageTailCut,
            HighLowTransitionModel highLowTransitionModel,
            int highLowRareCountCut) {

        public RuleDecision evaluate(List<Integer> candidate) {
            List<Integer> c = sortedFive(candidate);
            EnumSet<AdaptiveRule> rejected = EnumSet.noneOf(AdaptiveRule.class);
            if (robustPositionModel.score(c) >= robustPositionUpperCut) rejected.add(AdaptiveRule.ROBUST_POSITIONAL);
            double entropy = gapEntropy(c);
            if (gapEntropyTailCut.outside(entropy)) rejected.add(AdaptiveRule.GAP_ENTROPY);
            if (interArrivalRegularity.score(c) <= interArrivalLowerCut) rejected.add(AdaptiveRule.INTERARRIVAL_REGULARITY);
            double ageScore = ageModel.currentScore(c);
            if (ageTailCut.outside(ageScore)) rejected.add(AdaptiveRule.AGE_VECTOR);
            String from = highLowState(latestDraw);
            String to = highLowState(c);
            if (highLowTransitionModel.count(from, to) <= highLowRareCountCut) rejected.add(AdaptiveRule.HIGH_LOW_TRANSITION);
            return new RuleDecision(c, rejected);
        }
    }

    public enum AdaptiveRule {
        ROBUST_POSITIONAL,
        GAP_ENTROPY,
        INTERARRIVAL_REGULARITY,
        AGE_VECTOR,
        HIGH_LOW_TRANSITION
    }

    public record RuleDecision(List<Integer> candidate, Set<AdaptiveRule> rejectedBy) {
        public boolean rejectedBy(AdaptiveRule rule) { return rejectedBy.contains(rule); }
        public boolean rejectedByAny() { return !rejectedBy.isEmpty(); }
    }

    public record TailCut(double low, double high) {
        public boolean outside(double value) { return value <= low || value >= high; }
    }

    public static final class RobustPositionModel {
        private final double[] median = new double[5];
        private final double[] iqr = new double[5];

        static RobustPositionModel fit(List<List<Integer>> history) {
            RobustPositionModel m = new RobustPositionModel();
            for (int p = 0; p < 5; p++) {
                final int position = p;
                double[] values = history.stream().mapToDouble(d -> d.get(position)).toArray();
                m.median[p] = quantile(values, 0.50);
                m.iqr[p] = Math.max(1.0, quantile(values, 0.75) - quantile(values, 0.25));
            }
            return m;
        }

        public double score(List<Integer> draw) {
            List<Integer> s = sortedFive(draw);
            double score = 0;
            for (int i = 0; i < 5; i++) score += Math.abs(s.get(i) - median[i]) / iqr[i];
            return score;
        }

        public double[] medians() { return median.clone(); }
        public double[] iqrs() { return iqr.clone(); }
    }

    public static final class NumberRegularity {
        private final double[] cv = new double[51];

        static NumberRegularity fit(List<List<Integer>> history) {
            NumberRegularity r = new NumberRegularity();
            for (int n = 1; n <= 50; n++) {
                List<Integer> indexes = new ArrayList<>();
                for (int i = 0; i < history.size(); i++) if (history.get(i).contains(n)) indexes.add(i);
                if (indexes.size() < 3) { r.cv[n] = 9; continue; }
                double[] gaps = new double[indexes.size() - 1];
                for (int i = 1; i < indexes.size(); i++) gaps[i - 1] = indexes.get(i) - indexes.get(i - 1);
                double mean = Arrays.stream(gaps).average().orElse(1.0);
                double variance = 0;
                for (double g : gaps) variance += (g - mean) * (g - mean);
                r.cv[n] = Math.sqrt(variance / gaps.length) / mean;
            }
            return r;
        }

        public double score(List<Integer> draw) {
            return sortedFive(draw).stream().mapToDouble(n -> cv[n]).average().orElse(9.0);
        }
    }

    public static final class AgeModel {
        private final List<List<Integer>> history;
        private final int[] currentAge = new int[51];

        static AgeModel fit(List<List<Integer>> history) {
            return new AgeModel(history);
        }

        private AgeModel(List<List<Integer>> history) {
            this.history = history;
            Arrays.fill(currentAge, history.size() + 1);
            for (int n = 1; n <= 50; n++) {
                for (int i = history.size() - 1; i >= 0; i--) {
                    if (history.get(i).contains(n)) { currentAge[n] = history.size() - 1 - i; break; }
                }
            }
        }

        public double currentScore(List<Integer> draw) { return score(draw, currentAge); }

        public double[] historicalScores() {
            int[] last = new int[51];
            Arrays.fill(last, -1);
            List<Double> result = new ArrayList<>();
            for (int i = 0; i < history.size(); i++) {
                if (i > 20) {
                    int[] ages = new int[51];
                    for (int n = 1; n <= 50; n++) ages[n] = last[n] < 0 ? i : i - last[n];
                    result.add(score(history.get(i), ages));
                }
                for (int n : history.get(i)) last[n] = i;
            }
            return result.stream().mapToDouble(Double::doubleValue).toArray();
        }

        private double score(List<Integer> draw, int[] ages) {
            List<Integer> s = sortedFive(draw);
            double mean = s.stream().mapToInt(n -> ages[n]).average().orElse(0);
            int max = s.stream().mapToInt(n -> ages[n]).max().orElse(0);
            return mean + 0.35 * max;
        }
    }

    public static final class HighLowTransitionModel {
        private final Map<String, Integer> counts = new HashMap<>();
        private final List<Integer> observedCounts = new ArrayList<>();

        static HighLowTransitionModel fit(List<List<Integer>> history) {
            HighLowTransitionModel m = new HighLowTransitionModel();
            for (int i = 1; i < history.size(); i++) {
                String key = highLowState(history.get(i - 1)) + ">" + highLowState(history.get(i));
                m.counts.merge(key, 1, Integer::sum);
            }
            for (int i = 1; i < history.size(); i++) {
                m.observedCounts.add(m.count(highLowState(history.get(i - 1)), highLowState(history.get(i))));
            }
            return m;
        }

        public int count(String from, String to) { return counts.getOrDefault(from + ">" + to, 0); }

        int cutoffForKeep(double keep) {
            int[] counts = observedCounts.stream().mapToInt(Integer::intValue).sorted().toArray();
            int idx = (int) Math.floor((1.0 - keep) * counts.length);
            idx = Math.max(0, Math.min(counts.length - 1, idx));
            return counts[idx] - 1;
        }
    }

    private static double upperCut(List<List<Integer>> history, ToDoubleFunction<List<Integer>> score, double keep) {
        return quantile(history.stream().mapToDouble(score).toArray(), keep);
    }

    private static double lowerCut(List<List<Integer>> history, ToDoubleFunction<List<Integer>> score, double keep) {
        return quantile(history.stream().mapToDouble(score).toArray(), 1.0 - keep);
    }

    private static TailCut centralTailCut(double[] values, double keep) {
        double tail = (1.0 - keep) / 2.0;
        return new TailCut(quantile(values, tail), quantile(values, 1.0 - tail));
    }

    private static double quantile(double[] source, double q) {
        double[] a = source.clone();
        Arrays.sort(a);
        int index = (int) Math.ceil(q * a.length) - 1;
        index = Math.max(0, Math.min(a.length - 1, index));
        return a[index];
    }

    private static double gapEntropy(List<Integer> draw) {
        List<Integer> s = sortedFive(draw);
        int[] gaps = {s.get(1)-s.get(0), s.get(2)-s.get(1), s.get(3)-s.get(2), s.get(4)-s.get(3)};
        double total = Arrays.stream(gaps).sum();
        double entropy = 0;
        for (int g : gaps) { double p = g / total; entropy -= p * Math.log(p); }
        return entropy;
    }

    private static String highLowState(List<Integer> draw) {
        long high = sortedFive(draw).stream().filter(n -> n > 25).count();
        return Long.toString(high);
    }

    private static List<Integer> sortedFive(List<Integer> draw) {
        List<Integer> sorted = draw.stream().sorted().toList();
        if (sorted.size() != 5 || new HashSet<>(sorted).size() != 5) throw new IllegalArgumentException("Expected 5 unique main numbers");
        if (sorted.get(0) < 1 || sorted.get(4) > 50) throw new IllegalArgumentException("Main numbers must be in 1..50");
        return sorted;
    }
}
