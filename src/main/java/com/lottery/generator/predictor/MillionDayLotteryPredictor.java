package com.lottery.generator.predictor;

import com.lottery.generator.ActualStatistic;
import com.lottery.generator.category.Category;
import com.lottery.generator.category.MillionDayItalyCategories;
import com.lottery.generator.model.LotteryResult;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@AllArgsConstructor
public class MillionDayLotteryPredictor {

    private MillionDayItalyCategories categories;

    private final Map<Integer, Map<Integer, Double>> indexDepthProbabilities =
            //format: index -> Map<depth ->probability> of the same index
            //when there is no key(f.e. index 0 -> depth 6), then probability is 0.0
            Map.of(
                    0, Map.of(
                            6, 0.0,//for index 0 is probability of depth 6 0.0%
                            5, 0.1,
                            4, 0.15,
                            3, 0.2,
                            2, 0.25,
                            1, 0.25),

                    1, Map.of(
                            2, 0.1,
                            1, 0.2),
                    -1, Map.of(
                            1, 0.1
                    )
            );

    //when the index can not be appear anymore
    private final Map<Integer, Map<Integer, Double>> nextIndexWithProbabilityInCategoryA = Map.of(
            //format: last index -> Map<next index-> probability>
            0, Map.of(
                    1, 0.45,//when 0 can not appear any more, than the next index is 1 with probability 0.5
                    -1, 0.45,
                    2, 0.05,
                    -2, 0.05
            )

    );

    public void predict(List<LotteryResult> oldLotteryResults) {
        int nextIndexInCategoryA = predictNextIndexInCategoryA(oldLotteryResults);
        List<Integer> newBasisNumbers = predictBasisNumbers(oldLotteryResults, nextIndexInCategoryA);
    }

    private List<Integer> predictBasisNumbers(List<LotteryResult> oldLotteryResults, int nextIndexInCategoryA) {
        return null;
    }

    private int predictNextIndexInCategoryA(List<LotteryResult> oldLotteryResults) {
        Category categoryA = categories.getCategoryA();
        LotteryResult lastLotteryResult = oldLotteryResults.get(0);
        List<Integer> basisNumbers = lastLotteryResult.getBasisNumbers();
        int indexForNumber = categoryA.getIndexForNumber(basisNumbers.get(categoryA.getIndexInBasisNumbers()));
        Map<Integer, Double> depthProbabilitiesForIndex = indexDepthProbabilities.get(indexForNumber);

        //the same index can not appear any more because the index is very seldom
        if (depthProbabilitiesForIndex == null) {
            return 0;
        }

        int actualDepth = ActualStatistic.getIndexDepthForCategory(oldLotteryResults, categoryA);
        int predictDepth = predictIndexDepth(depthProbabilitiesForIndex);
        if (actualDepth < predictDepth) {
            return indexForNumber;
        }

        return predictNextIndexOfNumber(indexForNumber);
    }

    private Integer predictIndexDepth(Map<Integer, Double> depthProbabilitiesForIndex) {
        ArrayList<Integer> probabilities = fillListWithValuesByProbability(depthProbabilitiesForIndex);
        return getRandomValue(probabilities);
    }

    private int predictNextIndexOfNumber(Integer indexForNumber) {
        Map<Integer, Double> nextIndexProbabilities = nextIndexWithProbabilityInCategoryA.get(indexForNumber);
        if (nextIndexProbabilities == null) {
            return 0;
        }
        ArrayList<Integer> nextPossibleIndexForNumberList = fillListWithValuesByProbability(nextIndexProbabilities);
        return getRandomValue(nextPossibleIndexForNumberList);
    }

    private Integer getRandomValue(ArrayList<Integer> values) {
        Collections.shuffle(values);
        int randomPositionInList = new Random().nextInt(99);
        return values.get(randomPositionInList);
    }

    private ArrayList<Integer> fillListWithValuesByProbability(Map<Integer, Double> nextIndexProbabilities) {
        ArrayList<Integer> nextPossibleIndexForNumberList = new ArrayList<>(100);
        for (Map.Entry<Integer, Double> entry : nextIndexProbabilities.entrySet()) {
            Integer index = entry.getKey();
            double probability = entry.getValue();
            for (int i = 0; i < probability * 100; i++) {
                nextPossibleIndexForNumberList.add(index);
            }
        }

        return nextPossibleIndexForNumberList;
    }
}
