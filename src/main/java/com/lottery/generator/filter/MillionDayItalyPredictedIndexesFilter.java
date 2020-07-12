package com.lottery.generator.filter;

import com.lottery.generator.model.LotteryResult;

import java.util.List;

public interface MillionDayItalyPredictedIndexesFilter {

    PredictedIndexesFilterResult filter(List<LotteryResult> lotteryResults, List<Integer> predictedIndexes);
}
