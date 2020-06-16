package com.lottery.generator;

import com.lottery.generator.category.Categories;
import com.lottery.generator.eurojackpot.EuroJackpotLotteryResultsReader;
import com.lottery.generator.model.LotteryResult;
import com.lottery.generator.theory.AllBasisNumbersWereGotYetTheory;
import com.lottery.generator.theory.LotteryResultContainsXNumbersFromLastResults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.time.temporal.ValueRange;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootApplication
public class GeneratorApplication implements CommandLineRunner {

    @Autowired
    private EuroJackpotLotteryResultsReader lotteryResultsReader;

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
    private Categories categories;

    public static void main(String[] args) {
        SpringApplicationBuilder builder = new SpringApplicationBuilder(GeneratorApplication.class);
        builder.headless(false);
        ConfigurableApplicationContext context = builder.run(args);
//        SpringApplication.run(GeneratorApplication.class, args);
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

//        ChartUtils.saveLotteryResultsCategoriesAsImage(lotteryResults, "XYLineChart.jpeg");

        List<Integer> indexes = lotteryResults.stream().map(result -> categories.calculateIndex(result)).collect(Collectors.toList());
        long count0 = indexes.stream().filter(v -> v.equals(0)).count();
        System.out.println("0 -> " + count0 * 1.0 / lotteryResults.size());

        long count1 = indexes.stream().filter(v -> v.equals(1)).count();
        System.out.println("1 -> " + count1 * 1.0 / lotteryResults.size());

        long count2 = indexes.stream().filter(v -> v.equals(2)).count();
        System.out.println("2 -> " + count2 * 1.0 / lotteryResults.size());

        long count3 = indexes.stream().filter(v -> v.equals(3)).count();
        System.out.println("3 -> " + count3 * 1.0 / lotteryResults.size());

        long count4 = indexes.stream().filter(v -> v.equals(4)).count();
        System.out.println("4 -> " + count4 * 1.0 / lotteryResults.size());

        long count5 = indexes.stream().filter(v -> v.equals(5)).count();
        System.out.println("5 -> " + count5 * 1.0 / lotteryResults.size());

        long count6 = indexes.stream().filter(v -> v.equals(6)).count();
        System.out.println("6 -> " + count6 * 1.0 / lotteryResults.size());

        long count7 = indexes.stream().filter(v -> v.equals(7)).count();
        System.out.println("7 -> " + count7 * 1.0 / lotteryResults.size());
    }
}
