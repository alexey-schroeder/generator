package com.lottery.generator;

import com.lottery.generator.model.LotteryResult;
import com.lottery.generator.theory.AllBasisNumbersWereGotYetTheory;
import com.lottery.generator.theory.XBasisNumbersWereGotYetTheory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SpringBootApplication
public class GeneratorApplication implements CommandLineRunner {

    @Autowired
    private LotteryResultsReader lotteryResultsReader;

    @Autowired
    private AllBasisNumbersWereGotYetTheory allBasisNumbersWereGotYetTheory;

    @Autowired
    private XBasisNumbersWereGotYetTheory xBasisNumbersWereGotYetTheory;

    public static void main(String[] args) {
        SpringApplication.run(GeneratorApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        ArrayList<LotteryResult> lotteryResults = lotteryResultsReader.readLotteryResults();

        boolean basisNumbersGotYet = allBasisNumbersWereGotYetTheory
                .wereBasisNumbersGotYet(Arrays.asList(3, 25, 31, 32, 48), lotteryResults);
        System.out.println(basisNumbersGotYet);

//        int sameResults = 0;
//        int counter = 4;
//        for (int i = 0; i < lotteryResults.size() - 1; i++) {
//            List<LotteryResult> sameLotteryResults = xBasisNumbersWereGotYetTheory
//                    .wereBasisNumbersGotYet(lotteryResults.get(i).getBasisNumbers(), lotteryResults, counter);
//
//            for (LotteryResult lotteryResult : sameLotteryResults) {
//                if (!lotteryResult.getDate().equals(lotteryResults.get(i).getDate())) {
//                    sameResults++;
//                    System.out.println(MessageFormat.format("found {0} same numbers in lottery results {1} and {2}",
//                            counter, lotteryResults.get(i), lotteryResult));
//                }
//            }
//        }
//        if (sameResults == 0) {
//            System.out.println(MessageFormat.format("found no intersactions with counter {0}", counter));
//        } else {
//            System.out.println(MessageFormat.format("found {0} intersactions with counter {1} in {2} results",
//                    sameResults, counter, lotteryResults.size()));
//        }
    }
}
