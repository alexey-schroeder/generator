package com.lottery.generator;

import com.lottery.generator.model.CheckResult;
import com.lottery.generator.model.LotteryResult;
import com.lottery.generator.theory.AllBasisNumbersWereGotYetTheory;
import com.lottery.generator.theory.LotteryResultContainsXNumbersFromLastResults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

@SpringBootApplication
public class GeneratorApplication implements CommandLineRunner {

    @Autowired
    private LotteryResultsReader lotteryResultsReader;

    @Autowired
    private AllBasisNumbersWereGotYetTheory allBasisNumbersWereGotYetTheory;

    @Autowired
    private ActualStatistic actualStatistic;

    @Autowired
    private LotteryResultGenerator resultGenerator;

    @Autowired
    private CalculatedLotteryResultChecker calculatedLotteryResultChecker;

    @Autowired
    private LotteryResultContainsXNumbersFromLastResults lotteryResultContainsXNumbersFromLastResult;

    public static void main(String[] args) {
        SpringApplication.run(GeneratorApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        ArrayList<LotteryResult> lotteryResults = lotteryResultsReader.readLotteryResults();

//        boolean basisNumbersGotYet = allBasisNumbersWereGotYetTheory
//                .wereBasisNumbersGotYet(Arrays.asList(3, 25, 31, 32, 48), lotteryResults);
//        System.out.println(basisNumbersGotYet);

//        actualStatistic.printSameNumbersResultsBySameNumbersAmount(lotteryResults, 5);
//        actualStatistic.printSameNumbersResultsBySameNumbersAmountAndMaxDeepOfSearchInAllResults(lotteryResults, 3, 14);
//        actualStatistic.printEachNumberExistInLotteryResult(lotteryResults);
//        actualStatistic.printNumbersWithMaxPause(lotteryResults);
//        calculatedLotteryResultChecker.setOldLotteryResults(lotteryResults);
//        CheckResult check = calculatedLotteryResultChecker.check(LotteryResult.builder()
//                .basisNumbers(Arrays.asList(3, 7, 19, 34, 35))
//                .build());
//        System.out.println(check);

        calculatedLotteryResultChecker.setOldLotteryResults(lotteryResults);
        List<LotteryResult> newResults = new ArrayList<>(8);
        while (newResults.size() != 8) {
            LotteryResult generatedLotteryResult = resultGenerator.generateLotteryResult();
            CheckResult checkResult = calculatedLotteryResultChecker.check(generatedLotteryResult);
            if (checkResult.isApproved()) {
                newResults.add(generatedLotteryResult);
            } else {
                System.out.println(MessageFormat.format("generated result {0} was not approved." +
                        "Check result: {1}", generatedLotteryResult, checkResult));
            }
        }
        newResults.stream().map(res->res.getBasisNumbers()).collect(Collectors.toList()).forEach(System.out::println);
    }
}
