package com.lottery.generator.filter;

import com.lottery.generator.category.MillionDayItalyCategories;
import com.lottery.generator.model.LotteryResult;
import org.junit.jupiter.api.Test;

import java.util.List;

class NotPossiblePredictedLotteryResultFilterTest {

    @Test
    void filter() {
        NotPossiblePredictedLotteryResultFilter filter = new NotPossiblePredictedLotteryResultFilter(new MillionDayItalyCategories());
        PredictedResultFilter result = filter.filter(
                List.of(LotteryResult.builder().
                        basisNumbers(List.of(1,2,3,4,5)).
                        build())

                , List.of(0, 0, -1, -2, -4));

    }
}