package com.lottery.generator;

import com.lottery.generator.model.LotteryResult;
import com.lottery.generator.theory.AllBasisNumbersWereGotYetTheory;
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
//        actualStatistic.printSameNumbersResultsBySameNumbersAmountAndMaxDeepOfSearchInAllResults(lotteryResults, 1, 3);
//        actualStatistic.printEachNumberExistInLotteryResult(lotteryResults);
        Map<Integer, List<Integer>> lotteryCountsWithoutNumber = actualStatistic.getLotteryCountsWithoutNumber(lotteryResults);
        Map<List<Integer>, Integer> inversedMap = lotteryCountsWithoutNumber.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
        List<List<Integer>> allCounts = new ArrayList<>(inversedMap.keySet());
        allCounts.sort((o1, o2) -> {
            int tempResult = o2.get(0) - o1.get(0);
            if (tempResult != 0) {
                return tempResult;
            }
            tempResult = o1.size() - o2.size();
            if(tempResult != 0){
                return tempResult;
            }
            return o2.get(1) - o1.get(1);
        });
        for (List<Integer> count : allCounts) {
            System.out.println(MessageFormat.format("{0}: {1}", inversedMap.get(count), count));
        }
    }
}
