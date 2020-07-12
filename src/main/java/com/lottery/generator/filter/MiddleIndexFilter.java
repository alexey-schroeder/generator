package com.lottery.generator.filter;

import com.lottery.generator.model.LotteryResult;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.List;

@Component
public class MiddleIndexFilter implements MillionDayItalyPredictedIndexesFilter {
    @Override
    public PredictedIndexesFilterResult filter(List<LotteryResult> lotteryResults, List<Integer> predictedIndexes) {
        Integer middleIndex = predictedIndexes.get(2);
        boolean check = middleIndex == 0 || middleIndex == 1 || middleIndex == -1;
        String reason = "";
        if (!check) {
            reason = MessageFormat.format("The middle index is {0}, but may be 0, 1 or -1", middleIndex);
        }

        return PredictedIndexesFilterResult.builder()
                .filterName(getClass().getSimpleName())
                .result(check)
                .reason(reason)
                .oldResults(lotteryResults)
                .predictedIndexes(predictedIndexes)
                .build();
    }
}
