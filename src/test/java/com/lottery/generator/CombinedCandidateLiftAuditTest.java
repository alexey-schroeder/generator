package com.lottery.generator;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Walk-forward audit for a YES/NO candidate service.
 *
 * Rules are selected only on the earlier evaluation segment. The final segment is untouched
 * holdout. Search-space rates during selection/holdout use a deterministic uniform sample of
 * 10,000 of the C(50,5) main-number combinations. Final current-state space for every selected
 * configuration is enumerated exactly over all 2,118,760 combinations.
 */
class CombinedCandidateLiftAuditTest {
    private static final int TOTAL = 2_118_760;
    private static final int WARMUP = 120;
    private static final int SAMPLE_SIZE = 10_000;
    private static final long SAMPLE_SEED = 20260908L;

    private static final String[] RULES = {
            "OVERLAP_3", "RECENT_UNION_4", "MULTI_STREAK", "SORTED_L1_5",
            "EQUAL_SHIFTS_4", "GAP_L1_5", "NEAR_PROGRESSION", "RUN_4",
            "MIRROR_L1_5", "ONE_DECADE", "HIGH_HIGH", "LOW_LOW",
            "NARROW_NARROW", "ALL_SAME_PARITY", "HIGH_SUM_175", "LOW_SUM_80",
            "NARROW_SPAN_20", "MAX_GAP_30", "RUN_3", "SAME_HALF_ALL_5"
    };

    private enum PositionalBase {
        NONE(0, 2), P98_SOFT(98, 2), P95_SOFT(95, 2), P95_STRICT(95, 1), P89_SOFT(89, 2);
        final int coveragePct;
        final int rejectOutside;
        PositionalBase(int coveragePct, int rejectOutside) {
            this.coveragePct = coveragePct;
            this.rejectOutside = rejectOutside;
        }
    }

    @Test
    void selectsRuleSubsetsOnPastAndMeasuresProbabilityLiftOnUntouchedFuture() throws Exception {
        List<int[]> history = loadChronological();
        List<int[]> sample = uniformReservoirSample();
        assertEquals(987, history.size());
        assertEquals(SAMPLE_SIZE, sample.size());

        int evaluatedStates = history.size() - WARMUP;
        int selectionStates = (int) Math.floor(evaluatedStates * 0.60);
        int holdoutStates = evaluatedStates - selectionStates;
        System.out.printf("LIFT_SETUP|draws=%d|warmup=%d|evaluated=%d|selection=%d|holdout=%d|spaceSample=%d|seed=%d%n",
                history.size(), WARMUP, evaluatedStates, selectionStates, holdoutStates, SAMPLE_SIZE, SAMPLE_SEED);

        EnumMap<PositionalBase, Dataset> data = new EnumMap<>(PositionalBase.class);
        for (PositionalBase base : PositionalBase.values()) data.put(base, new Dataset(selectionStates, holdoutStates));

        for (int i = WARMUP; i < history.size(); i++) {
            boolean selection = i - WARMUP < selectionStates;
            List<int[]> past = history.subList(0, i);
            int[] previous = history.get(i - 1);
            int[] previous2 = history.get(i - 2);
            PositionBounds p98 = PositionBounds.fit(past, 0.98);
            PositionBounds p95 = PositionBounds.fit(past, 0.95);
            PositionBounds p89 = PositionBounds.fit(past, 0.89);

            for (int[] candidate : sample) {
                int rules = rejectionMask(candidate, previous, previous2);
                for (PositionalBase base : PositionalBase.values()) {
                    boolean baseRejected = positionalRejected(base, candidate, p98, p95, p89);
                    data.get(base).addSpace(selection, rules, baseRejected);
                }
            }

            int[] actual = history.get(i);
            int actualRules = rejectionMask(actual, previous, previous2);
            for (PositionalBase base : PositionalBase.values()) {
                boolean baseRejected = positionalRejected(base, actual, p98, p95, p89);
                data.get(base).addActual(selection, actualRules, baseRejected);
            }
        }

        for (PositionalBase base : PositionalBase.values()) {
            Dataset d = data.get(base);
            printMetric("BASE", base.name(), 0, d.selectionMetric(0), d.holdoutMetric(0));
        }

        List<Chosen> chosen = new ArrayList<>();
        for (double target : new double[]{0.99, 0.95, 0.90}) {
            for (PositionalBase base : List.of(PositionalBase.NONE, PositionalBase.P98_SOFT, PositionalBase.P95_SOFT)) {
                Dataset d = data.get(base);
                int subset = greedySelect(d, target);
                Metric sel = d.selectionMetric(subset);
                Metric hold = d.holdoutMetric(subset);
                printMetric("SELECTED", base.name() + "_KEEP" + (int)Math.round(target*100), subset, sel, hold);
                chosen.add(new Chosen(base, target, subset, hold));
            }
        }

        // User-proposed examples: positional generation plus intuitive structural bans.
        int parity = bit("ALL_SAME_PARITY");
        int intuitive = parity | bit("HIGH_SUM_175") | bit("NARROW_SPAN_20") | bit("MAX_GAP_30") | bit("RUN_3");
        for (PositionalBase base : List.of(PositionalBase.P95_STRICT, PositionalBase.P95_SOFT, PositionalBase.P89_SOFT)) {
            Dataset d = data.get(base);
            printMetric("MANUAL", base.name() + "_PARITY", parity, d.selectionMetric(parity), d.holdoutMetric(parity));
            printMetric("MANUAL", base.name() + "_INTUITIVE", intuitive, d.selectionMetric(intuitive), d.holdoutMetric(intuitive));
        }

        // Exact current-state space for configurations selected without seeing holdout.
        List<int[]> past = history;
        int[] previous = history.get(history.size() - 1);
        int[] previous2 = history.get(history.size() - 2);
        PositionBounds p98 = PositionBounds.fit(past, 0.98);
        PositionBounds p95 = PositionBounds.fit(past, 0.95);
        PositionBounds p89 = PositionBounds.fit(past, 0.89);
        for (Chosen c : chosen) {
            long accepted = exactCurrentAccepted(c.base, c.subset, previous, previous2, p98, p95, p89);
            double acceptedRate = accepted / (double) TOTAL;
            double impliedLift = acceptedRate == 0 ? Double.NaN : c.holdout.keepRate / acceptedRate;
            System.out.printf(Locale.ROOT,
                    "LIFT_CURRENT|base=%s|targetKeep=%.2f|rules=%s|accepted=%d|acceptedPct=%.6f|holdoutKeepPct=%.6f|currentImpliedLift=%.6f|currentImpliedGainPct=%.6f%n",
                    c.base, c.target, names(c.subset), accepted, acceptedRate*100.0,
                    c.holdout.keepRate*100.0, impliedLift, (impliedLift-1.0)*100.0);
        }

        assertTrue(chosen.stream().allMatch(c -> c.holdout.spaceAcceptedRate > 0));
    }

    private static int greedySelect(Dataset d, double targetKeep) {
        int subset = 0;
        Metric current = d.selectionMetric(subset);
        while (true) {
            int bestRule = -1;
            Metric best = current;
            for (int r = 0; r < RULES.length; r++) {
                int b = 1 << r;
                if ((subset & b) != 0) continue;
                Metric m = d.selectionMetric(subset | b);
                if (m.keepRate + 1e-12 < targetKeep) continue;
                if (m.lift > best.lift + 1e-5 ||
                        (Math.abs(m.lift - best.lift) <= 1e-5 && m.spaceAcceptedRate < best.spaceAcceptedRate)) {
                    bestRule = r;
                    best = m;
                }
            }
            if (bestRule < 0) break;
            subset |= 1 << bestRule;
            current = best;
        }
        return subset;
    }

    private static void printMetric(String kind, String name, int subset, Metric selection, Metric holdout) {
        System.out.printf(Locale.ROOT,
                "LIFT_%s|name=%s|rules=%s|selectionSpaceAcceptedPct=%.6f|selectionKeepPct=%.6f|selectionLift=%.6f|holdoutSpaceAcceptedPct=%.6f|holdoutKeepPct=%.6f|holdoutLift=%.6f|holdoutGainPct=%.6f%n",
                kind, name, names(subset), selection.spaceAcceptedRate*100.0, selection.keepRate*100.0, selection.lift,
                holdout.spaceAcceptedRate*100.0, holdout.keepRate*100.0, holdout.lift, (holdout.lift-1.0)*100.0);
    }

    private static long exactCurrentAccepted(PositionalBase base, int subset, int[] prev, int[] prev2,
                                             PositionBounds p98, PositionBounds p95, PositionBounds p89) {
        long accepted = 0;
        int[] c = new int[5];
        for (int a=1;a<=46;a++) { c[0]=a;
            for (int b=a+1;b<=47;b++) { c[1]=b;
                for (int d2=b+1;d2<=48;d2++) { c[2]=d2;
                    for (int d=d2+1;d<=49;d++) { c[3]=d;
                        for (int e=d+1;e<=50;e++) { c[4]=e;
                            if (positionalRejected(base,c,p98,p95,p89)) continue;
                            if ((rejectionMask(c,prev,prev2) & subset) == 0) accepted++;
                        }
                    }
                }
            }
        }
        return accepted;
    }

    private static boolean positionalRejected(PositionalBase base, int[] c, PositionBounds p98, PositionBounds p95, PositionBounds p89) {
        return switch (base) {
            case NONE -> false;
            case P98_SOFT -> p98.outside(c) >= 2;
            case P95_SOFT -> p95.outside(c) >= 2;
            case P95_STRICT -> p95.outside(c) >= 1;
            case P89_SOFT -> p89.outside(c) >= 2;
        };
    }

    private static int rejectionMask(int[] c, int[] prev, int[] prev2) {
        int m = 0;
        if (intersection(c,prev) >= 3) m |= bit("OVERLAP_3");
        if (intersectionWithUnion(c,prev,prev2) >= 4) m |= bit("RECENT_UNION_4");
        if (continuingStreak(c,prev,prev2) >= 2) m |= bit("MULTI_STREAK");
        if (sortedL1(c,prev) <= 5) m |= bit("SORTED_L1_5");
        if (maxEqualShift(c,prev) >= 4) m |= bit("EQUAL_SHIFTS_4");
        if (gapL1(c,prev) <= 5) m |= bit("GAP_L1_5");
        if (gapRange(c) <= 2) m |= bit("NEAR_PROGRESSION");
        if (maxRun(c) >= 4) m |= bit("RUN_4");
        if (mirrorL1(c,prev) <= 5) m |= bit("MIRROR_L1_5");
        if (maxDecade(c) == 5) m |= bit("ONE_DECADE");
        if (sum(prev) >= 175 && sum(c) >= 175) m |= bit("HIGH_HIGH");
        if (sum(prev) <= 80 && sum(c) <= 80) m |= bit("LOW_LOW");
        if (span(prev) <= 20 && span(c) <= 20) m |= bit("NARROW_NARROW");
        int odd = oddCount(c);
        if (odd == 0 || odd == 5) m |= bit("ALL_SAME_PARITY");
        if (sum(c) >= 175) m |= bit("HIGH_SUM_175");
        if (sum(c) <= 80) m |= bit("LOW_SUM_80");
        if (span(c) <= 20) m |= bit("NARROW_SPAN_20");
        if (maxGap(c) >= 30) m |= bit("MAX_GAP_30");
        if (maxRun(c) >= 3) m |= bit("RUN_3");
        if (allSameHalf(c)) m |= bit("SAME_HALF_ALL_5");
        return m;
    }

    private static int bit(String name) {
        for (int i=0;i<RULES.length;i++) if (RULES[i].equals(name)) return 1 << i;
        throw new IllegalArgumentException(name);
    }

    private static String names(int mask) {
        if (mask == 0) return "NONE";
        StringJoiner j = new StringJoiner("+");
        for (int i=0;i<RULES.length;i++) if ((mask & (1<<i)) != 0) j.add(RULES[i]);
        return j.toString();
    }

    private static final class Dataset {
        final IntStore selectionSpace = new IntStore();
        final IntStore holdoutSpace = new IntStore();
        final IntStore selectionActual = new IntStore();
        final IntStore holdoutActual = new IntStore();
        long selectionSpaceBaseAccepted, holdoutSpaceBaseAccepted;
        long selectionActualBaseAccepted, holdoutActualBaseAccepted;
        final int selectionStates, holdoutStates;

        Dataset(int selectionStates, int holdoutStates) { this.selectionStates=selectionStates; this.holdoutStates=holdoutStates; }
        void addSpace(boolean selection, int mask, boolean baseRejected) {
            if (baseRejected) return;
            if (selection) { selectionSpace.add(mask); selectionSpaceBaseAccepted++; }
            else { holdoutSpace.add(mask); holdoutSpaceBaseAccepted++; }
        }
        void addActual(boolean selection, int mask, boolean baseRejected) {
            if (baseRejected) return;
            if (selection) { selectionActual.add(mask); selectionActualBaseAccepted++; }
            else { holdoutActual.add(mask); holdoutActualBaseAccepted++; }
        }
        Metric selectionMetric(int subset) { return metric(selectionSpace, selectionActual, selectionStates, subset); }
        Metric holdoutMetric(int subset) { return metric(holdoutSpace, holdoutActual, holdoutStates, subset); }
        private Metric metric(IntStore space, IntStore actual, int states, int subset) {
            long acceptedSpace = space.countCompatible(subset);
            long acceptedActual = actual.countCompatible(subset);
            double spaceRate = acceptedSpace / (double)(states * SAMPLE_SIZE);
            double keep = acceptedActual / (double)states;
            double lift = spaceRate == 0 ? Double.NaN : keep / spaceRate;
            return new Metric(spaceRate, keep, lift, acceptedSpace, acceptedActual);
        }
    }

    private record Metric(double spaceAcceptedRate, double keepRate, double lift, long acceptedSpace, long acceptedActual) {}
    private record Chosen(PositionalBase base, double target, int subset, Metric holdout) {}

    private static final class IntStore {
        private int[] values = new int[1 << 16];
        private int size;
        void add(int v) { if (size==values.length) values=Arrays.copyOf(values,values.length*2); values[size++]=v; }
        long countCompatible(int subset) {
            long n=0;
            for (int i=0;i<size;i++) if ((values[i]&subset)==0) n++;
            return n;
        }
    }

    private record PositionBounds(int[] low, int[] high) {
        static PositionBounds fit(List<int[]> h, double coverage) {
            int[] lo=new int[5], hi=new int[5];
            double tail=(1.0-coverage)/2.0;
            for (int p=0;p<5;p++) {
                int[] v=new int[h.size()];
                for (int i=0;i<h.size();i++) v[i]=h.get(i)[p];
                Arrays.sort(v);
                lo[p]=nearest(v,tail); hi[p]=nearest(v,1.0-tail);
            }
            return new PositionBounds(lo,hi);
        }
        int outside(int[] c) { int n=0; for(int i=0;i<5;i++) if(c[i]<low[i]||c[i]>high[i]) n++; return n; }
        private static int nearest(int[] v,double q) { int r=(int)Math.ceil(q*v.length); r=Math.max(1,Math.min(v.length,r)); return v[r-1]; }
    }

    private static List<int[]> uniformReservoirSample() {
        Random rnd = new Random(SAMPLE_SEED);
        int[][] reservoir = new int[SAMPLE_SIZE][];
        long seen=0;
        for(int a=1;a<=46;a++) for(int b=a+1;b<=47;b++) for(int c=b+1;c<=48;c++) for(int d=c+1;d<=49;d++) for(int e=d+1;e<=50;e++) {
            int[] x={a,b,c,d,e};
            seen++;
            if(seen<=SAMPLE_SIZE) reservoir[(int)seen-1]=x;
            else {
                long j=nextLong(rnd,seen);
                if(j<SAMPLE_SIZE) reservoir[(int)j]=x;
            }
        }
        assertEquals(TOTAL,seen);
        return Arrays.asList(reservoir);
    }

    private static long nextLong(Random r,long bound) {
        long bits, val;
        do { bits=r.nextLong()>>>1; val=bits%bound; } while(bits-val+(bound-1)<0L);
        return val;
    }

    private static List<int[]> loadChronological() throws Exception {
        InputStream stream=CombinedCandidateLiftAuditTest.class.getResourceAsStream("/eurojackpot_archiv.csv");
        if(stream==null) throw new IllegalStateException("Missing archive");
        List<int[]> draws=new ArrayList<>();
        try(BufferedReader br=new BufferedReader(new InputStreamReader(stream,StandardCharsets.UTF_8))) {
            String line; while((line=br.readLine())!=null) { if(line.isBlank()) continue; String[] c=line.split(",");
                int[] n=Arrays.stream(c[1].split("-")).mapToInt(Integer::parseInt).sorted().toArray(); draws.add(n); }
        }
        Collections.reverse(draws); return draws;
    }

    private static int intersection(int[] a,int[] b){int n=0,i=0,j=0;while(i<5&&j<5){if(a[i]==b[j]){n++;i++;j++;}else if(a[i]<b[j])i++;else j++;}return n;}
    private static int intersectionWithUnion(int[] c,int[] a,int[] b){int n=0;for(int x:c)if(contains(a,x)||contains(b,x))n++;return n;}
    private static int continuingStreak(int[] c,int[] a,int[] b){int n=0;for(int x:c)if(contains(a,x)&&contains(b,x))n++;return n;}
    private static boolean contains(int[] a,int x){for(int v:a)if(v==x)return true;return false;}
    private static int sortedL1(int[] a,int[] b){int s=0;for(int i=0;i<5;i++)s+=Math.abs(a[i]-b[i]);return s;}
    private static int maxEqualShift(int[] a,int[] b){Map<Integer,Integer> m=new HashMap<>();int best=0;for(int i=0;i<5;i++){int x=b[i]-a[i];best=Math.max(best,m.merge(x,1,Integer::sum));}return best;}
    private static int gapL1(int[] a,int[] b){int s=0;for(int i=0;i<4;i++)s+=Math.abs((a[i+1]-a[i])-(b[i+1]-b[i]));return s;}
    private static int gapRange(int[] a){int min=99,max=0;for(int i=0;i<4;i++){int g=a[i+1]-a[i];min=Math.min(min,g);max=Math.max(max,g);}return max-min;}
    private static int maxRun(int[] a){int best=1,run=1;for(int i=1;i<5;i++){if(a[i]==a[i-1]+1)run++;else run=1;best=Math.max(best,run);}return best;}
    private static int mirrorL1(int[] a,int[] b){int s=0;for(int i=0;i<5;i++)s+=Math.abs((51-a[4-i])-b[i]);return s;}
    private static int maxDecade(int[] a){int[] c=new int[6];int best=0;for(int x:a){int d=(x-1)/10;best=Math.max(best,++c[d]);}return best;}
    private static int sum(int[] a){int s=0;for(int x:a)s+=x;return s;}
    private static int span(int[] a){return a[4]-a[0];}
    private static int oddCount(int[] a){int n=0;for(int x:a)if((x&1)==1)n++;return n;}
    private static int maxGap(int[] a){int m=0;for(int i=0;i<4;i++)m=Math.max(m,a[i+1]-a[i]);return m;}
    private static boolean allSameHalf(int[] a){return a[4]<=25||a[0]>25;}
}
