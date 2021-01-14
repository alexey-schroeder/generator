package com.lottery.generator.filter;

import com.lottery.generator.model.LotteryResult;
import org.springframework.stereotype.Component;

@Component
public class EvenOrOddNumbersOnlyFilter {
    public PredictedResultFilter filter(LotteryResult predictedNumbers) {
        String reason = "is ok";
        boolean result = true;
        int sum = predictedNumbers.getBasisNumbers().stream().mapToInt(number -> number % 2).sum();
        if (sum >= 4 || sum < 1) {
            reason = "the numbers contains only even or odd numbers";
            result = false;
        }

        return PredictedResultFilter.builder()
                .filterName(getClass().getSimpleName())
                .result(result)
                .reason(reason)
                .oldResults(null)
                .predictedResult(predictedNumbers.getBasisNumbers())
                .build();
    }
}
