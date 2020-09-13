package com.lottery.generator.filter;

import com.lottery.generator.category.EuroJackpotCategories;
import com.lottery.generator.model.LotteryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SeldomIndexListFilter implements EuroJackpotPredictedIndexesFilter {
    @Autowired
    private EuroJackpotCategories euroJackpotCategories;

    private int minFrequency = 1;

    @Override
    public PredictedResultFilter filter(List<LotteryResult> lotteryResults, List<Integer> predictedIndexes) {
        if (predictedIndexes.stream().filter(index -> index > 5).count() > 0) {
            throw new IllegalArgumentException("It seems, it are not indexes but numbers!");
        }

        List<List<Integer>> indexes = lotteryResults.stream().map(result -> euroJackpotCategories.calculateIndexes(result)).collect(Collectors.toList());
        boolean result = Collections.frequency(indexes, predictedIndexes) >= minFrequency;
        String reason;
        if (result) {
            reason = "Frequency of predicted indexes in old result is greater than min frequency of " + minFrequency;
        } else {
            reason = "Frequency of predicted indexes in old result is less than min frequency of " + minFrequency;
        }

        return PredictedResultFilter.builder()
                .filterName(getClass().getSimpleName())
                .result(result)
                .reason(reason)
                .oldResults(lotteryResults)
                .predictedResult(predictedIndexes)
                .build();
    }
}
