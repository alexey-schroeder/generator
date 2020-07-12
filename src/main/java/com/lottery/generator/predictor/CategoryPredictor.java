package com.lottery.generator.predictor;

import com.lottery.generator.ActualStatistic;
import com.lottery.generator.category.Category;
import com.lottery.generator.model.LotteryResult;
import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;

@Getter
public class CategoryPredictor {

    private Category category;
    //format: index -> Map<depth ->probability> of the same index
    //when there is no key(f.e. index 0 -> depth 6), then probability is 0.0
    private Map<Integer, Map<Integer, Double>> indexDepthProbabilities;

    //format: last index -> Map<next index-> probability>
    private Map<Integer, Map<Integer, Double>> nextIndexWithProbabilityInCategory;
    private List<LotteryResult> oldLotteryResults;
    private List<Integer> oldIndexes;

    public CategoryPredictor(Category category, List<LotteryResult> oldLotteryResults) {
        this.category = category;
        this.oldLotteryResults = oldLotteryResults;
        initIndexDepthProbabilities();
        initNextIndexWithProbabilityInCategory();
        initOldIndexes();
    }

    public int predictNextIndexInCategory(List<LotteryResult> oldLotteryResults) {
        LotteryResult lastLotteryResult = oldLotteryResults.get(0);
        List<Integer> basisNumbers = lastLotteryResult.getBasisNumbers();
        int actualIndex = category.getIndexForNumber(basisNumbers.get(category.getIndexInBasisNumbers()));
//        Map<Integer, Double> depthProbabilitiesForIndex = indexDepthProbabilities.get(actualIndex);

        int actualDepth = ActualStatistic.getIndexDepthForCategory(oldLotteryResults, category);
//        int predictDepth = predictIndexDepth(depthProbabilitiesForIndex);
//        if (actualDepth < predictDepth) { //the same index can be get
//            return actualIndex;
//        }

        int predictedIndex = predictNextIndexOfNumber(actualIndex);
//        if (predictedIndex == actualIndex) {
//            boolean checkDepth = checkDepth(actualIndex, actualDepth);
//            if (checkDepth) {
//                return predictedIndex;
//            }
//            return predictNextIndexOfNumber(actualIndex);
//        }

        return predictedIndex;

    }

    private boolean checkDepth(int actualIndex, int actualDepth) {
        Map<Integer, Double> depthProbabilities = indexDepthProbabilities.get(actualIndex);
        return depthProbabilities.containsKey(actualDepth + 1);
    }


    private Integer predictIndexDepth(Map<Integer, Double> depthProbabilitiesForIndex) {
        ArrayList<Integer> probabilities = fillListWithValuesByProbability(depthProbabilitiesForIndex);
        return getRandomValue(probabilities);
    }

    private int predictNextIndexOfNumber(Integer indexForNumber) {

        return getRandomValue(oldIndexes);
    }

    private Integer getRandomValue(List<Integer> values) {
        List<Integer> copy = new ArrayList<>(values);
        Collections.shuffle(copy);
        int randomPositionInList = new Random().nextInt(copy.size());
        return copy.get(randomPositionInList);
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

    private void initIndexDepthProbabilities() {
        Map<Integer, Map<Integer, Integer>> numberOfDepth = new HashMap<>();
        List<LotteryResult> tempResults = new ArrayList<>(oldLotteryResults);
        while (!tempResults.isEmpty()) {
            int index = ActualStatistic.getBasisNumberIndexInCategory(tempResults.get(0), category);
            int depth = ActualStatistic.getIndexDepthForCategory(tempResults, category);

            Map<Integer, Integer> indexDepthMap = numberOfDepth.get(index);
            if (indexDepthMap == null) {
                indexDepthMap = new HashMap<>();
                numberOfDepth.put(index, indexDepthMap);
            }

            Integer depthCount = indexDepthMap.get(depth);
            if (depthCount == null) {
                depthCount = 0;
            }
            Integer newDepthNumber = Integer.valueOf(depthCount + 1);
            indexDepthMap.put(depth, newDepthNumber);

            tempResults = tempResults.subList(depth, tempResults.size());
        }

        indexDepthProbabilities = new HashMap<>();
        for (Map.Entry<Integer, Map<Integer, Integer>> entry : numberOfDepth.entrySet()) {
            Map<Integer, Double> depthProbabilityMap = new HashMap<>();
            Map<Integer, Integer> depths = entry.getValue();
            int depthSum = depths.values().stream().mapToInt(Integer::valueOf).sum();
            for (Map.Entry<Integer, Integer> depth : depths.entrySet()) {
                double depthProbability = depth.getValue() * 1.0 / depthSum;
                depthProbabilityMap.put(depth.getKey(), depthProbability);
            }
            indexDepthProbabilities.put(entry.getKey(), depthProbabilityMap);
        }
    }

    private void initNextIndexWithProbabilityInCategory() {
        List<LotteryResult> tempResults = new ArrayList<>(oldLotteryResults);
        Collections.reverse(tempResults);
        Map<Integer, Map<Integer, Integer>> numberIndexChanges = new HashMap<>();

        for (int i = 0; i < tempResults.size() - 1; i++) {
            List<Integer> actualBasisNumbers = tempResults.get(i).getBasisNumbers();
            int actualIndex = category.getIndexForNumber(actualBasisNumbers.get(category.getIndexInBasisNumbers()));

            List<Integer> nextBasisNumbers = tempResults.get(i + 1).getBasisNumbers();
            int nextIndex = category.getIndexForNumber(nextBasisNumbers.get(category.getIndexInBasisNumbers()));

            Map<Integer, Integer> mapForActualIndex = numberIndexChanges.get(actualIndex);
            if (mapForActualIndex == null) {
                mapForActualIndex = new HashMap<>();
                numberIndexChanges.put(actualIndex, mapForActualIndex);
            }

            mapForActualIndex.merge(nextIndex, 1, (oldIndex, newIndex) -> oldIndex + 1);
        }

        nextIndexWithProbabilityInCategory = new HashMap<>();
        for (Map.Entry<Integer, Map<Integer, Integer>> entry : numberIndexChanges.entrySet()) {
            Map<Integer, Double> nextIndexProbabilityMap = new HashMap<>();
            Map<Integer, Integer> changesCounts = entry.getValue();
            int changesCountSum = changesCounts.values().stream().mapToInt(Integer::valueOf).sum();
            for (Map.Entry<Integer, Integer> nextIndex : changesCounts.entrySet()) {
                double indexProbability = nextIndex.getValue() * 1.0 / changesCountSum;
                nextIndexProbabilityMap.put(nextIndex.getKey(), indexProbability);
            }
            nextIndexWithProbabilityInCategory.put(entry.getKey(), nextIndexProbabilityMap);
        }
    }

    private void initOldIndexes() {
        oldIndexes = oldLotteryResults.stream()
                .map(lotteryResult -> {
                    Integer basisNumber = lotteryResult.getBasisNumbers().get(category.getIndexInBasisNumbers());
                    return category.getIndexForNumber(basisNumber);
                })
                .collect(Collectors.toList());
    }
}
