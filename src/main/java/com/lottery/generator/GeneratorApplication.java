package com.lottery.generator;

import com.lottery.generator.category.EuroJackpotCategories;
import com.lottery.generator.category.MillionDayItalyCategories;
import com.lottery.generator.model.LotteryResult;
import com.lottery.generator.printer.Printer;
import com.lottery.generator.resultreader.EuroJackpotLotteryResultsReader;
import com.lottery.generator.resultreader.MillionDayItalyLotteryResultsReader;
import com.lottery.generator.theory.AllBasisNumbersWereGotYetTheory;
import com.lottery.generator.theory.LotteryResultContainsXNumbersFromLastResults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SpringBootApplication
public class GeneratorApplication implements CommandLineRunner {

    @Autowired
    private EuroJackpotLotteryResultsReader euroJackpotLotteryResultsReader;

    @Autowired
    private MillionDayItalyLotteryResultsReader millionDayItalyLotteryResultsReader;

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

    @Autowired
    private EuroJackpotCategories euroJackpotCategories;

    @Autowired
    private MillionDayItalyCategories millionDayItalyCategories;

    public static void main(String[] args) {
        SpringApplicationBuilder builder = new SpringApplicationBuilder(GeneratorApplication.class);
        builder.headless(false);
        ConfigurableApplicationContext context = builder.run(args);
//        SpringApplication.run(GeneratorApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        ArrayList<LotteryResult> lotteryResults = millionDayItalyLotteryResultsReader.readLotteryResults();

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
//        while (newResults.size() != 8) {
//            LotteryResult generatedLotteryResult = resultGenerator.generateLotteryResult();
//            CheckResult checkResult = calculatedLotteryResultChecker.check(generatedLotteryResult);
//            if (checkResult.isApproved()) {
//                newResults.add(generatedLotteryResult);
//            } else {
//                System.out.println(MessageFormat.format("generated result {0} was not approved." +
//                        "Check result: {1}", generatedLotteryResult, checkResult));
//            }
//        }
//        newResults.stream().map(res->res.getBasisNumbers()).collect(Collectors.toList()).forEach(System.out::println);

//        ChartUtils.saveLotteryResultsCategoriesAsImage(lotteryResults, 55,"millionDayItaly.jpeg");

//        List<Integer> absIndexes = lotteryResults.stream().map(result -> millionDayItalyCategories.calculateAbsIndex(result)).collect(Collectors.toList());
//        for (int i = 0; i < 10; i++) {
//            int finalI = i;
//            long count = absIndexes.stream().filter(v -> v.equals(finalI)).count();
//            System.out.println(MessageFormat.format("{0} -> {1}", i, count * 1.0 / lotteryResults.size()));
//        }
//
//        List<Integer> indexes = lotteryResults.stream().map(result -> millionDayItalyCategories.calculateIndex(result)).collect(Collectors.toList());
//        for (int i = -10; i < 10; i++) {
//            int finalI = i;
//            long count = indexes.stream().filter(v -> v.equals(finalI)).count();
//            System.out.println(MessageFormat.format("{0} -> {1}", i, count * 1.0 / lotteryResults.size()));
//        }

        List<List<Integer>> lists = lotteryResults.stream().map(result -> millionDayItalyCategories.calculateIndexes(result)).collect(Collectors.toList());
        long listsWithZerroInMiddle = lists.stream().filter(indexesList -> indexesList.get(2).equals(0)).count();
//
        System.out.println(listsWithZerroInMiddle * 1.0 / lotteryResults.size());

        long countOfListWithOneInMiddle = lists.stream().filter(indexesList -> indexesList.get(2).equals(1)).count();
//        long countOfListWithFirstNumbersGreaterThanZero = lists.stream().filter(indexesList -> indexesList.get(0) > 0 && indexesList.get(1) > 0).count();
        System.out.println(countOfListWithOneInMiddle * 1.0 / lotteryResults.size());
        long countOfListWithMinusOneInMiddle = lists.stream().filter(indexesList -> indexesList.get(2).equals(-1)).count();
        System.out.println(countOfListWithMinusOneInMiddle * 1.0 / lotteryResults.size());

//        long countWithSimetricalIndexes = lists.stream().filter(
//                indexesList ->
//                        indexesList.get(0).equals(indexesList.get(4)) &&
//                                indexesList.get(1).equals(indexesList.get(3))).count();
//
//        System.out.println(countWithSimetricalIndexes * 1.0 / lists.size());
//        lists.forEach(System.out::println);

        Map<LotteryResult, List<Integer>> categories = ActualStatistic.calculateCategoriesForResults(lotteryResults, millionDayItalyCategories);
        Printer.print(categories);

        int depth = ActualStatistic.getIndexDepthForCategory(lotteryResults.subList(2, lotteryResults.size()), millionDayItalyCategories.getCategoryC());
        System.out.println(depth);
    }
}
