package com.lottery.generator;

import com.lottery.generator.category.EuroJackpotCategories;
import com.lottery.generator.filter.EvenOrOddNumbersOnlyFilter;
import com.lottery.generator.filter.ForbiddenNumbersFilter;
import com.lottery.generator.filter.PredictedResultFilter;
import com.lottery.generator.filter.SeldomIndexListFilter;
import com.lottery.generator.model.CheckResult;
import com.lottery.generator.model.LotteryResult;
import com.lottery.generator.theory.AllBasisNumbersWereGotYetTheory;
import com.lottery.generator.theory.LotteryResultContainsXNumbersFromLastResults;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class CalculatedLotteryResultChecker {

    @Autowired
    private EuroJackpotCategories euroJackpotCategories;

    @Setter
    private List<LotteryResult> oldLotteryResults;

    @Autowired
    private AllBasisNumbersWereGotYetTheory allBasisNumbersWereGotYetTheory;

    @Autowired
    private LotteryResultContainsXNumbersFromLastResults lotteryResultContainsXNumbersFromLastResult;

    @Autowired
    private SeldomIndexListFilter seldomIndexListFilter;

    @Autowired
    private ForbiddenNumbersFilter forbiddenNumbersFilter;

    @Autowired
    private EvenOrOddNumbersOnlyFilter evenOrOddNumbersOnlyFilter;

    public CheckResult check(LotteryResult lotteryResult, List<Integer> forbiddenNumbers) {
        if (oldLotteryResults == null || oldLotteryResults.isEmpty()) {
            throw new IllegalStateException("Old lottery results are not set!");
        }

        PredictedResultFilter resultFilter = forbiddenNumbersFilter.filter(lotteryResult, forbiddenNumbers);
        if (!resultFilter.getResult()) {
            return CheckResult.builder()
                    .resultFilter(resultFilter)
                    .reason(resultFilter.getReason())
                    .approved(false)
                    .build();
        }

        PredictedResultFilter wereBasisNumbersGotYetResult = allBasisNumbersWereGotYetTheory.existOldLotteryResultWithSameBasisNumbers(lotteryResult.getBasisNumbers(), oldLotteryResults);
        if (wereBasisNumbersGotYetResult.getResult()) {
            return CheckResult.builder()
                    .resultFilter(wereBasisNumbersGotYetResult)
                    .approved(false)
                    .reason(wereBasisNumbersGotYetResult.getReason())
                    .build();
        }

        PredictedResultFilter theoryResult = lotteryResultContainsXNumbersFromLastResult.hasXSameNumbersFromLastResult(lotteryResult, oldLotteryResults);
        if (theoryResult.getResult()) {
            return CheckResult.builder()
                    .resultFilter(theoryResult)
                    .approved(false)
                    .reason(theoryResult.getReason())
                    .build();
        }

        List<Integer> indexes = euroJackpotCategories.calculateIndexes(lotteryResult);

        PredictedResultFilter seldomIndexFilterResult = seldomIndexListFilter.filter(oldLotteryResults, indexes);
        if (!seldomIndexFilterResult.getResult()) {
            return CheckResult.builder()
                    .resultFilter(seldomIndexFilterResult)
                    .approved(false)
                    .reason(seldomIndexFilterResult.getReason())
                    .build();
        }

        PredictedResultFilter evenOrOddNumbersOnlyResult = evenOrOddNumbersOnlyFilter.filter(lotteryResult);
        if (!evenOrOddNumbersOnlyResult.getResult()) {
            return CheckResult.builder()
                    .resultFilter(evenOrOddNumbersOnlyResult)
                    .approved(false)
                    .reason(evenOrOddNumbersOnlyResult.getReason())
                    .build();
        }

        return CheckResult.builder()
                .approved(true)
                .reason("Every check was ok")
                .build();
    }
}
