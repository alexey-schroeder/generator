package com.lottery.generator;

import com.lottery.generator.model.LotteryResult;
import com.lottery.generator.theory.XBasisNumbersWereGotYetTheory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

@Component
public class ActualStatistic {

    @Autowired
    private XBasisNumbersWereGotYetTheory xBasisNumbersWereGotYetTheory;

    public void printSameNumbersResultsByNumberCounter(ArrayList<LotteryResult> lotteryResults, int counter) {
        int sameResults = 0;
        for (int i = 0; i < lotteryResults.size() - 1; i++) {
            List<LotteryResult> subSetLotteryResult = lotteryResults.subList(i + 1, lotteryResults.size() - 1);
            List<LotteryResult> sameLotteryResults = xBasisNumbersWereGotYetTheory
                    .wereBasisNumbersGotYet(lotteryResults.get(i).getBasisNumbers(), subSetLotteryResult, counter);

            for (LotteryResult lotteryResult : sameLotteryResults) {
                if (!lotteryResult.getDate().equals(lotteryResults.get(i).getDate())) {
                    sameResults++;
                    System.out.println(MessageFormat.format("found {0} same numbers in lottery results {1} and {2}",
                            counter, lotteryResults.get(i), lotteryResult));
                }
            }
        }
        if (sameResults == 0) {
            System.out.println(MessageFormat.format("found no intersactions with counter {0}", counter));
        } else {
            System.out.println(MessageFormat.format("found {0} intersactions with counter {1} in {2} results",
                    sameResults, counter, lotteryResults.size()));
        }
    }
}
