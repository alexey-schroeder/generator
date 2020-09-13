package com.lottery.generator;

import com.lottery.generator.category.Categories;
import com.lottery.generator.category.Category;
import com.lottery.generator.model.LotteryResult;
import com.lottery.generator.theory.XBasisNumbersWereGotYetTheory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ActualStatistic {

    @Autowired
    private XBasisNumbersWereGotYetTheory xBasisNumbersWereGotYetTheory;

    public  void printSameNumbersResultsBySameNumbersAmount(List<LotteryResult> lotteryResults, int sameNumbersAmount) {
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

    public static List<Integer> getNumberExistInLotteryResult(int number, List<LotteryResult> lotteryResults) {
        return lotteryResults.stream()
                .map(result -> result.getBasisNumbers().contains(number))
                .map(contains -> contains ? 1 : 0)
                .collect(Collectors.toList());
    }

    public static Map<Integer, List<Integer>> getLotteryCountsMapWithoutNumber(List<LotteryResult> lotteryResults, int maxBasisNumber) {
        Map<Integer, List<Integer>> result = new HashMap<>();
        for (int i = 1; i <= maxBasisNumber; i++) {
            List<Integer> integerList = getLotteryCountsWithoutNumber(lotteryResults, i);
            result.put(i, integerList);
        }
        return result;
    }

    public static void printNumbersWithMaxPause(List<LotteryResult> lotteryResults, int maxBusisNumber) {
        Map<Integer, List<Integer>> lotteryCountsWithoutNumber = getLotteryCountsMapWithoutNumber(lotteryResults, maxBusisNumber);
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
            if (tempResult != 0) {
                return tempResult;
            }
            return o2.get(1) - o1.get(1);
        });
        for (List<Integer> count : allCounts) {
            System.out.println(MessageFormat.format("{0}: {1}", inversedMap.get(count), count));
        }
    }

    public static List<Integer> getLotteryCountsWithoutNumber(List<LotteryResult> lotteryResults, int number) {
        List<Integer> existsList = getNumberExistInLotteryResult(number, lotteryResults);
        String listAsString = getListAsString(existsList);
        String[] split = listAsString.split("1");
        return Arrays.stream(split).map(array -> array.length()).collect(Collectors.toList());
    }

    public static List<List<Integer>> calculateDistancesBetweenNumberInLotteryResult(List<LotteryResult> lotteryResults) {
        return lotteryResults.stream().
                map(ActualStatistic::calculateDistancesBetweenNumberInLotteryResult).
                collect(Collectors.toList());
    }

    public static List<Integer> calculateDistancesBetweenNumberInLotteryResult(LotteryResult lotteryResult) {
        List<Integer> basisNumbers = lotteryResult.getBasisNumbers();
        List<Integer> result = new ArrayList<>(basisNumbers.size() - 1);
        for (int i = 0; i < basisNumbers.size() - 1; i++) {
            int distance = basisNumbers.get(i + 1) - basisNumbers.get(i);
            result.add(distance);
        }
        return result;
    }

    public static Map<LotteryResult, Integer> calculateBasisNumbersSum(List<LotteryResult> lotteryResults) {
        return lotteryResults.stream().
                collect(Collectors.toMap(
                        result -> result,
                        result -> result.getBasisNumbers().stream().mapToInt(Integer::intValue).sum()));
    }

    public void printEachNumberExistInLotteryResult(List<LotteryResult> lotteryResults) {
        for (int i = 1; i <= 50; i++) {
            List<Integer> existsList = getNumberExistInLotteryResult(i, lotteryResults);
            System.out.println(MessageFormat.format("{0}: {1}", i, existsList));
        }
    }

    public static Map<LotteryResult, List<Integer>> calculateCategoriesForResults(List<LotteryResult> lotteryResults, Categories categories) {
        return lotteryResults.stream().
                collect(Collectors.toMap(
                        Function.identity(),
                        lotteryResult -> categories.calculateIndexes(lotteryResult)));
    }

    //как долго повторяется в последних результатах один и тот же индекс
    //например над0 посмотреть сколько раз за последнее время был в середине индекс 0
    public static int getIndexDepthForCategory(List<LotteryResult> lotteryResults, Category category) {
        LotteryResult lastLotteryResult = lotteryResults.get(0);
        int lastIndexInCategory = getBasisNumberIndexInCategory(lastLotteryResult, category);

        for (int i = 1; i < lotteryResults.size(); i++) {
            int indexInCategory = getBasisNumberIndexInCategory(lotteryResults.get(i), category);
            if (lastIndexInCategory != indexInCategory) {
                return i;
            }
        }
        return lotteryResults.size();
    }

    private static String getListAsString(List<Integer> list) {
        return list.stream().map(i -> i.toString()).collect(Collectors.joining());
    }

    public static int getBasisNumberIndexInCategory(LotteryResult lotteryResult, Category category) {
        int basisNumberInCategory = lotteryResult.getBasisNumbers().get(category.getIndexInBasisNumbers());
        return category.getIndexForNumber(basisNumberInCategory);
    }

    /**
     * @param lotteryResults
     * @param categories
     * @param threshold
     * @return список позиций на которых стоит нуль -> количество этих позиций в прошлах резултатах
     * например 0_3_4 -> 125 , значит варианты где на нулевом,третьем и четвертом месте были нули
     * встречаются 125 раз
     */
    public static List<Map.Entry<String, Integer>> calculateStatisticZeroPositionsInResult(List<LotteryResult> lotteryResults, Categories categories, double threshold) {
        Map<String, Integer> map = new HashMap<>();

        for (int i = 0; i < lotteryResults.size(); i++) {
            LotteryResult lotteryResult = lotteryResults.get(i);
            List<Integer> basisNumbers = lotteryResult.getBasisNumbers();
            for (int a = 0; a < basisNumbers.size() - 2; a++) {
                Category categoryA = categories.getCategoryByIndexInBasisNumbers(a);
                if (categoryA.getIndexForNumber(basisNumbers.get(a)) == 0) {
                    for (int b = a + 1; b < basisNumbers.size() - 1; b++) {
                        Category categoryB = categories.getCategoryByIndexInBasisNumbers(b);
                        int indexForB = categoryB.getIndexForNumber(basisNumbers.get(b));
                        if (indexForB == 0) {
                            for (int c = b + 1; c < basisNumbers.size(); c++) {
                                Category categoryC = categories.getCategoryByIndexInBasisNumbers(c);
                                int indexForC = categoryC.getIndexForNumber(basisNumbers.get(c));
                                if (indexForC == 0) {
                                    map.merge(a + "_" + b + "_" + c, 1, (integer, integer2) -> integer + 1);
                                }
                            }
                        }
                    }
                }
            }
        }

        List<Map.Entry<String, Integer>> list = new ArrayList<>(map.entrySet());
        list.sort((o1, o2) -> o2.getValue() - o1.getValue());
        return list;

    }

    public static List<List<Integer>> calculateMinimalListWithZerosByThreshold(List<Map.Entry<String, Integer>> entries, int place, double threshold) {
        List<Map.Entry<String, Integer>> listWitZerosOnStart = entries.stream()
                .filter(entry -> entry.getKey().contains("" + place))
                .collect(Collectors.toList());


        boolean found = false;
        int index;
        int sum = 0;
        for (index = 0; index < listWitZerosOnStart.size() && !found; index++) {
            Map.Entry<String, Integer> entry = listWitZerosOnStart.get(index);
            sum = sum + entry.getValue();
            found = sum >= threshold;
        }

        return listWitZerosOnStart.subList(0, index).stream()
                .map(entry -> entry.getKey())
                .map(string -> {
                    String[] strings = string.split("_");
                    return Arrays.stream(strings).map(Integer::valueOf).collect(Collectors.toList());
                })
                .collect(Collectors.toList());
    }

    public static int getBasePositionWithZero(List<LotteryResult> lotteryResults, Categories categories) {

        long maxCount = 0;
        int result = -1;
        for (int index = 0; index < lotteryResults.get(0).getBasisNumbers().size(); index++) {
            List<List<Integer>> indexes = lotteryResults.stream().map(lotteryResult -> categories.calculateIndexes(lotteryResult)).collect(Collectors.toList());
            int finalIndex = index;
            long listsWithZerroOnIndex = indexes.stream().filter(indexesList -> indexesList.get(finalIndex).equals(0)).count();
            if (listsWithZerroOnIndex > maxCount) {
                maxCount = listsWithZerroOnIndex;
                result = index;
            }
        }
        return result;
    }

    public static Map<List<Integer>, Long> calculateCountOfIndexes(List<LotteryResult> lotteryResults, Categories categories) {
        return lotteryResults.stream()
                .map(result -> categories.calculateIndexes(result))
                .collect(Collectors.groupingBy(e -> e, Collectors.counting()));
    }
}
