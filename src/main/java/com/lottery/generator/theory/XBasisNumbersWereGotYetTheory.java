package com.lottery.generator.theory;

import com.lottery.generator.model.LotteryResult;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class XBasisNumbersWereGotYetTheory {
    public List<LotteryResult> wereBasisNumbersGotYet(List<Integer> newNumbers, List<LotteryResult> oldLotteryResults, int counter) {
       List<LotteryResult> result = new ArrayList<>();
        for (LotteryResult lotteryResult : oldLotteryResults) {
            List<Integer> intersection = calculateIntersection(newNumbers, lotteryResult.getBasisNumbers());
            if (intersection.size() >= counter) {
                intersection.sort(Integer::compareTo);
                result.add(lotteryResult);
//                System.out.println(MessageFormat.format("founded {0} equals numbers in lists {1}  and {2}: {3}",
//                        intersection.size(), newNumbers, lotteryResult.getBasisNumbers(), intersection));
            }
        }
        return result;
    }

    private List<Integer> calculateIntersection(List<Integer> list, List<Integer> otherList) {
        return list.stream()
                .filter(otherList::contains)
                .collect(Collectors.toList());
    }
}
