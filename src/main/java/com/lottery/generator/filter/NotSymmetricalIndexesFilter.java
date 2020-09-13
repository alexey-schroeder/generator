package com.lottery.generator.filter;

import com.lottery.generator.model.LotteryResult;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.List;

@Component
public class NotSymmetricalIndexesFilter implements MillionDayItalyPredictedIndexesFilter {
    @Override
    public PredictedResultFilter filter(List<LotteryResult> lotteryResults, List<Integer> predictedIndexes) {
        boolean isSymmetrical = predictedIndexes.get(0).equals(predictedIndexes.get(4)) &&
                predictedIndexes.get(1).equals(predictedIndexes.get(3));

        String reason = "indexes are not symmetrical";
        if (isSymmetrical) {
            reason = MessageFormat.format("The predicted indexes {0} are symmetrical. Indexes may not be symmetrical!", predictedIndexes);
        }

        return PredictedResultFilter.builder()
                .oldResults(lotteryResults)
                .predictedResult(predictedIndexes)
                .reason(reason)
                .result(!isSymmetrical)
                .build();
    }
}
