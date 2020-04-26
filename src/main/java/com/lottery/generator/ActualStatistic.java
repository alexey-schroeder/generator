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

    public void printSameNumbersResultsBySameNumbersAmount(List<LotteryResult> lotteryResults, int sameNumbersAmount) {
        int sameResults = 0;
        for (int i = 0; i < lotteryResults.size() - 1; i++) {
            List<LotteryResult> subSetLotteryResult = lotteryResults.subList(i + 1, lotteryResults.size() - 1);
            List<LotteryResult> sameLotteryResults = xBasisNumbersWereGotYetTheory
                    .wereBasisNumbersGotYet(lotteryResults.get(i).getBasisNumbers(), subSetLotteryResult, sameNumbersAmount);

            for (LotteryResult lotteryResult : sameLotteryResults) {
                if (!lotteryResult.getDate().equals(lotteryResults.get(i).getDate())) {
                    sameResults++;
                    System.out.println(MessageFormat.format("found {0} same numbers in lottery results {1} and {2}",
                            sameNumbersAmount, lotteryResults.get(i), lotteryResult));
                }
            }
        }
        if (sameResults == 0) {
            System.out.println(MessageFormat.format("found no intersactions with sameNumbersAmount {0}", sameNumbersAmount));
        } else {
            System.out.println(MessageFormat.format("found {0} intersactions with sameNumbersAmount {1} in {2} results",
                    sameResults, sameNumbersAmount, lotteryResults.size()));
        }
    }

    public void printSameNumbersResultsBySameNumbersAmountAndMaxDeepOfSearchInAllResults(ArrayList<LotteryResult> lotteryResults, int sameNumbersAmount, int searchDeep) {
        //TODO special implementation for case for searchDeep == 0?
        for (int i = 0; i < lotteryResults.size() - searchDeep - 1; i++) {
            LotteryResult lotteryResultForCompare = lotteryResults.get(i);
            List<LotteryResult> lotteryResultWithDeep = lotteryResults.subList(i + 1, i + searchDeep + 1);
            List<LotteryResult> sameLotteryResults = xBasisNumbersWereGotYetTheory
                    .wereBasisNumbersGotYet(lotteryResultForCompare.getBasisNumbers(), lotteryResultWithDeep, sameNumbersAmount);
            if (!sameLotteryResults.isEmpty()) {
                System.out.println(MessageFormat.format("found  same numbers in lottery results {0} and {1}",
                        lotteryResultForCompare, sameLotteryResults));
            }
        }
    }
}
