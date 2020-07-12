package com.lottery.generator.predictor;

import com.lottery.generator.category.Category;
import com.lottery.generator.model.LotteryResult;
import org.junit.jupiter.api.Test;

import java.time.temporal.ValueRange;
import java.util.List;
import java.util.Map;

import static java.time.temporal.ValueRange.of;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CategoryPredictorTest {

    @Test
    void getIndexDepthProbabilities_shouldReturnCorrectResult() {
        //given
        Map<Integer, ValueRange> rangeMap = Map.of(
                0, of(1, 6),
                1, of(7, 12),
                2, of(13, 17),
                3, of(18, 22),
                4, of(23, 50));

        Category category = new Category("testCategory", 0, rangeMap);

        List<LotteryResult> lotteryResults = List.of(
                createLotteryResult(List.of(1, 7, 13, 18, 23)), // 0, 1, 2, 3, 4 <- indexes in category
                createLotteryResult(List.of(7, 13, 18, 23, 41)), //1, 2, 3, 4, 4
                createLotteryResult(List.of(8, 13, 18, 23, 41)), //1, 2, 3, 4, 4
                createLotteryResult(List.of(13, 15, 18, 23, 41)), //2, 2, 3, 4, 4
                createLotteryResult(List.of(14, 15, 18, 23, 41)), //2, 2, 3, 4, 4
                createLotteryResult(List.of(15, 16, 18, 23, 41)), //2, 2, 3, 4, 4
                createLotteryResult(List.of(8, 13, 18, 23, 41)), //1, 2, 3, 4, 4
                createLotteryResult(List.of(14, 15, 18, 23, 41)), //2, 2, 3, 4, 4
                createLotteryResult(List.of(15, 16, 18, 23, 41)), //2, 2, 3, 4, 4
                createLotteryResult(List.of(8, 13, 18, 23, 41)), //1, 2, 3, 4, 4
                createLotteryResult(List.of(15, 16, 18, 23, 41)) //2, 2, 3, 4, 4
        );

        Map<Integer, Map<Integer, Double>> expectedIndexDepthProbabilities = Map.of(
                0, Map.of(1, 1.0),
                1, Map.of(1, 0.666, 2, 0.33),
                2, Map.of(3, 0.33, 2, 0.33, 1, 0.33)
        );

        CategoryPredictor categoryPredictor = new CategoryPredictorFactory().categoryPredictor(category, lotteryResults);

        //when
        Map<Integer, Map<Integer, Double>> indexDepthProbabilities = categoryPredictor.getIndexDepthProbabilities();

        //then
        assertTrue(equals(expectedIndexDepthProbabilities, indexDepthProbabilities));
    }

    @Test
    void nextIndexWithProbabilityInCategory_shouldReturnCorrectResult() {
        //given
        Map<Integer, ValueRange> rangeMap = Map.of(
                0, of(1, 10),
                1, of(11, 20),
                2, of(21, 30),
                3, of(31, 40),
                4, of(41, 50));

        Category category = new Category("testCategory", 0, rangeMap);

        List<LotteryResult> lotteryResults = List.of(
                createLotteryResult(List.of(1, 7, 13, 18, 23)), // 0, 0, 1, 1, 3 <- indexes in category
                createLotteryResult(List.of(7, 13, 18, 23, 41)), //0, 1, 1, 3, 4
                createLotteryResult(List.of(8, 13, 18, 23, 41)), //0, 1, 1, 2, 4
                createLotteryResult(List.of(13, 15, 18, 23, 41)), //1, 1, 1, 2, 4
                createLotteryResult(List.of(14, 15, 18, 23, 41)), //1, 1, 1, 2, 4
                createLotteryResult(List.of(25, 26, 38, 43, 41)), //2, 2, 3, 4, 4
                createLotteryResult(List.of(8, 13, 18, 23, 41)), //0, 1, 1, 2, 4
                createLotteryResult(List.of(14, 15, 18, 23, 41)), //1, 1, 1, 2, 4
                createLotteryResult(List.of(15, 16, 18, 23, 41)), //1, 1, 1, 2, 4
                createLotteryResult(List.of(8, 13, 18, 23, 41)), //0, 1, 1, 2, 4
                createLotteryResult(List.of(15, 16, 18, 23, 41)), //1, 1, 1, 2, 4
                createLotteryResult(List.of(38, 39, 40, 41, 45)), //3, 3, 4, 4, 4
                createLotteryResult(List.of(8, 14, 18, 23, 41)) //0, 1, 1, 2, 4
        );

        Map<Integer, Map<Integer, Double>> expectedNextIndexProbabilities = Map.of(

                0, Map.of(0, 0.25, 2, 0.5, 3, 0.25),
                1, Map.of(0, 0.25, 2, 0.5, 3, 0.25),
                2, Map.of(1, 1.0),
                3, Map.of(2, 1.0)
        );

        CategoryPredictor categoryPredictor = new CategoryPredictorFactory().categoryPredictor(category, lotteryResults);

        //when
        Map<Integer, Map<Integer, Double>> actualNextIndexProbabilities = categoryPredictor.getNextIndexWithProbabilityInCategory();

        //then
        assertTrue(equals(expectedNextIndexProbabilities, actualNextIndexProbabilities));
    }

    private LotteryResult createLotteryResult(List<Integer> basisNumbers) {
        return LotteryResult.builder().
                basisNumbers(basisNumbers).
                build();
    }

    private boolean equals(Map<Integer, Map<Integer, Double>> expected, Map<Integer, Map<Integer, Double>> actual) {
        if (!equalsBySizeAndKeys(expected, actual)) {
            return false;
        }

        for (Map.Entry<Integer, Map<Integer, Double>> mapEntry : expected.entrySet()) {
            Integer expectedKey = mapEntry.getKey();
            Map<Integer, Double> expectedValue = mapEntry.getValue();
            Map<Integer, Double> actualValue = actual.get(expectedKey);

            if (!equalsBySizeAndKeys(expectedValue, actualValue)) {
                return false;
            }

            for (Map.Entry<Integer, Double> expectedValuesEntry : expectedValue.entrySet()) {
                Integer expectedInteger = expectedValuesEntry.getKey();
                double expectedDouble = expectedValuesEntry.getValue();
                double actualDouble = actualValue.get(expectedInteger);
                if (Math.abs(expectedDouble - actualDouble) > 0.1) {
                    return false;
                }

            }
        }
        return true;
    }

    private boolean equalsBySizeAndKeys(Map expected, Map actual) {
        if (expected.size() != actual.size()) {
            return false;
        }

        if (!expected.keySet().containsAll(actual.keySet())) {
            return false;
        }

        return true;
    }
}