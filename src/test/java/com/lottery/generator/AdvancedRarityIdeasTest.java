package com.lottery.generator;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.ToDoubleFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Experimental comparison of additional rarity ideas.  The first 70% of draws (chronological)
 * are used to fit empirical thresholds; the last 30% are held out.  Candidate-space impact is
 * then measured against all C(50,5) candidates using the full archive/current state where needed.
 */
class AdvancedRarityIdeasTest {
    private static final int TOTAL = 2_118_760;
    private static final double TRAIN_FRACTION = 0.70;

    @Test
    void evaluateAdvancedIdeas() throws Exception {
        List<List<Integer>> all = loadChronological();
        int split = (int) Math.floor(all.size() * TRAIN_FRACTION);
        List<List<Integer>> train = all.subList(0, split);
        List<List<Integer>> holdout = all.subList(split, all.size());
        System.out.printf("ADV_SETUP|draws=%d|train=%d|holdout=%d%n", all.size(), train.size(), holdout.size());

        RobustModel positional = RobustModel.forPositions(train);
        double positionalCut = upperQuantile(train.stream().mapToDouble(positional::scorePositions).toArray(), 0.90);
        reportStatic("POSITIONAL_ROBUST", train, holdout, positional::scorePositions, positionalCut, true,
                c -> positional.scorePositions(c) >= positionalCut);

        GapModel gap = GapModel.from(train, 0.90);
        reportPredicate("GAP_PERCENTILE_2_OUT", holdout, c -> gap.outside(c) >= 2,
                c -> gap.outside(c) >= 2);

        Map<String,Integer> decadeCounts = counts(train, AdvancedRarityIdeasTest::decadeSignature);
        int decadeCut = lowFrequencyCutoff(train, decadeCounts, AdvancedRarityIdeasTest::decadeSignature, 0.10);
        reportPredicate("DECADE_SIGNATURE_RARE", holdout,
                c -> decadeCounts.getOrDefault(decadeSignature(c), 0) <= decadeCut,
                c -> decadeCounts.getOrDefault(decadeSignature(c), 0) <= decadeCut);

        double[] entTrain = train.stream().mapToDouble(AdvancedRarityIdeasTest::gapEntropy).toArray();
        double entLow = lowerQuantile(entTrain, 0.05), entHigh = upperQuantile(entTrain, 0.95);
        reportPredicate("GAP_ENTROPY_TAILS", holdout,
                c -> gapEntropy(c) <= entLow || gapEntropy(c) >= entHigh,
                c -> gapEntropy(c) <= entLow || gapEntropy(c) >= entHigh);

        double mirrorCut = lowerQuantile(train.stream().mapToDouble(AdvancedRarityIdeasTest::mirrorScore).toArray(), 0.10);
        reportStatic("MIRROR_SYMMETRY", train, holdout, AdvancedRarityIdeasTest::mirrorScore, mirrorCut, false,
                c -> mirrorScore(c) <= mirrorCut);

        double lastDigitCut = upperQuantile(train.stream().mapToDouble(AdvancedRarityIdeasTest::sameLastDigitPairs).toArray(), 0.90);
        reportStatic("LAST_DIGIT_CONCENTRATION", train, holdout, AdvancedRarityIdeasTest::sameLastDigitPairs, lastDigitCut, true,
                c -> sameLastDigitPairs(c) >= lastDigitCut);

        double centerDeltaCut = transitionUpperCut(train, AdvancedRarityIdeasTest::centerDelta, 0.90);
        List<Integer> latest = all.get(all.size()-1);
        double latestCenter = center(latest);
        reportTransitionPredicate("CENTER_DELTA", holdout, centerDeltaCut,
                (prev,cur) -> centerDelta(prev,cur) >= centerDeltaCut,
                c -> Math.abs(center(c)-latestCenter) >= centerDeltaCut);

        PairModel pairTrain = PairModel.from(train);
        double pairCut = lowerQuantile(train.stream().mapToDouble(pairTrain::score).toArray(), 0.10);
        PairModel pairFull = PairModel.from(all);
        double pairFullCut = lowerQuantile(all.stream().mapToDouble(pairFull::score).toArray(), 0.10);
        reportPredicate("PAIR_COMPATIBILITY_LOW", holdout,
                c -> pairTrain.score(c) <= pairCut,
                c -> pairFull.score(c) <= pairFullCut);

        NumberRegularity regularity = NumberRegularity.from(train);
        double regularityCut = lowerQuantile(train.stream().mapToDouble(regularity::score).toArray(), 0.10);
        reportPredicate("INTERARRIVAL_REGULARITY", holdout,
                c -> regularity.score(c) <= regularityCut,
                c -> regularity.score(c) <= regularityCut);

        TransitionModel state = TransitionModel.from(train, AdvancedRarityIdeasTest::structuralState);
        reportTransitionModel("STRUCTURAL_TRANSITION", all, split, state, AdvancedRarityIdeasTest::structuralState);

        TransitionModel sumBuckets = TransitionModel.from(train, AdvancedRarityIdeasTest::sumBucket);
        reportTransitionModel("SUM_BUCKET_TRANSITION", all, split, sumBuckets, AdvancedRarityIdeasTest::sumBucket);

        TransitionModel highLow = TransitionModel.from(train, AdvancedRarityIdeasTest::highLowState);
        reportTransitionModel("HIGH_LOW_TRANSITION", all, split, highLow, AdvancedRarityIdeasTest::highLowState);

        TransitionModel divisibility = TransitionModel.from(train, AdvancedRarityIdeasTest::divisibilityState);
        reportTransitionModel("DIVISIBILITY_TRANSITION", all, split, divisibility, AdvancedRarityIdeasTest::divisibilityState);

        AgeModel age = AgeModel.fromHistory(all);
        double[] historicalAgeScores = age.historicalScores();
        int ageSplit = Math.max(1, (int)(historicalAgeScores.length * TRAIN_FRACTION));
        double ageLow = lowerQuantile(Arrays.copyOf(historicalAgeScores, ageSplit), 0.05);
        double ageHigh = upperQuantile(Arrays.copyOf(historicalAgeScores, ageSplit), 0.95);
        long ageHoldReject = 0;
        for (int i=ageSplit;i<historicalAgeScores.length;i++) if (historicalAgeScores[i] <= ageLow || historicalAgeScores[i] >= ageHigh) ageHoldReject++;
        long ageSpace = enumerate(c -> { double s=age.currentScore(c); return s <= ageLow || s >= ageHigh; });
        System.out.printf("ADV|AGE_VECTOR|holdout=%d|holdoutRejected=%d|holdoutKeepPct=%.6f|spaceRejected=%d|spaceRejectedPct=%.6f|cutLow=%.4f|cutHigh=%.4f%n",
                historicalAgeScores.length-ageSplit, ageHoldReject, pct(historicalAgeScores.length-ageSplit-ageHoldReject,historicalAgeScores.length-ageSplit),
                ageSpace,pct(ageSpace,TOTAL),ageLow,ageHigh);

        HotColdModel hotCold = HotColdModel.from(all, 20);
        double hcLow = lowerQuantile(hotCold.historicalScores(), 0.05), hcHigh = upperQuantile(hotCold.historicalScores(), 0.95);
        long hcSpace = enumerate(c -> {double s=hotCold.currentScore(c); return s<=hcLow || s>=hcHigh;});
        double[] hcHist=hotCold.historicalScores(); int hcS=(int)(hcHist.length*TRAIN_FRACTION); long hcRej=0;
        for(int i=hcS;i<hcHist.length;i++) if(hcHist[i]<=hcLow||hcHist[i]>=hcHigh) hcRej++;
        System.out.printf("ADV|HOT_COLD_OVERDUE|holdout=%d|holdoutRejected=%d|holdoutKeepPct=%.6f|spaceRejected=%d|spaceRejectedPct=%.6f%n",
                hcHist.length-hcS,hcRej,pct(hcHist.length-hcS-hcRej,hcHist.length-hcS),hcSpace,pct(hcSpace,TOTAL));

        // Nearest historical sorted-L1 is expensive for all 2.1M candidates. Evaluate holdout exactly
        // and a deterministic 100k candidate sample for search-space impact.
        int nearestCut = (int) lowerQuantile(train.stream().mapToDouble(d -> nearestL1ExcludingSelf(d, train)).filter(x -> x < 999).toArray(), 0.10);
        long nearestHold=0;
        for(List<Integer> d:holdout) if(nearestL1(d,train)<=nearestCut) nearestHold++;
        int sample=100_000, sampleReject=0;
        Random rnd=new Random(20260907L);
        for(int i=0;i<sample;i++){List<Integer> c=randomCombination(rnd); if(nearestL1(c,all)<=nearestCut) sampleReject++;}
        System.out.printf("ADV|NEAREST_HISTORY_L1|holdout=%d|holdoutRejected=%d|holdoutKeepPct=%.6f|sample=%d|sampleRejected=%d|sampleRejectedPct=%.6f|cut=%d|spaceMode=DETERMINISTIC_SAMPLE%n",
                holdout.size(),nearestHold,pct(holdout.size()-nearestHold,holdout.size()),sample,sampleReject,pct(sampleReject,sample),nearestCut);

        assertEquals(987, all.size());
    }

    private interface Pred { boolean test(List<Integer> c); }
    private interface TransitionPred { boolean test(List<Integer> prev,List<Integer> cur); }

    private static void reportStatic(String name,List<List<Integer>> train,List<List<Integer>> holdout,ToDoubleFunction<List<Integer>> score,double cut,boolean upper,Pred spacePred){
        long r=holdout.stream().filter(c -> upper?score.applyAsDouble(c)>=cut:score.applyAsDouble(c)<=cut).count();
        long space=enumerate(spacePred);
        System.out.printf("ADV|%s|holdout=%d|holdoutRejected=%d|holdoutKeepPct=%.6f|spaceRejected=%d|spaceRejectedPct=%.6f|cut=%.6f%n",name,holdout.size(),r,pct(holdout.size()-r,holdout.size()),space,pct(space,TOTAL),cut);
    }
    private static void reportPredicate(String name,List<List<Integer>> holdout,Pred historical,Pred currentSpace){
        long r=holdout.stream().filter(historical::test).count(); long space=enumerate(currentSpace);
        System.out.printf("ADV|%s|holdout=%d|holdoutRejected=%d|holdoutKeepPct=%.6f|spaceRejected=%d|spaceRejectedPct=%.6f%n",name,holdout.size(),r,pct(holdout.size()-r,holdout.size()),space,pct(space,TOTAL));
    }
    private static void reportTransitionPredicate(String name,List<List<Integer>> holdout,double cut,TransitionPred hist,Pred current){
        long r=0; for(int i=1;i<holdout.size();i++) if(hist.test(holdout.get(i-1),holdout.get(i))) r++;
        long space=enumerate(current); int n=holdout.size()-1;
        System.out.printf("ADV|%s|holdout=%d|holdoutRejected=%d|holdoutKeepPct=%.6f|spaceRejected=%d|spaceRejectedPct=%.6f|cut=%.6f%n",name,n,r,pct(n-r,n),space,pct(space,TOTAL),cut);
    }
    private static void reportTransitionModel(String name,List<List<Integer>> all,int split,TransitionModel model,java.util.function.Function<List<Integer>,String> key){
        int cutoff=model.rareCutoffForTrainingKeep(0.90); long rej=0; int n=0;
        for(int i=split;i<all.size();i++){if(i==0)continue;n++;if(model.count(key.apply(all.get(i-1)),key.apply(all.get(i)))<=cutoff)rej++;}
        String latest=key.apply(all.get(all.size()-1)); long space=enumerate(c -> model.count(latest,key.apply(c))<=cutoff);
        System.out.printf("ADV|%s|holdout=%d|holdoutRejected=%d|holdoutKeepPct=%.6f|spaceRejected=%d|spaceRejectedPct=%.6f|rareCountCut=%d%n",name,n,rej,pct(n-rej,n),space,pct(space,TOTAL),cutoff);
    }

    private static long enumerate(Pred p){long count=0;for(int a=1;a<=46;a++)for(int b=a+1;b<=47;b++)for(int c=b+1;c<=48;c++)for(int d=c+1;d<=49;d++)for(int e=d+1;e<=50;e++)if(p.test(List.of(a,b,c,d,e)))count++;return count;}
    private static double pct(long c,long t){return t==0?0:100.0*c/t;}
    private static double upperQuantile(double[] a,double q){return quantile(a,q);}
    private static double lowerQuantile(double[] a,double q){return quantile(a,q);}
    private static double quantile(double[] a,double q){double[] b=a.clone();Arrays.sort(b);int idx=(int)Math.ceil(q*b.length)-1;idx=Math.max(0,Math.min(b.length-1,idx));return b[idx];}
    private static double transitionUpperCut(List<List<Integer>> h,TransitionScore f,double q){double[] x=new double[h.size()-1];for(int i=1;i<h.size();i++)x[i-1]=f.score(h.get(i-1),h.get(i));return quantile(x,q);}
    private interface TransitionScore{double score(List<Integer>a,List<Integer>b);}

    private static List<List<Integer>> loadChronological() throws Exception {InputStream s=AdvancedRarityIdeasTest.class.getResourceAsStream("/eurojackpot_archiv.csv");if(s==null)throw new IllegalStateException();List<List<Integer>> x=new ArrayList<>();try(BufferedReader r=new BufferedReader(new InputStreamReader(s,StandardCharsets.UTF_8))){String line;while((line=r.readLine())!=null){if(line.isBlank())continue;String[] p=line.split(",");x.add(Arrays.stream(p[1].split("-")).map(Integer::valueOf).sorted().toList());}}Collections.reverse(x);return x;}
    private static List<Integer> sorted(List<Integer>x){return x.stream().sorted().toList();}
    private static int sum(List<Integer>x){return x.stream().mapToInt(Integer::intValue).sum();}
    private static double center(List<Integer>x){return sum(x)/5.0;}
    private static double centerDelta(List<Integer>a,List<Integer>b){return Math.abs(center(a)-center(b));}
    private static int[] gaps(List<Integer>x){List<Integer>s=sorted(x);return new int[]{s.get(1)-s.get(0),s.get(2)-s.get(1),s.get(3)-s.get(2),s.get(4)-s.get(3)};}
    private static double gapEntropy(List<Integer>x){int[] g=gaps(x);double total=Arrays.stream(g).sum(),h=0;for(int v:g){double p=v/total;h-=p*Math.log(p);}return h;}
    private static double mirrorScore(List<Integer>x){List<Integer>s=sorted(x);return Math.abs(s.get(0)+s.get(4)-51)+Math.abs(s.get(1)+s.get(3)-51)+Math.abs(2*s.get(2)-51);}
    private static int sameLastDigitPairs(List<Integer>x){int[] c=new int[10];for(int n:x)c[n%10]++;int p=0;for(int n:c)p+=n*(n-1)/2;return p;}
    private static String decadeSignature(List<Integer>x){int[] c=new int[5];for(int n:x)c[(n-1)/10]++;return Arrays.toString(c);}
    private static String sumBucket(List<Integer>x){return Integer.toString(sum(x)/20);}
    private static String highLowState(List<Integer>x){int h=0;for(int n:x)if(n>25)h++;return Integer.toString(h);}
    private static String divisibilityState(List<Integer>x){int d2=0,d3=0,d5=0;for(int n:x){if(n%2==0)d2++;if(n%3==0)d3++;if(n%5==0)d5++;}return d2+"-"+d3+"-"+d5;}
    private static String structuralState(List<Integer>x){List<Integer>s=sorted(x);return (sum(x)/25)+"|"+((s.get(4)-s.get(0))/5)+"|"+highLowState(x)+"|"+decadeSignature(x);}
    private static <T> Map<T,Integer> counts(List<List<Integer>> h,java.util.function.Function<List<Integer>,T> f){Map<T,Integer>m=new HashMap<>();for(List<Integer>d:h)m.merge(f.apply(d),1,Integer::sum);return m;}
    private static int lowFrequencyCutoff(List<List<Integer>> h,Map<String,Integer>m,java.util.function.Function<List<Integer>,String> f,double tail){double[] v=h.stream().mapToDouble(d->m.get(f.apply(d))).toArray();return (int)quantile(v,tail);}

    static final class RobustModel {final double[] med=new double[5],iqr=new double[5];static RobustModel forPositions(List<List<Integer>>h){RobustModel m=new RobustModel();for(int p=0;p<5;p++){final int pp=p;double[]v=h.stream().mapToDouble(d->sorted(d).get(pp)).toArray();m.med[p]=quantile(v,.5);m.iqr[p]=Math.max(1,quantile(v,.75)-quantile(v,.25));}return m;}double scorePositions(List<Integer>x){List<Integer>s=sorted(x);double z=0;for(int i=0;i<5;i++)z+=Math.abs(s.get(i)-med[i])/iqr[i];return z;}}
    static final class GapModel {final double[] lo=new double[4],hi=new double[4];static GapModel from(List<List<Integer>>h,double cov){GapModel m=new GapModel();double t=(1-cov)/2;for(int p=0;p<4;p++){final int pp=p;double[]v=h.stream().mapToDouble(d->gaps(d)[pp]).toArray();m.lo[p]=quantile(v,t);m.hi[p]=quantile(v,1-t);}return m;}int outside(List<Integer>x){int[]g=gaps(x),n=0;for(int i=0;i<4;i++)if(g[i]<lo[i]||g[i]>hi[i])n++;return n;}}
    static final class PairModel {final int[][]w=new int[51][51];static PairModel from(List<List<Integer>>h){PairModel m=new PairModel();for(List<Integer>d:h)for(int i=0;i<5;i++)for(int j=i+1;j<5;j++){int a=d.get(i),b=d.get(j);m.w[a][b]++;m.w[b][a]++;}return m;}double score(List<Integer>x){double s=0;for(int i=0;i<5;i++)for(int j=i+1;j<5;j++)s+=w[x.get(i)][x.get(j)];return s;}}
    static final class NumberRegularity {final double[]cv=new double[51];static NumberRegularity from(List<List<Integer>>h){NumberRegularity r=new NumberRegularity();for(int n=1;n<=50;n++){List<Integer>idx=new ArrayList<>();for(int i=0;i<h.size();i++)if(h.get(i).contains(n))idx.add(i);if(idx.size()<3){r.cv[n]=9;continue;}double[]g=new double[idx.size()-1];for(int i=1;i<idx.size();i++)g[i-1]=idx.get(i)-idx.get(i-1);double mean=Arrays.stream(g).average().orElse(1),var=0;for(double v:g)var+=(v-mean)*(v-mean);r.cv[n]=Math.sqrt(var/g.length)/mean;}return r;}double score(List<Integer>x){return x.stream().mapToDouble(n->cv[n]).average().orElse(9);}}
    static final class TransitionModel {final Map<String,Integer>counts=new HashMap<>();final List<Integer>observed=new ArrayList<>();static TransitionModel from(List<List<Integer>>h,java.util.function.Function<List<Integer>,String>f){TransitionModel m=new TransitionModel();for(int i=1;i<h.size();i++){String k=f.apply(h.get(i-1))+">"+f.apply(h.get(i));m.counts.merge(k,1,Integer::sum);}for(int i=1;i<h.size();i++)m.observed.add(m.count(f.apply(h.get(i-1)),f.apply(h.get(i))));return m;}int count(String a,String b){return counts.getOrDefault(a+">"+b,0);}int rareCutoffForTrainingKeep(double keep){int[]a=observed.stream().mapToInt(Integer::intValue).toArray();Arrays.sort(a);int idx=(int)Math.floor((1-keep)*a.length);return a[Math.max(0,Math.min(a.length-1,idx))]-1;}}
    static final class AgeModel {final List<List<Integer>>h;final int[]currentAge=new int[51];AgeModel(List<List<Integer>>h){this.h=h;Arrays.fill(currentAge,h.size()+1);for(int n=1;n<=50;n++){for(int i=h.size()-1;i>=0;i--)if(h.get(i).contains(n)){currentAge[n]=h.size()-1-i;break;}}}static AgeModel fromHistory(List<List<Integer>>h){return new AgeModel(h);}double ageScore(List<Integer>d,int[]age){double mean=d.stream().mapToInt(n->age[n]).average().orElse(0);int max=d.stream().mapToInt(n->age[n]).max().orElse(0);return mean+0.35*max;}double currentScore(List<Integer>d){return ageScore(d,currentAge);}double[] historicalScores(){int[]last=new int[51];Arrays.fill(last,-1);List<Double>s=new ArrayList<>();for(int i=0;i<h.size();i++){if(i>20){int[]a=new int[51];for(int n=1;n<=50;n++)a[n]=last[n]<0?i:i-last[n];s.add(ageScore(h.get(i),a));}for(int n:h.get(i))last[n]=i;}return s.stream().mapToDouble(Double::doubleValue).toArray();}}
    static final class HotColdModel {final List<List<Integer>>h;final int window;final int[]currentFreq=new int[51];HotColdModel(List<List<Integer>>h,int w){this.h=h;window=w;for(int i=Math.max(0,h.size()-w);i<h.size();i++)for(int n:h.get(i))currentFreq[n]++;}static HotColdModel from(List<List<Integer>>h,int w){return new HotColdModel(h,w);}double currentScore(List<Integer>d){return d.stream().mapToInt(n->currentFreq[n]).sum();}double[]historicalScores(){List<Double>s=new ArrayList<>();for(int i=window;i<h.size();i++){int[]f=new int[51];for(int j=i-window;j<i;j++)for(int n:h.get(j))f[n]++;double z=0;for(int n:h.get(i))z+=f[n];s.add(z);}return s.stream().mapToDouble(Double::doubleValue).toArray();}}
    private static int nearestL1(List<Integer>a,List<List<Integer>>h){int best=999;List<Integer>s=sorted(a);for(List<Integer>d:h){List<Integer>t=sorted(d);int z=0;for(int i=0;i<5;i++)z+=Math.abs(s.get(i)-t.get(i));best=Math.min(best,z);}return best;}
    private static int nearestL1ExcludingSelf(List<Integer>a,List<List<Integer>>h){int best=999;boolean skipped=false;for(List<Integer>d:h){if(!skipped&&d.equals(a)){skipped=true;continue;}best=Math.min(best,nearestL1(a,List.of(d)));}return best;}
    private static List<Integer> randomCombination(Random r){TreeSet<Integer>s=new TreeSet<>();while(s.size()<5)s.add(1+r.nextInt(50));return new ArrayList<>(s);}
}
