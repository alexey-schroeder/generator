package com.lottery.generator;

import com.lottery.generator.category.EuroJackpotCategories;
import com.lottery.generator.category.MillionDayItalyCategories;
import com.lottery.generator.model.CheckResult;
import com.lottery.generator.model.LotteryResult;
import com.lottery.generator.predictor.MillionDayLotteryPredictor;
import com.lottery.generator.resultreader.EuroJackpotLotteryResultsReader;
import com.lottery.generator.resultreader.MillionDayItalyLotteryResultsReader;
import com.lottery.generator.theory.AllBasisNumbersWereGotYetTheory;
import com.lottery.generator.theory.LotteryResultContainsXNumbersFromLastResults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootApplication
public class GeneratorApplication implements CommandLineRunner {

    //https://innen.hessen.de/sites/default/files/media/hmdis/white_list.pdf

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

    @Autowired
    private MillionDayLotteryPredictor millionDayLotteryPredictor;

    public static void main(String[] args) {
        SpringApplicationBuilder builder = new SpringApplicationBuilder(GeneratorApplication.class);
        builder.headless(false);
        ConfigurableApplicationContext context = builder.run(args);
//        SpringApplication.run(GeneratorApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        ArrayList<LotteryResult> lotteryResults = euroJackpotLotteryResultsReader.readLotteryResults();

        ChartUtils.saveLotteryResultsCategoriesAsImage(lotteryResults, 55, "millionday.bmp");
//        boolean basisNumbersGotYet = allBasisNumbersWereGotYetTheory
//                .wereBasisNumbersGotYet(Arrays.asList(3, 25, 31, 32, 48), lotteryResults);
//        System.out.println(basisNumbersGotYet);

//        actualStatistic.printSameNumbersResultsBySameNumbersAmount(lotteryResults, 3);
//        actualStatistic.printSameNumbersResultsBySameNumbersAmountAndMaxDeepOfSearchInAllResults(lotteryResults, 3, 14);
//        actualStatistic.printEachNumberExistInLotteryResult(lotteryResults);
//        actualStatistic.printNumbersWithMaxPause(lotteryResults, 55);
//        calculatedLotteryResultChecker.setOldLotteryResults(lotteryResults);
//        CheckResult checkResult = calculatedLotteryResultChecker.checkResult(LotteryResult.builder()
//                .basisNumbers(Arrays.asList(3, 7, 19, 34, 35))
//                .build());
//        System.out.println(checkResult);

//        calculatedLotteryResultChecker.setOldLotteryResults(lotteryResults);
//        List<LotteryResult> newResults = new ArrayList<>(8);
//        while (newResults.size() != 8) {
//            LotteryResult generatedLotteryResult = resultGenerator.generateLotteryResult();
//            CheckResult checkResult = calculatedLotteryResultChecker.checkResult(generatedLotteryResult);
//            if (checkResult.isApproved()) {
//                newResults.add(generatedLotteryResult);
//            } else {
//                System.out.println(MessageFormat.format("generated result {0} was not approved." +
//                        "Check result: {1}", generatedLotteryResult, checkResult));
//            }
//        }
//        newResults.stream().map(res->res.getBasisNumbers()).collect(Collectors.toList()).forEach(System.out::println);

//        ChartUtils.saveLotteryResultsCategoriesAsImage(lotteryResults, 50, "millionDayItaly.jpeg");

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

//        int index = 0;
//
//        List<List<Integer>> lists = lotteryResults.stream().map(result -> euroJackpotCategories.calculateIndexes(result)).collect(Collectors.toList());
//        long listsWithZerroInMiddle = lists.stream().filter(indexesList -> indexesList.get(index).equals(0)).count();
////
//        System.out.println("with 0 in index " + index + " : " + listsWithZerroInMiddle * 1.0 / lotteryResults.size());
//
//        long countOfListWithOneInMiddle = lists.stream().filter(indexesList -> indexesList.get(index).equals(1)).count();
////        long countOfListWithFirstNumbersGreaterThanZero = lists.stream().filter(indexesList -> indexesList.get(0) > 0 && indexesList.get(1) > 0).count();
//        System.out.println("with 1 in index " + index + " : " + countOfListWithOneInMiddle * 1.0 / lotteryResults.size());
//        long countOfListWithMinusOneInMiddle = lists.stream().filter(indexesList -> indexesList.get(index).equals(-1)).count();
//        System.out.println("with -1 in index " + index + " : " + countOfListWithMinusOneInMiddle * 1.0 / lotteryResults.size());
//
//        long countWithSymmetricalIndexes = lists.stream().filter(
//                indexesList ->
//                        indexesList.get(0).equals(indexesList.get(4)) &&
//                                indexesList.get(1).equals(indexesList.get(3))).count();
//
//        System.out.println("with symmetrical indexes: " + countWithSymmetricalIndexes * 1.0 / lists.size());
//        lists.forEach(System.out::println);


//        millionDayLotteryPredictor.predictBasisNumbers(lotteryResults);
//        bestPlaces.forEach(System.out::println);
//        int frequency = 3;
//        long countByFrequency = lotteryResults.stream()
//                .filter(lotteryResult -> {
//                    List<Integer> indexes = millionDayItalyCategories.calculateIndexes(lotteryResult);
//                    return Collections.frequency(indexes, Integer.valueOf(0)) >= frequency;
//                })
//                .count();
//        System.out.println(countByFrequency * 1.0 / lotteryResults.size());

//        List<Integer> indexes = List.of(0,0,-1,0,1);
//
//        int size = lotteryResults.stream()
//                .map(result -> euroJackpotCategories.calculateIndexes(result))
//                .filter(result ->
//                        result.get(0).equals(indexes.get(0)) &&
//                                result.get(1).equals(indexes.get(1)) &&
//                                result.get(2).equals(indexes.get(2)) &&
//                                result.get(3).equals(indexes.get(3)) &&
//                                result.get(4).equals(indexes.get(4)))
//                .collect(Collectors.toList()).size();
//        System.out.println(indexes+": " + size + " from " + lotteryResults.size() + "; " + size * 1.0 / lotteryResults.size() * 100 + "%");
//
//        lotteryResults.stream()
//                .map(result -> euroJackpotCategories.calculateIndexes(result))
//                .forEach(System.out::println);

//        Map<List<Integer>, Long> counts =
//                ActualStatistic.calculateCountOfIndexes(lotteryResults, euroJackpotCategories);
//        counts.entrySet().forEach(System.out::println);
////        predictedIndexes.forEach(System.out::println);
////        lotteryResults.stream().map(result -> euroJackpotCategories.calculateIndexes(result)).forEach(System.out::println);
//        int size = counts.entrySet().stream().filter(entry -> entry.getValue() < 2).collect(Collectors.toList()).size();
//
//        System.out.println(size*1.0 / lotteryResults.size());

//        List<List<Integer>> indexes = lotteryResults.stream().map(result -> euroJackpotCategories.calculateIndexes(result)).collect(Collectors.toList());
//        boolean result = Collections.frequency(indexes, predictedIndexes) >= minFrequency;


//        System.out.println("#########");
//        lotteryResults.forEach(GeneratorApplication::print);

//        actualStatistic.printSameNumbersResultsBySameNumbersAmountAndMaxDeepOfSearchInAllResults(lotteryResults, 3, 12);

        List<Integer> forbiddenNumbers = List.of();
        LotteryResult lotteryResult = LotteryResult.builder()
                .basisNumbers(List.of(9, 19, 32, 35, 50))
                .additionallyNumbers(List.of(1, 2))
                .date(Instant.now())
                .build();

        calculatedLotteryResultChecker.setOldLotteryResults(lotteryResults);
        CheckResult checkResult = calculatedLotteryResultChecker.check(lotteryResult, forbiddenNumbers);
        System.out.println(checkResult.isApproved() + ":  reason -> " + checkResult.getReason());

    }

    private static void print(LotteryResult lotteryResult) {
        String collect = lotteryResult.getBasisNumbers().stream().map(Object::toString).collect(Collectors.joining(";"));
        String date = DateTimeFormatter.ISO_INSTANT.format(lotteryResult.getDate().truncatedTo(ChronoUnit.DAYS));

        System.out.println(date.replace("T00:00:00Z", "") + ";" + collect);
    }
}
