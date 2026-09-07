package com.lottery.generator.predictor;

import com.lottery.generator.category.Category;
import com.lottery.generator.category.CategoryIndexValues;
import com.lottery.generator.model.LotteryResult;
import org.junit.jupiter.api.Test;

import java.time.temporal.ValueRange;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CategoryPredictorTest {

    @Test
    void getIndexDepthProbabilities_shouldReturnCorrectResult() {
        Map<Integer, CategoryIndexValues> rangeMap = Map.of(
                0, range(1, 6),
                1, range(7, 12),
                2, range(13, 17),
                3, range(18, 22),
                4, range(23, 50));

        Category category = new Category("testCategory", 0, rangeMap);

        List<LotteryResult> lotteryResults = List.of(
                createLotteryResult(List.of(1, 7, 13, 18, 23)),
                createLotteryResult(List.of(7, 13, 18, 23, 41)),
                createLotteryResult(List.of(8, 13, 18, 23, 41)),
                createLotteryResult(List.of(13, 15, 18, 23, 41)),
                createLotteryResult(List.of(14, 15, 18, 23, 41)),
                createLotteryResult(List.of(15, 16, 18, 23, 41)),
                createLotteryResult(List.of(8, 13, 18, 23, 41)),
                createLotteryResult(List.of(14, 15, 18, 23, 41)),
                createLotteryResult(List.of(15, 16, 18, 23, 41)),
                createLotteryResult(List.of(8, 13, 18, 23, 41)),
                createLotteryResult(List.of(15, 16, 18, 23, 41))
        );

        Map<Integer, Map<Integer, Double>> expectedIndexDepthProbabilities = Map.of(
                0, Map.of(1, 1.0),
                1, Map.of(1, 0.666, 2, 0.33),
                2, Map.of(3, 0.33, 2, 0.33, 1, 0.33)
        );

        CategoryPredictor categoryPredictor = new CategoryPredictorFactory().categoryPredictor(category, lotteryResults);
        assertTrue(equals(expectedIndexDepthProbabilities, categoryPredictor.getIndexDepthProbabilities()));
    }

    @Test
    void nextIndexWithProbabilityInCategory_shouldReturnCorrectResult() {
        Map<Integer, CategoryIndexValues> rangeMap = Map.of(
                0, range(1, 10),
                1, range(11, 20),
                2, range(21, 30),
                3, range(31, 40),
                4, range(41, 50));

        Category category = new Category("testCategory", 0, rangeMap);

        List<LotteryResult> lotteryResults = List.of(
                createLotteryResult(List.of(1, 7, 13, 18, 23)),
                createLotteryResult(List.of(7, 13, 18, 23, 41)),
                createLotteryResult(List.of(8, 13, 18, 23, 41)),
                createLotteryResult(List.of(13, 15, 18, 23, 41)),
                createLotteryResult(List.of(14, 15, 18, 23, 41)),
                createLotteryResult(List.of(25, 26, 38, 43, 41)),
                createLotteryResult(List.of(8, 13, 18, 23, 41)),
                createLotteryResult(List.of(14, 15, 18, 23, 41)),
                createLotteryResult(List.of(15, 16, 18, 23, 41)),
                createLotteryResult(List.of(8, 13, 18, 23, 41)),
                createLotteryResult(List.of(15, 16, 18, 23, 41)),
                createLotteryResult(List.of(38, 39, 40, 41, 45)),
                createLotteryResult(List.of(8, 14, 18, 23, 41))
        );

        Map<Integer, Map<Integer, Double>> expectedNextIndexProbabilities = Map.of(
                0, Map.of(0, 0.25, 2, 0.5, 3, 0.25),
                1, Map.of(0, 0.25, 2, 0.5, 3, 0.25),
                2, Map.of(1, 1.0),
                3, Map.of(2, 1.0)
        );

        CategoryPredictor categoryPredictor = new CategoryPredictorFactory().categoryPredictor(category, lotteryResults);
        assertTrue(equals(expectedNextIndexProbabilities, categoryPredictor.getNextIndexWithProbabilityInCategory()));
    }

    private CategoryIndexValues range(long min, long max) {
        return CategoryIndexValues.from(ValueRange.of(min, max), List.of());
    }

    private LotteryResult createLotteryResult(List<Integer> basisNumbers) {
        return LotteryResult.builder().basisNumbers(basisNumbers).build();
    }

    private boolean equals(Map<Integer, Map<Integer, Double>> expected, Map<Integer, Map<Integer, Double>> actual) {
        if (!equalsBySizeAndKeys(expected, actual)) return false;
        for (Map.Entry<Integer, Map<Integer, Double>> mapEntry : expected.entrySet()) {
            Integer expectedKey = mapEntry.getKey();
            Map<Integer, Double> expectedValue = mapEntry.getValue();
            Map<Integer, Double> actualValue = actual.get(expectedKey);
            if (!equalsBySizeAndKeys(expectedValue, actualValue)) return false;
            for (Map.Entry<Integer, Double> expectedValuesEntry : expectedValue.entrySet()) {
                double actualDouble = actualValue.get(expectedValuesEntry.getKey());
                if (Math.abs(expectedValuesEntry.getValue() - actualDouble) > 0.1) return false;
            }
        }
        return true;
    }

    private boolean equalsBySizeAndKeys(Map expected, Map actual) {
        return expected.size() == actual.size() && expected.keySet().containsAll(actual.keySet());
    }
}
