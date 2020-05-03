package com.lottery.generator;

import com.lottery.generator.model.CheckResult;
import com.lottery.generator.model.LotteryResult;
import com.lottery.generator.theory.AllBasisNumbersWereGotYetTheory;
import com.lottery.generator.theory.LotteryResultContainsXNumbersFromLastResults;
import com.lottery.generator.model.TheoryResult;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class CalculatedLotteryResultChecker {

    @Setter
    private List<LotteryResult> oldLotteryResults;

    @Autowired
    private AllBasisNumbersWereGotYetTheory allBasisNumbersWereGotYetTheory;

    @Autowired
    private LotteryResultContainsXNumbersFromLastResults lotteryResultContainsXNumbersFromLastResult;

    public CheckResult check(LotteryResult lotteryResult) {
        if(oldLotteryResults== null || oldLotteryResults.isEmpty()){
            throw new IllegalStateException("Old lottery results are not set!");
        }

        TheoryResult wereBasisNumbersGotYetResult = allBasisNumbersWereGotYetTheory.existOldLotteryResultWithSameBasisNumbers(lotteryResult.getBasisNumbers(), oldLotteryResults);
        if (wereBasisNumbersGotYetResult.getResult()) {
            return CheckResult.builder()
                    .theoryResult(wereBasisNumbersGotYetResult)
                    .approved(false)
                    .build();
        }

        TheoryResult theoryResult = lotteryResultContainsXNumbersFromLastResult.hasXSameNumbersFromLastResult(lotteryResult, oldLotteryResults);
        if (theoryResult.getResult()) {
            return CheckResult.builder()
                    .theoryResult(theoryResult)
                    .approved(false)
                    .build();
        }

        return CheckResult.builder()
                .approved(true)
                .reason("Every check was ok")
                .build();
    }
}
