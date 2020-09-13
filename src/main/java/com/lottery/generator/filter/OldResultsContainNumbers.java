package com.lottery.generator.filter;

import com.lottery.generator.model.LotteryResult;
import com.lottery.generator.theory.XBasisNumbersWereGotYetTheory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.List;

@Component
public class OldResultsContainNumbers implements EuroJackpotPredictedIndexesFilter {

    @Autowired
    private XBasisNumbersWereGotYetTheory xBasisNumbersWereGotYetTheory;

    private int depthToCompare = 12;

    private int comparedNumberCount = 3;

    @Override
    public PredictedResultFilter filter(List<LotteryResult> lotteryResults, List<Integer> predictedNumbers) {
        List<LotteryResult> subResult = lotteryResults.subList(0, depthToCompare);

        List<LotteryResult> sameLotteryResults = xBasisNumbersWereGotYetTheory
                .wereBasisNumbersGotYet(predictedNumbers, subResult, comparedNumberCount);
        String reason = MessageFormat.format("No {0} same numbers found in last {1} old lottery results", comparedNumberCount, depthToCompare);

        if(!sameLotteryResults.isEmpty()){
            reason = MessageFormat.format("{0} old results with  same {1} numbers found in last {2} old lottery results", sameLotteryResults.size(), comparedNumberCount, depthToCompare);
        }

        return PredictedResultFilter.builder()
                .filterName(getClass().getSimpleName())
                .result(sameLotteryResults.isEmpty())
                .reason(reason)
                .oldResults(lotteryResults)
                .predictedResult(predictedNumbers)
                .build();
    }
}
