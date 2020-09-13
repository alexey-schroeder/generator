package com.lottery.generator.theory;

import com.lottery.generator.filter.PredictedResultFilter;
import com.lottery.generator.model.LotteryResult;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
public class AllBasisNumbersWereGotYetTheory {

    public PredictedResultFilter existOldLotteryResultWithSameBasisNumbers(List<Integer> newNumbers, List<LotteryResult> oldLotteryResults) {
        List<Integer> newNumbersCopy = new ArrayList<>(newNumbers);
        newNumbersCopy.sort(Integer::compareTo);
        String newNumbersCopyAsString = newNumbersCopy.toString();

        List<LotteryResult> lotteryResultsWithSameNumbers = oldLotteryResults.parallelStream()
                .filter(areNumbersEquals(newNumbersCopyAsString))
                .collect(Collectors.toList());
        if (lotteryResultsWithSameNumbers.isEmpty()) {
            return PredictedResultFilter.builder()
                    .filterName(getClass().getSimpleName())
                    .result(false)
                    .reason(MessageFormat.format(
                            "There is no old lottery results with the same numbers {0}", newNumbers))
                    .build();
        } else {
            return PredictedResultFilter.builder()
                    .filterName(getClass().getSimpleName())
                    .result(true)
                    .reason(MessageFormat.format(
                            "Old lottery results with the same numbers {0} found", newNumbers))
                    .oldResults(lotteryResultsWithSameNumbers)
                    .build();
        }
    }

    private Predicate<LotteryResult> areNumbersEquals(String newNumbersCopyAsString) {
        return lotteryResult -> {
            List<Integer> basisNumbers = lotteryResult.getBasisNumbers();
            List<Integer> basisNumbersCopy = new ArrayList<>(basisNumbers);
            basisNumbersCopy.sort(Integer::compareTo);
            return basisNumbersCopy.toString().equals(newNumbersCopyAsString);
        };
    }
}
