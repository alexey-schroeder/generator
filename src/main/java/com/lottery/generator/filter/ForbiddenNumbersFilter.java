package com.lottery.generator.filter;

import com.lottery.generator.model.LotteryResult;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.lottery.generator.theory.TheoryUtils.calculateIntersection;

@Component
public class ForbiddenNumbersFilter {

    public PredictedResultFilter filter(LotteryResult lotteryResult, List<Integer> forbiddenNumbers) {
        List<Integer> intersection = calculateIntersection(lotteryResult.getBasisNumbers(), forbiddenNumbers);
        String reason = "no intersection";
        if (!intersection.isEmpty()) {
            reason = "There is intersection with forbidden numbers: " + intersection;
        }

        return PredictedResultFilter.builder()
                .filterName(getClass().getSimpleName())
                .result(intersection.isEmpty())
                .reason(reason)
                .oldResults(null)
                .predictedResult(lotteryResult.getBasisNumbers())
                .build();
    }
}
