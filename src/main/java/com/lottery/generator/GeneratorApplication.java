package com.lottery.generator;

import com.lottery.generator.model.LotteryResult;
import com.lottery.generator.theory.AllBasisNumbersWereGotYetTheory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;
import java.util.Arrays;

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

        boolean basisNumbersGotYet = allBasisNumbersWereGotYetTheory
                .wereBasisNumbersGotYet(Arrays.asList(3, 25, 31, 32, 48), lotteryResults);
        System.out.println(basisNumbersGotYet);

//        actualStatistic.printSameNumbersResultsBySameNumbersAmount(lotteryResults, 5);
        actualStatistic.printSameNumbersResultsBySameNumbersAmountAndMaxDeepOfSearchInAllResults(lotteryResults, 1, 3);

    }
}
