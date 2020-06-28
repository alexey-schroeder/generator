package com.lottery.generator.predictor;

import com.lottery.generator.ActualStatistic;
import com.lottery.generator.category.Category;
import com.lottery.generator.model.LotteryResult;

import java.util.*;

public interface CategoryPredictor {

    Category getCategory();
    Map<Integer, Map<Integer, Double>> getIndexDepthProbabilities();

    Map<Integer, Map<Integer, Double>> getNextIndexWithProbabilityInCategory();

    default int predictNextIndexInCategoryA(List<LotteryResult> oldLotteryResults) {
        Category category = getCategory();
        LotteryResult lastLotteryResult = oldLotteryResults.get(0);
        List<Integer> basisNumbers = lastLotteryResult.getBasisNumbers();
        int indexForNumber = category.getIndexForNumber(basisNumbers.get(category.getIndexInBasisNumbers()));
        Map<Integer, Double> depthProbabilitiesForIndex = getIndexDepthProbabilities().get(indexForNumber);

        //the same index can not appear any more because the index is very seldom
        if (depthProbabilitiesForIndex == null) {
            return 0;
        }

        int actualDepth = ActualStatistic.getIndexDepthForCategory(oldLotteryResults, category);
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
        Map<Integer, Double> nextIndexProbabilities = getNextIndexWithProbabilityInCategory().get(indexForNumber);
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
