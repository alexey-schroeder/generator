package com.lottery.generator;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Scientific audit layer for the EuroJackpot main-number archive.
 *
 * This test deliberately separates discovery from validation:
 * - spectral periods are discovered on the first 70% of draws;
 * - the same periods are evaluated on the final 30% without refitting;
 * - Monte-Carlo random 5-of-50 histories provide a family-wise spectral baseline;
 * - structural rule frequencies are compared with exact uniform 5-of-50 probabilities.
 *
 * The output is descriptive evidence, not a claim of predictability.
 */
class ScientificRarityAuditTest {
    private static final int TOTAL_COMBINATIONS = 2_118_760;
    private static final double TRAIN_FRACTION = 0.70;
    private static final int MIN_PERIOD = 4;
    private static final int MAX_PERIOD = 100;
    private static final int MONTE_CARLO_RUNS = 80;
    private static final long SEED = 20260907L;

    @Test
    void runScientificAudit() throws Exception {
        List<List<Integer>> draws = loadChronological();
        assertEquals(987, draws.size());
        int split = (int) Math.floor(draws.size() * TRAIN_FRACTION);
        System.out.printf("SCI_SETUP|draws=%d|train=%d|holdout=%d|periods=%d..%d|mcRuns=%d%n",
                draws.size(), split, draws.size() - split, MIN_PERIOD, MAX_PERIOD, MONTE_CARLO_RUNS);

        SpectralBasis trainBasis = new SpectralBasis(split, MIN_PERIOD, MAX_PERIOD);
        SpectralBasis holdBasis = new SpectralBasis(draws.size() - split, MIN_PERIOD, MAX_PERIOD);
        SpectralBasis fullBasis = new SpectralBasis(draws.size(), MIN_PERIOD, MAX_PERIOD);

        // 50 binary number-occurrence series.
        List<SpectralFinding> numberFindings = new ArrayList<>();
        for (int n = 1; n <= 50; n++) {
            double[] series = binarySeries(draws, n);
            SpectralFinding f = validatePeak("NUMBER_" + n, series, split, trainBasis, holdBasis);
            numberFindings.add(f);
        }
        numberFindings.stream()
                .sorted(Comparator.comparingDouble(SpectralFinding::trainNormalizedPower).reversed())
                .limit(10)
                .forEach(ScientificRarityAuditTest::printSpectral);

        // Aggregate time series suggested by the exploratory work.
        Map<String, double[]> aggregates = new LinkedHashMap<>();
        aggregates.put("SUM", numericSeries(draws, d -> d.stream().mapToInt(Integer::intValue).sum()));
        aggregates.put("SPAN", numericSeries(draws, d -> Collections.max(d) - Collections.min(d)));
        aggregates.put("HIGH_COUNT", numericSeries(draws, d -> d.stream().filter(x -> x > 25).count()));
        for (int p = 0; p < 5; p++) {
            final int pos = p;
            aggregates.put("POSITION_" + (p + 1), numericSeries(draws, d -> d.stream().sorted().toList().get(pos)));
        }
        for (int p = 0; p < 4; p++) {
            final int gap = p;
            aggregates.put("GAP_" + (p + 1), numericSeries(draws, d -> gaps(d)[gap]));
        }
        for (Map.Entry<String,double[]> e : aggregates.entrySet()) {
            printSpectral(validatePeak(e.getKey(), e.getValue(), split, trainBasis, holdBasis));
        }

        // Family-wise Monte-Carlo baseline: strongest normalized spectral peak among all 50 number series.
        double observedFamilyMax = numberFindings.stream().mapToDouble(SpectralFinding::trainNormalizedPower).max().orElse(0);
        Random random = new Random(SEED);
        int exceed = 0;
        double[] mcMax = new double[MONTE_CARLO_RUNS];
        for (int r = 0; r < MONTE_CARLO_RUNS; r++) {
            List<List<Integer>> synthetic = randomHistory(split, random);
            double familyMax = 0;
            for (int n = 1; n <= 50; n++) {
                familyMax = Math.max(familyMax, trainBasis.strongest(binarySeries(synthetic, n)).normalizedPower());
            }
            mcMax[r] = familyMax;
            if (familyMax >= observedFamilyMax) exceed++;
        }
        Arrays.sort(mcMax);
        double pFamily = (exceed + 1.0) / (MONTE_CARLO_RUNS + 1.0);
        System.out.printf(Locale.ROOT,
                "SCI_MC_SPECTRAL|observedFamilyMax=%.6f|mcMedian=%.6f|mc95=%.6f|mc99=%.6f|exceed=%d|runs=%d|empiricalP=%.6f%n",
                observedFamilyMax, quantileSorted(mcMax,.50), quantileSorted(mcMax,.95), quantileSorted(mcMax,.99), exceed, MONTE_CARLO_RUNS, pFamily);

        // Full-history lag autocorrelation audit for occurrence series (lags 1..30), with max absolute correlation.
        double maxAbsAc = 0; int maxAcNumber = -1, maxAcLag = -1; double signed = 0;
        for (int n = 1; n <= 50; n++) {
            double[] s = binarySeries(draws, n);
            for (int lag = 1; lag <= 30; lag++) {
                double ac = autocorrelation(s, lag);
                if (Math.abs(ac) > maxAbsAc) { maxAbsAc = Math.abs(ac); signed = ac; maxAcNumber = n; maxAcLag = lag; }
            }
        }
        System.out.printf(Locale.ROOT,"SCI_AUTOCORR|maxNumber=%d|maxLag=%d|corr=%.6f|absCorr=%.6f%n",
                maxAcNumber,maxAcLag,signed,maxAbsAc);

        exactUniformStructuralAudit(draws);
    }

    private static SpectralFinding validatePeak(String name, double[] all, int split,
                                                 SpectralBasis trainBasis, SpectralBasis holdBasis) {
        double[] train = Arrays.copyOfRange(all, 0, split);
        double[] hold = Arrays.copyOfRange(all, split, all.length);
        Peak peak = trainBasis.strongest(train);
        double holdPower = holdBasis.normalizedPowerAt(hold, peak.period());
        double holdRank = holdBasis.rankPercentile(hold, peak.period());
        return new SpectralFinding(name, peak.period(), peak.normalizedPower(), holdPower, holdRank);
    }

    private static void printSpectral(SpectralFinding f) {
        System.out.printf(Locale.ROOT,
                "SCI_SPECTRAL|series=%s|trainBestPeriod=%d|trainNormPower=%.6f|holdoutNormPowerSamePeriod=%.6f|holdoutPeriodRankPct=%.3f%n",
                f.name(), f.period(), f.trainNormalizedPower(), f.holdoutNormalizedPower(), f.holdoutRankPercentile());
    }

    private static void exactUniformStructuralAudit(List<List<Integer>> draws) {
        List<Rule> rules = List.of(
                new Rule("SUM_GE_175", d -> sum(d) >= 175),
                new Rule("SUM_LE_80", d -> sum(d) <= 80),
                new Rule("SPAN_LE_20", d -> span(d) <= 20),
                new Rule("SAME_HALF_ALL_5", d -> d.stream().allMatch(x -> x <= 25) || d.stream().allMatch(x -> x > 25)),
                new Rule("MAX_GAP_GE_30", d -> Arrays.stream(gaps(d)).max().orElse(0) >= 30),
                new Rule("RUN_GE_3", d -> maxRun(d) >= 3),
                new Rule("EXTREME_PARITY", d -> { long o=d.stream().filter(x->x%2!=0).count(); return o<=1||o>=4; }),
                new Rule("ALL_SAME_MOD3", d -> { int m=d.get(0)%3; return d.stream().allMatch(x->x%3==m); })
        );
        long[] space = new long[rules.size()];
        forEachCombination(c -> {
            for (int i=0;i<rules.size();i++) if (rules.get(i).predicate().test(c)) space[i]++;
        });
        for (int i=0;i<rules.size();i++) {
            Rule rule=rules.get(i);
            long observed=draws.stream().filter(rule.predicate()).count();
            double p=space[i]/(double)TOTAL_COMBINATIONS;
            double expected=draws.size()*p;
            double sd=Math.sqrt(draws.size()*p*(1-p));
            double z=sd==0?0:(observed-expected)/sd;
            double twoSided=2.0*(1.0-normalCdf(Math.abs(z)));
            System.out.printf(Locale.ROOT,
                    "SCI_UNIFORM|rule=%s|space=%d|theoryPct=%.6f|observed=%d|observedPct=%.6f|expected=%.3f|z=%.4f|normalApproxP=%.6f%n",
                    rule.name(),space[i],100*p,observed,100.0*observed/draws.size(),expected,z,twoSided);
        }
    }

    static final class SpectralBasis {
        private final int length;
        private final int minPeriod;
        private final int maxPeriod;
        private final double[][] cos;
        private final double[][] sin;
        SpectralBasis(int length,int minPeriod,int maxPeriod) {
            this.length=length;this.minPeriod=minPeriod;this.maxPeriod=maxPeriod;
            int count=maxPeriod-minPeriod+1;
            cos=new double[count][length]; sin=new double[count][length];
            for(int p=minPeriod;p<=maxPeriod;p++) {
                int pi=p-minPeriod;
                for(int t=0;t<length;t++) {
                    double a=2.0*Math.PI*t/p;
                    cos[pi][t]=Math.cos(a); sin[pi][t]=Math.sin(a);
                }
            }
        }
        Peak strongest(double[] series) {
            double variance=variance(series);
            int bestPeriod=minPeriod; double best=-1;
            for(int p=minPeriod;p<=maxPeriod;p++) {
                double z=normalizedPowerAt(series,p,variance);
                if(z>best){best=z;bestPeriod=p;}
            }
            return new Peak(bestPeriod,best);
        }
        double normalizedPowerAt(double[] series,int period) { return normalizedPowerAt(series,period,variance(series)); }
        private double normalizedPowerAt(double[] series,int period,double variance) {
            if(series.length!=length) throw new IllegalArgumentException("length mismatch");
            double mean=Arrays.stream(series).average().orElse(0); int pi=period-minPeriod;
            double re=0,im=0;
            for(int t=0;t<length;t++){double v=series[t]-mean;re+=v*cos[pi][t];im+=v*sin[pi][t];}
            return variance<=1e-12?0:(re*re+im*im)/(length*variance);
        }
        double rankPercentile(double[] series,int targetPeriod) {
            double target=normalizedPowerAt(series,targetPeriod); int le=0,total=0;
            for(int p=minPeriod;p<=maxPeriod;p++){if(normalizedPowerAt(series,p)<=target)le++;total++;}
            return 100.0*le/total;
        }
    }

    private record Peak(int period,double normalizedPower) {}
    private record SpectralFinding(String name,int period,double trainNormalizedPower,double holdoutNormalizedPower,double holdoutRankPercentile) {}
    private record Rule(String name, Predicate<List<Integer>> predicate) {}

    private static double variance(double[] x){double m=Arrays.stream(x).average().orElse(0),v=0;for(double a:x)v+=(a-m)*(a-m);return v/x.length;}
    private static double autocorrelation(double[] x,int lag){double m=Arrays.stream(x).average().orElse(0),num=0,den=0;for(double v:x)den+=(v-m)*(v-m);for(int i=lag;i<x.length;i++)num+=(x[i]-m)*(x[i-lag]-m);return den==0?0:num/den;}
    private static double[] binarySeries(List<List<Integer>> draws,int number){double[]x=new double[draws.size()];for(int i=0;i<draws.size();i++)x[i]=draws.get(i).contains(number)?1:0;return x;}
    private static double[] numericSeries(List<List<Integer>> draws,ToDoubleFunction<List<Integer>> f){double[]x=new double[draws.size()];for(int i=0;i<draws.size();i++)x[i]=f.applyAsDouble(draws.get(i));return x;}
    private static List<List<Integer>> randomHistory(int n,Random r){List<List<Integer>>h=new ArrayList<>(n);for(int i=0;i<n;i++)h.add(randomCombination(r));return h;}
    private static List<Integer> randomCombination(Random r){int[]a=new int[50];for(int i=0;i<50;i++)a[i]=i+1;for(int i=0;i<5;i++){int j=i+r.nextInt(50-i);int t=a[i];a[i]=a[j];a[j]=t;}int[]b=Arrays.copyOf(a,5);Arrays.sort(b);return Arrays.stream(b).boxed().toList();}
    private interface CombinationConsumer{void accept(List<Integer> c);}
    private static void forEachCombination(CombinationConsumer f){for(int a=1;a<=46;a++)for(int b=a+1;b<=47;b++)for(int c=b+1;c<=48;c++)for(int d=c+1;d<=49;d++)for(int e=d+1;e<=50;e++)f.accept(List.of(a,b,c,d,e));}
    private static int sum(List<Integer>d){return d.stream().mapToInt(Integer::intValue).sum();}
    private static int span(List<Integer>d){return Collections.max(d)-Collections.min(d);}
    private static int[] gaps(List<Integer>d){List<Integer>s=d.stream().sorted().toList();return new int[]{s.get(1)-s.get(0),s.get(2)-s.get(1),s.get(3)-s.get(2),s.get(4)-s.get(3)};}
    private static int maxRun(List<Integer>d){List<Integer>s=d.stream().sorted().toList();int best=1,cur=1;for(int i=1;i<s.size();i++){if(s.get(i)==s.get(i-1)+1){cur++;best=Math.max(best,cur);}else cur=1;}return best;}
    private static double quantileSorted(double[] sorted,double q){int i=(int)Math.ceil(q*sorted.length)-1;return sorted[Math.max(0,Math.min(sorted.length-1,i))];}
    private static double normalCdf(double x){return 0.5*(1.0+erf(x/Math.sqrt(2.0)));}
    private static double erf(double x){double sign=x<0?-1:1;x=Math.abs(x);double a1=.254829592,a2=-.284496736,a3=1.421413741,a4=-1.453152027,a5=1.061405429,p=.3275911;double t=1.0/(1.0+p*x);double y=1.0-(((((a5*t+a4)*t)+a3)*t+a2)*t+a1)*t*Math.exp(-x*x);return sign*y;}

    private static List<List<Integer>> loadChronological() throws Exception {
        InputStream s=ScientificRarityAuditTest.class.getResourceAsStream("/eurojackpot_archiv.csv");
        if(s==null)throw new IllegalStateException("Missing archive");
        List<List<Integer>> x=new ArrayList<>();
        try(BufferedReader r=new BufferedReader(new InputStreamReader(s,StandardCharsets.UTF_8))){String line;while((line=r.readLine())!=null){if(line.isBlank())continue;String[]p=line.split(",");x.add(Arrays.stream(p[1].split("-")).map(Integer::valueOf).sorted().toList());}}
        Collections.reverse(x);return x;
    }
}
