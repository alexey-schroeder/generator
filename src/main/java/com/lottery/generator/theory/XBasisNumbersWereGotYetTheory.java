package com.lottery.generator.theory;

import com.lottery.generator.model.LotteryResult;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.lottery.generator.theory.TheoryUtils.calculateIntersection;

@Component
public class XBasisNumbersWereGotYetTheory {
    public List<LotteryResult> wereBasisNumbersGotYet(List<Integer> newNumbers, List<LotteryResult> oldLotteryResults, int minSameNumbersAmount) {
       List<LotteryResult> result = new ArrayList<>();
        for (LotteryResult lotteryResult : oldLotteryResults) {
            List<Integer> intersection = calculateIntersection(newNumbers, lotteryResult.getBasisNumbers());
            if (intersection.size() >= minSameNumbersAmount) {
                intersection.sort(Integer::compareTo);
                result.add(lotteryResult);
            }
        }
        return result;
    }
}
