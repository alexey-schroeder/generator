package com.lottery.generator.theory;

/*
Общий смысл:
если два числа из нового результата были в последнем результате,
то с большой вероятностью новый результат нужно выкинуть

 предпологаем, что в предыдущем результате не содержится maxAmountOfSameNumbers номеров из предпологаемого  результата
 пример:
 мы сгенерировали предпологаемый результат: 2, 7, 9, 34, 41
 в прошлом результыте были числа: 2, 13, 21, 34, 48

 если мы проверяем, что могут быть не более одного одинакого числа, т.е. "maxAmountOfSameNumbers" = 1
 то результат true, если  "maxAmountOfSameNumbers" = 2, то false,
 т.к. есть два одинаковый числа: 2 и 34.

 Но, так как все таки повторы случаются, то надо учитывать некоторую вероятность.
 Максимальная повторяемость была в 2017 году-> 7 повторов.
 Мы предположим, что в среднем в год бывают 5 повторов.
 За это отвечает параметер maxAmountOfLotteryResultsWithXSameNumbersFromLastResultProYear.

 если он в этом году превышен, то мы считаем,
 что вероятность повтора определяется параметром probabilityOfXNumberRepeatingInPercent
 */

import com.lottery.generator.model.LotteryResult;
import com.lottery.generator.model.TheoryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static com.lottery.generator.theory.TheoryUtils.calculateIntersection;

@Component
public class LotteryResultContainsXNumbersFromLastResults {

    //максимальное количество повторов в год одних и тех же номеров из предыдущих результатов
    private int maxAmountOfLotteryResultsWithXSameNumbersFromLastResultProYear = 5;

    //если количество повторов в текущем году превышает или равно maxAmountOfLotteryResultsWithXSameNumbersFromLastResultProYear,
    //то вероятность повтора номеров из предыдущего результата равна probabilityOfXNumberRepeatingInPercent
    private int probabilityOfXNumberRepeatingInPercent = 3;

    //максимальное количество чисел, которые могут совпадать в newNumbers и последним результатом лотереи
    private int maxAmountOfSameNumbers = 2;

    @Autowired
    private XBasisNumbersWereGotYetTheory xBasisNumbersWereGotYetTheory;

    public TheoryResult hasXSameNumbersFromLastResult(LotteryResult newNumbers, List<LotteryResult> oldLotteryResults) {
        LotteryResultContainsXNumbersFromLastResultsWithDeepSearch withDeepSearch = new LotteryResultContainsXNumbersFromLastResultsWithDeepSearch();
        TheoryResult resultWithDeepSearch = withDeepSearch.hasXSameNumbersFromLastResult(newNumbers, oldLotteryResults);
        if (resultWithDeepSearch.getResult()) {
            return resultWithDeepSearch;
        }

        ZoneId zone = ZoneId.of("Europe/Berlin");
        Instant now = Instant.now();
        int actualYear = now.atZone(zone).getYear();

        ArrayList<LotteryResult> lotteryResultsInActualYear = oldLotteryResults.stream()
                .filter(result -> result.getDate().atZone(zone).getYear() == actualYear)
                .collect(Collectors.toCollection(ArrayList::new));

        if (lotteryResultsInActualYear.isEmpty()) {
            return TheoryResult.builder()
                    .result(false)
                    .theoryName(getClass().getSimpleName())
                    .reason(MessageFormat.format("no old lottery results exist in actual year {0}", actualYear))
                    .build();
        }

        LotteryResult oldLotteryResultToCompare = lotteryResultsInActualYear.get(0);
        List<Integer> intersection = calculateIntersection(newNumbers.getBasisNumbers(), oldLotteryResultToCompare.getBasisNumbers());
        if (intersection.size() < maxAmountOfSameNumbers) {
            return TheoryResult.builder()
                    .theoryName(getClass().getSimpleName())
                    .result(false)
                    .reason(MessageFormat.format(
                            "in last lottery result was found same numbers: {0}. It is less than parameter: {1}",
                            intersection, maxAmountOfSameNumbers))
                    .build();
        }

        if (intersection.size() > maxAmountOfSameNumbers) {
            return TheoryResult.builder()
                    .theoryName(getClass().getSimpleName())
                    .result(true)
                    .reason(MessageFormat.format(
                            "in last lottery result was found same numbers: {0}. It is greater than parameter: {1}",
                            intersection, maxAmountOfSameNumbers))
                    .oldResults(Arrays.asList(oldLotteryResultToCompare))
                    .build();
        }

        int sameNumbersCount = findPreviousResultsWithSameNumbers(lotteryResultsInActualYear);
        int random = new Random().nextInt((100 - 1) + 1) + 1;

        //количество повторов в этом году уже больше или равно чем в среднем
        if (sameNumbersCount >= maxAmountOfLotteryResultsWithXSameNumbersFromLastResultProYear) {
            boolean result = probabilityOfXNumberRepeatingInPercent <= random;
            return TheoryResult.builder()
                    .theoryName(getClass().getSimpleName())
                    .result(result)
                    .reason(MessageFormat.format(
                            "it was found {0} old lottery results in actual year {1, number,#}, that is greater than " +
                                    " the parameter for max amount of old results with same numbers with value {2}." +
                                    "the calculated random was {3}, max probability is set to {4}",
                            sameNumbersCount, actualYear, maxAmountOfLotteryResultsWithXSameNumbersFromLastResultProYear,
                            random, probabilityOfXNumberRepeatingInPercent))
                    .build();
        } else {
            boolean result = probabilityOfXNumberRepeatingInPercent * 3 <= random;
            return TheoryResult.builder()
                    .theoryName(getClass().getSimpleName())
                    .result(result)
                    .reason(MessageFormat.format(
                            "it was found {0} old lottery results in actual year {1, number,#}, that is smaller than " +
                                    " the parameter for max amount of old results with same numbers with value {2}." +
                                    "Therefor  max probability is set to {3}, the calculated random was {4}",
                            sameNumbersCount, actualYear, maxAmountOfLotteryResultsWithXSameNumbersFromLastResultProYear,
                            probabilityOfXNumberRepeatingInPercent * 3, random))
                    .build();
        }
    }

    //считает сколько повторений было в прошлых результатах в текущем году
    private int findPreviousResultsWithSameNumbers(ArrayList<LotteryResult> lotteryResults) {
        int sameNumbersCount = 0;
        for (int i = 0; i < lotteryResults.size() - 1; i++) {
            LotteryResult lotteryResult = lotteryResults.get(i);
            LotteryResult previousLotteryResult = lotteryResults.get(i + 1);
            List<Integer> intersection = calculateIntersection(lotteryResult.getBasisNumbers(), previousLotteryResult.getBasisNumbers());
            if (intersection.size() >= maxAmountOfSameNumbers) {
                sameNumbersCount++;
            }
        }
        return sameNumbersCount;
    }

    //если три числа из нового результата уже были в 13 последних результатах,
    // то с вероятностью 10% новый результат нужно выкинуть

    private class LotteryResultContainsXNumbersFromLastResultsWithDeepSearch {

        private int maxDeep = 13;

        //максимальное количество чисел, которые могут совпадать в newNumbers и последним результатом лотереи
        private int maxAmountOfSameNumbers = 3;

        private int probabilityOfXNumberRepeatingInPercent = 10;

        public TheoryResult hasXSameNumbersFromLastResult(LotteryResult newNumbers, List<LotteryResult> oldLotteryResults) {
            List<LotteryResult> lotteryResultForCompare = oldLotteryResults.subList(0, maxDeep + 1);
            List<LotteryResult> resultWithSameNumbers = xBasisNumbersWereGotYetTheory.wereBasisNumbersGotYet(newNumbers.getBasisNumbers(), lotteryResultForCompare, maxAmountOfSameNumbers);

            if (resultWithSameNumbers.isEmpty()) {
                return TheoryResult.builder()
                        .theoryName(this.getClass().getSimpleName())
                        .result(false)
                        .reason(MessageFormat.format(
                                "no old results with search in deep {0} was found with {1} same numbers",
                                maxDeep, maxAmountOfSameNumbers))
                        .build();
            }

            int random = new Random().nextInt((100 - 1) + 1) + 1;
            boolean result = probabilityOfXNumberRepeatingInPercent <= random;

            return TheoryResult.builder()
                    .theoryName(this.getClass().getSimpleName())
                    .result(result)
                    .oldResults(resultWithSameNumbers)
                    .reason(MessageFormat.format(
                            "old results with {0} same numbers were found with deep of search {1}. " +
                                    "Parameter for max random is {2}. Calculated random was {3}",
                            maxAmountOfSameNumbers, maxDeep, probabilityOfXNumberRepeatingInPercent, random))
                    .build();
        }
    }

    //TODO написать проверку для случая, что число не может выпадать три раза вподрят
}
