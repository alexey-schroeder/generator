package com.lottery.generator;

import com.lottery.generator.rarity.PositionalRangeRule;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PositionalRangeRuleTest {
    private static final int TOTAL_COMBINATIONS = 2_118_760;

    @Test void positionalRangesAndExactSearchSpaceImpact() throws Exception {
        measureSpace("100", PositionalRangeRule.fromHistory(loadDrawsNewestFirst(), 2));
    }

    @Test void central95PercentRangesAndExactSearchSpaceImpact() throws Exception {
        measureSpace("95", PositionalRangeRule.fromCentralCoverage(loadDrawsNewestFirst(), 0.95, 2));
    }

    private static void measureSpace(String label, PositionalRangeRule rule) {
        long[] dist = new long[6]; long rejected = 0;
        for (int a=1;a<=46;a++) for(int b=a+1;b<=47;b++) for(int c=b+1;c<=48;c++)
            for(int d=c+1;d<=49;d++) for(int e=d+1;e<=50;e++) {
                int outside=rule.positionsOutsideRange(List.of(a,b,c,d,e)); dist[outside]++;
                if(outside>=2) rejected++;
            }
        assertEquals(TOTAL_COMBINATIONS, Arrays.stream(dist).sum()); assertTrue(rejected>0);
        System.out.printf("POSITION_%s_RANGES|min=%s|max=%s%n",label,rule.minimums(),rule.maximums());
        for(int i=0;i<dist.length;i++) System.out.printf("POSITION_%s_SPACE|outside=%d|count=%d|pct=%.6f%n",label,i,dist[i],pct(dist[i],TOTAL_COMBINATIONS));
        System.out.printf("POSITION_%s_REJECT|threshold=2|rejected=%d|rejectedPct=%.6f|remaining=%d|remainingPct=%.6f%n",label,rejected,pct(rejected,TOTAL_COMBINATIONS),TOTAL_COMBINATIONS-rejected,pct(TOTAL_COMBINATIONS-rejected,TOTAL_COMBINATIONS));
    }

    @Test void rollingHistoricalBacktestUsesOnlyPriorDraws() throws Exception { rolling("100",1.0); }
    @Test void rolling95PercentBacktestUsesOnlyPriorDraws() throws Exception { rolling("95",0.95); }

    private static void rolling(String label,double coverage) throws Exception {
        List<List<Integer>> chronological=loadDrawsNewestFirst(); Collections.reverse(chronological);
        int minTraining=50,checked=0,rejected=0; int[] dist=new int[6];
        for(int i=minTraining;i<chronological.size();i++) {
            List<List<Integer>> past=chronological.subList(0,i);
            PositionalRangeRule rule=coverage==1.0?PositionalRangeRule.fromHistory(past,2):PositionalRangeRule.fromCentralCoverage(past,coverage,2);
            int outside=rule.positionsOutsideRange(chronological.get(i)); dist[outside]++; if(outside>=2) rejected++; checked++;
        }
        System.out.printf("POSITION_%s_ROLLING|trainingMin=%d|checked=%d|rejected=%d|rejectedPct=%.6f|kept=%d|keptPct=%.6f%n",label,minTraining,checked,rejected,pct(rejected,checked),checked-rejected,pct(checked-rejected,checked));
        for(int i=0;i<dist.length;i++) System.out.printf("POSITION_%s_ROLLING_DIST|outside=%d|count=%d|pct=%.6f%n",label,i,dist[i],pct(dist[i],checked));
        assertEquals(987-minTraining,checked);
    }

    private static double pct(long c,long t){return 100.0*c/t;}
    private static List<List<Integer>> loadDrawsNewestFirst() throws Exception {
        InputStream stream=PositionalRangeRuleTest.class.getResourceAsStream("/eurojackpot_archiv.csv");
        if(stream==null) throw new IllegalStateException("Missing /eurojackpot_archiv.csv");
        List<List<Integer>> draws=new ArrayList<>();
        try(BufferedReader reader=new BufferedReader(new InputStreamReader(stream,StandardCharsets.UTF_8))){String line;while((line=reader.readLine())!=null){if(line.isBlank())continue;String[] columns=line.split(",");draws.add(Arrays.stream(columns[1].split("-")).map(Integer::valueOf).sorted().toList());}}
        return draws;
    }
}
