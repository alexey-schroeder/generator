package com.lottery.generator.filter;

import com.lottery.generator.model.LotteryResult;

import java.util.List;

public interface MillionDayItalyPredictedIndexesFilter {

    PredictedResultFilter filter(List<LotteryResult> lotteryResults, List<Integer> predictedIndexes);
}
