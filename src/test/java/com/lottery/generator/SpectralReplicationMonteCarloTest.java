package com.lottery.generator;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Family-wise Monte-Carlo check for spectral peaks that replicate from train into holdout. */
class SpectralReplicationMonteCarloTest {
    private static final int MIN_PERIOD=4, MAX_PERIOD=100, RUNS=120;
    private static final int DRAWS=987, SPLIT=690;
    private static final long SEED=20260908L;

    @Test void replicatedSpectralPeaksAgainstRandomFiveOfFifty() throws Exception {
        List<List<Integer>> real=load(); assertEquals(DRAWS,real.size());
        Basis trainBasis=new Basis(SPLIT), holdBasis=new Basis(DRAWS-SPLIT);
        Replication observed=replication(real,trainBasis,holdBasis);
        Random rnd=new Random(SEED); int maxExceed=0,countExceed=0;
        double[] mcMax=new double[RUNS]; int[] mcCount=new int[RUNS];
        for(int r=0;r<RUNS;r++){
            Replication x=replication(randomHistory(rnd),trainBasis,holdBasis);
            mcMax[r]=x.maxHoldPower;mcCount[r]=x.rank95Count;
            if(x.maxHoldPower>=observed.maxHoldPower)maxExceed++;
            if(x.rank95Count>=observed.rank95Count)countExceed++;
        }
        Arrays.sort(mcMax);Arrays.sort(mcCount);
        System.out.printf(Locale.ROOT,"SCI_REPLICATION|observedMaxHoldPower=%.6f|observedRank95Count=%d|bestNumber=%d|bestPeriod=%d|bestHoldRankPct=%.3f%n",
                observed.maxHoldPower,observed.rank95Count,observed.bestNumber,observed.bestPeriod,observed.bestHoldRank);
        System.out.printf(Locale.ROOT,"SCI_REPLICATION_MC|maxPowerMcMedian=%.6f|maxPowerMc95=%.6f|maxPowerP=%.6f|rank95CountMcMedian=%d|rank95CountMc95=%d|rank95CountP=%.6f|runs=%d%n",
                q(mcMax,.5),q(mcMax,.95),(maxExceed+1.0)/(RUNS+1.0),qi(mcCount,.5),qi(mcCount,.95),(countExceed+1.0)/(RUNS+1.0),RUNS);
    }

    private static Replication replication(List<List<Integer>> h,Basis tb,Basis hb){
        double max=-1,bestRank=0;int bestN=-1,bestP=-1,rank95=0;
        for(int n=1;n<=50;n++){
            double[] all=binary(h,n),tr=Arrays.copyOfRange(all,0,SPLIT),ho=Arrays.copyOfRange(all,SPLIT,DRAWS);
            int p=tb.bestPeriod(tr);double hp=hb.power(ho,p),rank=hb.rank(ho,p);
            if(rank>=95)rank95++;
            if(hp>max){max=hp;bestRank=rank;bestN=n;bestP=p;}
        }
        return new Replication(max,rank95,bestN,bestP,bestRank);
    }

    static final class Basis{
        final int len;final double[][]co=new double[MAX_PERIOD-MIN_PERIOD+1][],si=new double[MAX_PERIOD-MIN_PERIOD+1][];
        Basis(int len){this.len=len;for(int p=MIN_PERIOD;p<=MAX_PERIOD;p++){int k=p-MIN_PERIOD;co[k]=new double[len];si[k]=new double[len];for(int t=0;t<len;t++){double a=2*Math.PI*t/p;co[k][t]=Math.cos(a);si[k][t]=Math.sin(a);}}}
        int bestPeriod(double[]x){int bp=MIN_PERIOD;double b=-1;for(int p=MIN_PERIOD;p<=MAX_PERIOD;p++){double z=power(x,p);if(z>b){b=z;bp=p;}}return bp;}
        double power(double[]x,int p){double m=Arrays.stream(x).average().orElse(0),v=0,re=0,im=0;for(double z:x)v+=(z-m)*(z-m);v/=x.length;if(v==0)return 0;int k=p-MIN_PERIOD;for(int t=0;t<x.length;t++){double z=x[t]-m;re+=z*co[k][t];im+=z*si[k][t];}return(re*re+im*im)/(x.length*v);}
        double rank(double[]x,int p){double z=power(x,p);int le=0,total=0;for(int q=MIN_PERIOD;q<=MAX_PERIOD;q++){if(power(x,q)<=z)le++;total++;}return 100.0*le/total;}
    }
    private record Replication(double maxHoldPower,int rank95Count,int bestNumber,int bestPeriod,double bestHoldRank){}
    private static double[]binary(List<List<Integer>>h,int n){double[]x=new double[h.size()];for(int i=0;i<h.size();i++)x[i]=h.get(i).contains(n)?1:0;return x;}
    private static List<List<Integer>>randomHistory(Random r){List<List<Integer>>h=new ArrayList<>(DRAWS);for(int i=0;i<DRAWS;i++){int[]a=new int[50];for(int j=0;j<50;j++)a[j]=j+1;for(int j=0;j<5;j++){int k=j+r.nextInt(50-j),t=a[j];a[j]=a[k];a[k]=t;}int[]b=Arrays.copyOf(a,5);Arrays.sort(b);h.add(Arrays.stream(b).boxed().toList());}return h;}
    private static double q(double[]x,double p){return x[Math.max(0,Math.min(x.length-1,(int)Math.ceil(p*x.length)-1))];}
    private static int qi(int[]x,double p){return x[Math.max(0,Math.min(x.length-1,(int)Math.ceil(p*x.length)-1))];}
    private static List<List<Integer>>load()throws Exception{InputStream s=SpectralReplicationMonteCarloTest.class.getResourceAsStream("/eurojackpot_archiv.csv");if(s==null)throw new IllegalStateException();List<List<Integer>>x=new ArrayList<>();try(BufferedReader r=new BufferedReader(new InputStreamReader(s,StandardCharsets.UTF_8))){String l;while((l=r.readLine())!=null){if(l.isBlank())continue;String[]p=l.split(",");x.add(Arrays.stream(p[1].split("-")).map(Integer::valueOf).sorted().toList());}}Collections.reverse(x);return x;}
}
