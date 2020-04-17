package com.lottery.generator.theory;

import com.lottery.generator.model.LotteryResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@Component
public class AllBasisNumbersWereGotYetTheory {

    public boolean wereBasisNumbersGotYet(List<Integer> newNumbers, List<LotteryResult> oldLotteryResults) {
        List<Integer> newNumbersCopy = new ArrayList<>(newNumbers);
        newNumbersCopy.sort(Integer::compareTo);
        String newNumbersCopyAsString = newNumbersCopy.toString();

        return oldLotteryResults.parallelStream().anyMatch(
                areNumbersEquals(newNumbersCopyAsString));
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
